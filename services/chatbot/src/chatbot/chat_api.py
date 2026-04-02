import logging
import os
from uuid import uuid4

from quart import Blueprint, jsonify, request

from .agent_utils import trim_messages_to_token_limit
from .chat_service import (delete_chat_history, get_chat_history,
                           process_user_message)
from .config import Config
from .session_service import (get_api_key, get_model_name,
                              get_or_create_session_id, get_user_jwt,
                              store_api_key, store_model_name)

chat_bp = Blueprint("chat", __name__, url_prefix="/genai")
logger = logging.getLogger(__name__)


def _validate_provider_env(provider: str) -> str | None:
    if provider == "openai":
        return None
    if provider == "anthropic":
        return None
    if provider == "azure_openai":
        if not Config.AZURE_OPENAI_API_KEY and not Config.AZURE_AD_TOKEN:
            return "Missing AZURE_OPENAI_API_KEY or AZURE_AD_TOKEN"
        if not Config.AZURE_OPENAI_ENDPOINT:
            return "Missing AZURE_OPENAI_ENDPOINT"
        if not Config.AZURE_OPENAI_CHAT_DEPLOYMENT:
            return "Missing AZURE_OPENAI_CHAT_DEPLOYMENT"
        return None
    if provider == "groq":
        if not Config.GROQ_API_KEY:
            return "Missing GROQ_API_KEY"
        return None
    if provider == "mistral":
        if not Config.MISTRAL_API_KEY:
            return "Missing MISTRAL_API_KEY"
        return None
    if provider == "cohere":
        if not Config.COHERE_API_KEY:
            return "Missing COHERE_API_KEY"
        return None
    if provider == "bedrock":
        if not os.environ.get("AWS_REGION"):
            return "Missing AWS_REGION"
        # Allow: bearer token, static credentials, or assume role (uses instance profile)
        if not Config.AWS_BEARER_TOKEN_BEDROCK and not Config.AWS_ASSUME_ROLE_ARN:
            if not os.environ.get("AWS_ACCESS_KEY_ID") or not os.environ.get(
                "AWS_SECRET_ACCESS_KEY"
            ):
                return "Missing AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY, AWS_ASSUME_ROLE_ARN, or AWS_BEARER_TOKEN_BEDROCK"
        return None
    if provider == "vertex":
        # GOOGLE_APPLICATION_CREDENTIALS is optional if running in GCP with ADC
        if not Config.VERTEX_PROJECT:
            return "Missing VERTEX_PROJECT"
        if not Config.VERTEX_LOCATION:
            return "Missing VERTEX_LOCATION"
        return None
    return f"Unsupported provider {provider}"


@chat_bp.route("/init", methods=["POST"])
async def init():
    session_id = await get_or_create_session_id()
    data = await request.get_json()
    logger.info("Initializing bot for session %s", session_id)
    provider = Config.LLM_PROVIDER
    logger.info(
        "Init AI Config - provider: %s, model: %s, embeddings: %s",
        provider,
        Config.LLM_MODEL_NAME or "(not set)",
        Config.EMBEDDINGS_MODEL or "(not set)",
    )
    if provider == "openai":
        api_key = await get_api_key(session_id)
        if api_key:
            logger.info("Model already initialized with OpenAI API key")
            return jsonify({"message": "Model Already Initialized"}), 200
        if not data:
            logger.error("Invalid request")
            return jsonify({"message": "Invalid request"}), 400
        if "openai_api_key" not in data:
            logger.error("openai_api_key not provided")
            return jsonify({"message": "openai_api_key not provided"}), 400
        openai_api_key: str = data["openai_api_key"]
        logger.debug("OpenAI API Key %s", openai_api_key[:5])
        await store_api_key(session_id, openai_api_key, provider)
        return jsonify({"message": "Initialized"}), 200
    if provider == "anthropic":
        api_key = await get_api_key(session_id)
        if api_key:
            logger.info("Model already initialized with Anthropic API key")
            return jsonify({"message": "Model Already Initialized"}), 200
        if not data:
            logger.error("Invalid request")
            return jsonify({"message": "Invalid request"}), 400
        if "anthropic_api_key" not in data:
            logger.error("anthropic_api_key not provided")
            return jsonify({"message": "anthropic_api_key not provided"}), 400
        anthropic_api_key: str = data["anthropic_api_key"]
        logger.debug("Anthropic API Key %s", anthropic_api_key[:5])
        await store_api_key(session_id, anthropic_api_key, provider)
        return jsonify({"message": "Initialized"}), 200
    error = _validate_provider_env(provider)
    if error:
        logger.error("Provider %s misconfigured: %s", provider, error)
        return jsonify({"message": error}), 400
    return jsonify({"message": f"Initialized ({provider})"}), 200


@chat_bp.route("/model", methods=["POST"])
async def model():
    session_id = await get_or_create_session_id()
    data = await request.get_json()
    model_name = Config.LLM_MODEL_NAME
    model_source = "environment_default"
    if data and "model_name" in data and data["model_name"]:
        model_name = data["model_name"]
        model_source = "user_specified"
    logger.info(
        "Model selection - session_id: %s, model_name: %s, model_source: %s, provider: %s",
        session_id,
        model_name or "(not set)",
        model_source,
        Config.LLM_PROVIDER,
    )
    await store_model_name(session_id, model_name)
    return jsonify({"model_used": model_name}), 200


