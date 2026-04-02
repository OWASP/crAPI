import logging
from uuid import uuid4

from langgraph.graph.message import Messages

from .agent_utils import trim_messages_to_token_limit
from .config import Config
from .extensions import db
from .langgraph_agent import execute_langgraph_agent
from .retriever_utils import add_to_chroma_collection

logger = logging.getLogger(__name__)


async def get_chat_history(session_id):
    doc = await db.chat_sessions.find_one({"session_id": session_id})
    messages = doc["messages"] if doc else []
    return messages


async def update_chat_history(session_id, messages):
    await db.chat_sessions.update_one(
        {"session_id": session_id}, {"$set": {"messages": messages}}, upsert=True
    )


async def delete_chat_history(session_id):
    await db.chat_sessions.delete_one({"session_id": session_id})


async def process_user_message(session_id, user_message, api_key, model_name, user_jwt):
    logger.info(
        "Processing user message - session_id: %s, model_name: %s, provider: %s, has_api_key: %s, has_jwt: %s",
        session_id,
        model_name or "(default)",
        Config.LLM_PROVIDER,
        bool(api_key),
        bool(user_jwt),
    )
    logger.info(
        "=== AI CONFIG === provider: %s, model: %s, embeddings_model: %s",
        Config.LLM_PROVIDER,
        model_name or Config.LLM_MODEL_NAME or "(will derive default)",
        Config.EMBEDDINGS_MODEL or "(will derive default)",
    )

    history = await get_chat_history(session_id)
    logger.debug(
        "Retrieved chat history - session_id: %s, history_count: %d",
        session_id,
        len(history),
    )

    # generate a unique numeric id for the message that is random but unique
    source_message_id = uuid4().int & (1 << 63) - 1
    history.append({"id": source_message_id, "role": "user", "content": user_message})
    logger.debug("Added user message to history - message_id: %s", source_message_id)

    # Run LangGraph agent
    response = await execute_langgraph_agent(
        api_key, model_name, history, user_jwt, session_id
    )
    logger.debug("LangGraph agent response received - session_id: %s", session_id)
    logger.debug("Response messages count: %d", len(response.get("messages", [])))

    reply: Messages = response.get("messages", [{}])[-1]
    response_message_id = uuid4().int & (1 << 63) - 1
    history.append(
        {"id": response_message_id, "role": "assistant", "content": reply.content}
    )
    logger.debug(
        "Added assistant response to history - message_id: %s", response_message_id
    )

    add_to_chroma_collection(
        api_key,
        session_id,
        [{"user": user_message}, {"assistant": reply.content}],
        Config.LLM_PROVIDER,
        model_name,
    )
    logger.debug("Added messages to Chroma collection - session_id: %s", session_id)

    history = trim_messages_to_token_limit(history)
    await update_chat_history(session_id, history)
    logger.info(
        "Message processing complete - session_id: %s, response_id: %s, history_count: %d",
        session_id,
        response_message_id,
        len(history),
    )
    return reply.content, response_message_id
