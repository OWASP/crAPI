import json
import logging

from langchain.agents import AgentState
from langchain.agents.middleware.types import before_model
from langchain_core.messages import ToolMessage
from langgraph.runtime import Runtime

from .config import Config

logger = logging.getLogger(__name__)

INDIVIDUAL_MIN_LENGTH = 100
# Approximate characters per token across providers
CHARS_PER_TOKEN = 4


def collect_long_strings(obj):
    field_info = []

    def _collect(obj):
        if isinstance(obj, dict):
            for key, value in obj.items():
                if isinstance(value, str) and len(value) > INDIVIDUAL_MIN_LENGTH:
                    field_info.append(
                        {
                            "length": len(value),
                            "dict": obj,
                            "key": key,
                        }
                    )
                elif isinstance(value, (dict, list)):
                    _collect(value)
        elif isinstance(obj, list):
            for item in obj:
                if isinstance(item, (dict, list)):
                    _collect(item)

    _collect(obj)
    return field_info


def truncate_by_length(content, max_length):
    """
    Truncate JSON content by recursively truncating the longest fields until content is under limit.
    Preserves structure and smaller fields with minimum loss of information.
    """
    try:
        data = json.loads(content)
        field_info = sorted(collect_long_strings(data), key=lambda x: x["length"])

        cur_length = len(json.dumps(data))
        while field_info and cur_length - max_length > 0:
            longest = field_info.pop()
            excess = cur_length - max_length
            new_length = max(INDIVIDUAL_MIN_LENGTH, longest["length"] - excess)
            cur_length -= longest["length"] - new_length
            longest["dict"][longest["key"]] = (
                longest["dict"][longest["key"]][:new_length]
                + f"... [TRUNCATED: {longest['length'] - new_length} chars removed]"
            )

        if cur_length <= max_length:
            return json.dumps(data)
    except (json.JSONDecodeError, Exception):
        pass

    return content[:max_length] + "\n... [TRUNCATED]"


@before_model(state_schema=AgentState)
def truncate_tool_messages(state: AgentState, runtime: Runtime) -> AgentState:
    """
    Modify large tool messages to prevent exceeding model's token limits.
    Truncate to a length such that it keeps messages within your token limit.
    """
    messages = state.get("messages", [])
    modified_messages = []

    for i, msg in enumerate(messages):
        if (
            isinstance(msg, ToolMessage)
            and len(msg.content) > Config.MAX_CONTENT_LENGTH
        ):
            truncated_msg = msg.model_copy(
                update={
                    "content": truncate_by_length(
                        msg.content, Config.MAX_CONTENT_LENGTH
                    )
                }
            )
            modified_messages.append(truncated_msg)
        else:
            modified_messages.append(msg)
    return {"messages": modified_messages}


def _estimate_tokens(text):
    """Estimate token count using character-based approximation."""
    return len(text) // CHARS_PER_TOKEN


def _message_content(msg):
    """Extract text content from a message dict or object."""
    if isinstance(msg, dict):
        return msg.get("content", "")
    return getattr(msg, "content", "")


def trim_messages_to_token_limit(messages):
    """
    Trim conversation history from the oldest messages to fit within the token
    budget derived from MAX_CONTENT_LENGTH.
    The most recent message (the new user turn) is always kept.
    """
    max_tokens = Config.MAX_CONTENT_LENGTH // CHARS_PER_TOKEN

    if not messages:
        return messages

    # Estimate per-message tokens
    token_counts = [_estimate_tokens(_message_content(m)) for m in messages]
    total_tokens = sum(token_counts)

    if total_tokens <= max_tokens:
        return messages

    # Always keep the last message; trim from the front
    trimmed = list(messages)
    trimmed_tokens = list(token_counts)

    while len(trimmed) > 1 and sum(trimmed_tokens) > max_tokens:
        trimmed.pop(0)
        trimmed_tokens.pop(0)

    logger.info(
        "Trimmed conversation history from %d to %d messages "
        "(estimated tokens: %d -> %d, limit: %d)",
        len(messages),
        len(trimmed),
        total_tokens,
        sum(trimmed_tokens),
        max_tokens,
    )
    return trimmed