@chat_bp.route("/ask", methods=["POST"])
async def chat():
    session_id = await get_or_create_session_id()
    provider = Config.LLM_PROVIDER
    logger.info(
        "Chat request received - session_id: %s, provider: %s", session_id, provider
    )

    error = _validate_provider_env(provider)
    if error:
        logger.error(
            "Provider environment validation failed - provider: %s, error: %s",
            provider,
            error,
        )
        return jsonify({"message": error}), 400

    provider_api_key = await get_api_key(session_id)
    model_name = await get_model_name(session_id)
    user_jwt = await get_user_jwt()

    logger.info(
        "=== CHAT AI CONFIG === session_id: %s, provider: %s, model_name: %s, has_api_key: %s, has_jwt: %s",
        session_id,
        provider,
        model_name or "(will derive default)",
        bool(provider_api_key),
        bool(user_jwt),
    )
    logger.info(
        "Environment AI Config - LLM_MODEL_NAME: %s, EMBEDDINGS_MODEL: %s, EMBEDDINGS_DIMENSIONS: %d",
        Config.LLM_MODEL_NAME or "(not set)",
        Config.EMBEDDINGS_MODEL or "(not set)",
        Config.EMBEDDINGS_DIMENSIONS,
    )

    if provider in {"openai", "anthropic"} and not provider_api_key:
        message = (
            "Missing OpenAI API key. Please authenticate."
            if provider == "openai"
            else "Missing Anthropic API key. Please authenticate."
        )
        logger.warning(
            "API key missing for provider - session_id: %s, provider: %s",
            session_id,
            provider,
        )
        return jsonify({"message": message}), 400

    data = await request.get_json()
    logger.debug("Raw request data - type: %s, value: %r", type(data).__name__, data)
    if not isinstance(data, dict):
        logger.warning(
            "Invalid request body - expected JSON object, got %s: %r",
            type(data).__name__,
            data,
        )
        return jsonify({"message": "Invalid request body - expected JSON object"}), 400
    message = data.get("message", "").strip()
    id = data.get("id", uuid4().int & (1 << 63) - 1)
    if not message:
        logger.warning("Empty message received - session_id: %s", session_id)
        return jsonify({"message": "Message is required", "id": id}), 400

    logger.debug(
        "Processing message - session_id: %s, message_length: %d",
        session_id,
        len(message),
    )
    try:
        reply, response_id = await process_user_message(
            session_id, message, provider_api_key, model_name, user_jwt
        )
        logger.info(
            "Chat response sent - session_id: %s, response_id: %s",
            session_id,
            response_id,
        )
        return jsonify({"id": response_id, "message": reply}), 200
    except Exception as e:
        logger.error(
            "Error processing message - session_id: %s, error: %s",
            session_id,
            str(e),
            exc_info=True,
        )
        return jsonify({"id": id, "message": str(e)}), 200


@chat_bp.route("/state", methods=["GET"])
async def state():
    session_id = await get_or_create_session_id()
    logger.debug("Checking state for session %s", session_id)
    provider = Config.LLM_PROVIDER
    provider_api_key = await get_api_key(session_id)
    if provider in {"openai", "anthropic"} and provider_api_key:
        logger.debug(
            "Provider API key for session %s: %s", session_id, provider_api_key[:5]
        )
        chat_history = await get_chat_history(session_id)
        chat_history = trim_messages_to_token_limit(chat_history)
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
    if provider in {"openai", "anthropic"}:
        return (
            jsonify(
                {"initialized": "false", "message": "Model needs to be initialized"}
            ),
            200,
        )
    return jsonify({"initialized": "true", "message": "Model initialized"}), 200


@chat_bp.route("/history", methods=["GET"])
async def history():
    session_id = await get_or_create_session_id()
    logger.debug("Checking state for session %s", session_id)
    provider = Config.LLM_PROVIDER
    provider_api_key = await get_api_key(session_id)
    if provider in {"openai", "anthropic"} and provider_api_key:
        chat_history = await get_chat_history(session_id)
        chat_history = trim_messages_to_token_limit(chat_history)
        return jsonify({"chat_history": chat_history}), 200
    if provider in {"openai", "anthropic"}:
        return (
            jsonify({"chat_history": []}),
            200,
        )
    chat_history = await get_chat_history(session_id)
    chat_history = trim_messages_to_token_limit(chat_history) if chat_history else []
    return jsonify({"chat_history": chat_history}), 200


@chat_bp.route("/reset", methods=["POST"])
async def reset():
    session_id = await get_or_create_session_id()
    logger.debug("Checking state for session %s", session_id)
    await delete_chat_history(session_id)
    return jsonify({"initialized": "false", "message": "Reset successful"}), 200


@chat_bp.route("/health", methods=["GET"])
async def health():
    return jsonify({"message": "OK"}), 200
