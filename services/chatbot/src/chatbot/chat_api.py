"""
HTTP endpoints of the chatbot, mounted at /chatbot/genai by app.py.

Each request is tied to a chat session by the chat_session_id cookie
(session_service). The endpoints:

- POST /init     store the caller's LLM API key for the session (skipped
                 when the container already has one from CHATBOT_LLM_API_KEY)
- POST /model    pick the model name for the session
- POST /ask      run one user message through the LangGraph agent
                 (chat_service.process_user_message) and return the reply
- GET  /state    whether the session is initialised, plus recent history
- GET  /history  the last 20 messages
- POST /reset    delete the session's history
- GET  /health

The caller's own Bearer JWT, if present, is read by session_service and
forwarded to the MCP server, but the tools themselves run with the admin API
key obtained in mcpserver/server.py regardless of who is chatting.
"""

import logging
from uuid import uuid4

from quart import Blueprint, jsonify, request

from .chat_service import (delete_chat_history, get_chat_history,
                           process_user_message)
from .config import Config
from .session_service import (get_api_key, get_model_name,
                              get_or_create_session_id, get_user_jwt,
                              store_api_key, store_model_name)

chat_bp = Blueprint("chat", __name__, url_prefix="/genai")
logger = logging.getLogger(__name__)


@chat_bp.route("/init", methods=["POST"])
async def init():
    session_id = await get_or_create_session_id()
    data = await request.get_json()
    logger.debug("Initializing bot for session %s", session_id)
    api_key = await get_api_key(session_id)
    if api_key:
        logger.info("Model already initialized with LLM API key from environment")
        return jsonify({"message": "Model Already Initialized"}), 200
    elif not data:
        logger.error("Invalid request")
        return jsonify({"message": "Invalid request"}), 400
    elif "openai_api_key" not in data:
        logger.error("openai_api_key not provided")
        return jsonify({"message": "openai_api_key not provided"}), 400
    openai_api_key: str = data["openai_api_key"]
    logger.debug("OpenAI API Key %s", openai_api_key[:5])
    # Save the api key in session
    await store_api_key(session_id, openai_api_key)
    return jsonify({"message": "Initialized"}), 200


@chat_bp.route("/model", methods=["POST"])
async def model():
    session_id = await get_or_create_session_id()
    data = await request.get_json()
    model_name = Config.DEFAULT_MODEL_NAME
    if data and "model_name" in data and data["model_name"]:
        model_name = data["model_name"]
    logger.debug("Setting model %s for session %s", model_name, session_id)
    await store_model_name(session_id, model_name)
    return jsonify({"model_used": model_name}), 200


@chat_bp.route("/ask", methods=["POST"])
async def chat():
    session_id = await get_or_create_session_id()
    openai_api_key = await get_api_key(session_id)
    model_name = await get_model_name(session_id)
    user_jwt = await get_user_jwt()
    if not openai_api_key:
        return jsonify({"message": "Missing OpenAI API key. Please authenticate."}), 400
    data = await request.get_json()
    message = data.get("message", "").strip()
    id = data.get("id", uuid4().int & (1 << 63) - 1)
    if not message:
        return jsonify({"message": "Message is required", "id": id}), 400
    reply, response_id = await process_user_message(
        session_id, message, openai_api_key, model_name, user_jwt
    )
    return jsonify({"id": response_id, "message": reply}), 200


@chat_bp.route("/state", methods=["GET"])
async def state():
    session_id = await get_or_create_session_id()
    logger.debug("Checking state for session %s", session_id)
    openai_api_key = await get_api_key(session_id)
    if openai_api_key:
        logger.debug(
            "OpenAI API Key for session %s: %s", session_id, openai_api_key[:5]
        )
        chat_history = await get_chat_history(session_id)
        # Limit chat history to last 20 messages
        chat_history = chat_history[-20:]
        return (
            jsonify(
                {
                    "initialized": "true",
                    "message": "Model initialized",
                    "chat_history": chat_history,
                }
            ),
            200,
        )
    return (
        jsonify({"initialized": "false", "message": "Model needs to be initialized"}),
        200,
    )


@chat_bp.route("/history", methods=["GET"])
async def history():
    session_id = await get_or_create_session_id()
    logger.debug("Checking state for session %s", session_id)
    openai_api_key = await get_api_key(session_id)
    if openai_api_key:
        chat_history = await get_chat_history(session_id)
        # Limit chat history to last 20 messages
        chat_history = chat_history[-20:]
        return jsonify({"chat_history": chat_history}), 200
    return (
        jsonify({"chat_history": []}),
        200,
    )


@chat_bp.route("/reset", methods=["POST"])
async def reset():
    session_id = await get_or_create_session_id()
    logger.debug("Checking state for session %s", session_id)
    await delete_chat_history(session_id)
    return jsonify({"initialized": "false", "message": "Reset successful"}), 200


@chat_bp.route("/health", methods=["GET"])
async def health():
    return jsonify({"message": "OK"}), 200
