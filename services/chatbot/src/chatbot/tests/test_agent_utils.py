"""Tests for trim_messages_to_token_limit and supporting helpers."""
import sys
from types import ModuleType
from unittest.mock import MagicMock, patch

import pytest

# ---------------------------------------------------------------------------
# Stub out heavy third-party deps so the module can be imported without them.
# ---------------------------------------------------------------------------
_STUBS = {}
for mod_name in [
    "langchain", "langchain.agents", "langchain.agents.middleware",
    "langchain.agents.middleware.types",
    "langchain_core", "langchain_core.messages",
    "langgraph", "langgraph.runtime",
    "motor", "motor.motor_asyncio",
    "langchain_community", "langchain_community.agent_toolkits",
    "langchain_community.utilities",
    "pymongo",
]:
    if mod_name not in sys.modules:
        stub = ModuleType(mod_name)
        sys.modules[mod_name] = stub
        _STUBS[mod_name] = stub

# Provide the decorator used by agent_utils at import time
sys.modules["langchain.agents"].AgentState = dict
sys.modules["langchain.agents.middleware.types"].before_model = (
    lambda **kw: (lambda fn: fn)
)
sys.modules["langchain_core.messages"].ToolMessage = type("ToolMessage", (), {})
sys.modules["langgraph.runtime"].Runtime = type("Runtime", (), {})

# Stub dotenv
dotenv_stub = ModuleType("dotenv")
dotenv_stub.load_dotenv = lambda *a, **kw: None
sys.modules["dotenv"] = dotenv_stub

# Stub dbconnections before config is imported
db_stub = ModuleType("chatbot.dbconnections")
db_stub.CHROMA_HOST = "localhost"
db_stub.CHROMA_PORT = 8000
db_stub.MONGO_CONNECTION_URI = "mongodb://localhost"
db_stub.POSTGRES_URI = "postgresql://localhost"
sys.modules["chatbot.dbconnections"] = db_stub

# Now safe to import the module under test
from chatbot.agent_utils import (
    CHARS_PER_TOKEN,
    _estimate_tokens,
    _message_content,
    trim_messages_to_token_limit,
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_msg(role, content):
    """Return a plain dict message like those stored in chat history."""
    return {"role": role, "content": content}


# ---------------------------------------------------------------------------
# _estimate_tokens
# ---------------------------------------------------------------------------

class TestEstimateTokens:
    def test_empty_string(self):
        assert _estimate_tokens("") == 0

    def test_known_length(self):
        text = "a" * 400  # 400 chars -> 100 tokens
        assert _estimate_tokens(text) == 400 // CHARS_PER_TOKEN

    def test_short_string(self):
        assert _estimate_tokens("hi") == 0  # 2 // 4 == 0


# ---------------------------------------------------------------------------
# _message_content
# ---------------------------------------------------------------------------

class TestMessageContent:
    def test_dict_message(self):
        assert _message_content({"role": "user", "content": "hello"}) == "hello"

    def test_dict_missing_content(self):
        assert _message_content({"role": "user"}) == ""

    def test_object_message(self):
        class Msg:
            content = "from object"
        assert _message_content(Msg()) == "from object"

    def test_object_no_content(self):
        class Msg:
            pass
        assert _message_content(Msg()) == ""


# ---------------------------------------------------------------------------
# trim_messages_to_token_limit
# ---------------------------------------------------------------------------

MAX_CONTENT_LENGTH = 100000  # default


class TestTrimMessagesToTokenLimit:
    """Tests use a patched MAX_CONTENT_LENGTH to keep fixtures small."""

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", 400)
    def test_under_limit_returns_all(self):
        """Messages totalling fewer tokens than the budget are untouched."""
        msgs = [_make_msg("user", "a" * 100), _make_msg("assistant", "b" * 100)]
        result = trim_messages_to_token_limit(msgs)
        assert len(result) == 2
        assert result == msgs

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", 400)
    def test_over_limit_trims_oldest(self):
        """Oldest messages are dropped first to fit within budget."""
        # budget = 400 // 4 = 100 tokens
        # Each message = 200 chars = 50 tokens -> 3 msgs = 150 tokens > 100
        msgs = [
            _make_msg("user", "a" * 200),
            _make_msg("assistant", "b" * 200),
            _make_msg("user", "c" * 200),
        ]
        result = trim_messages_to_token_limit(msgs)
        assert len(result) < 3
        # Last message is always preserved
        assert result[-1]["content"] == "c" * 200

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", 400)
    def test_last_message_always_kept(self):
        """Even if a single message exceeds the budget, it is kept."""
        msgs = [_make_msg("user", "x" * 800)]
        result = trim_messages_to_token_limit(msgs)
        assert len(result) == 1
        assert result[0]["content"] == "x" * 800

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", 400)
    def test_trims_from_front_not_back(self):
        """Verify older messages (front) are removed, newer ones (back) stay."""
        # budget = 100 tokens; each msg = 50 tokens
        msgs = [
            _make_msg("user", "first-" + "a" * 194),
            _make_msg("assistant", "second-" + "b" * 193),
            _make_msg("user", "third-" + "c" * 194),
        ]
        result = trim_messages_to_token_limit(msgs)
        assert result[-1]["content"].startswith("third-")
        assert not any(m["content"].startswith("first-") for m in result)

    def test_empty_messages(self):
        assert trim_messages_to_token_limit([]) == []

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", MAX_CONTENT_LENGTH)
    def test_default_limit_is_derived_from_max_content_length(self):
        """Token budget should be MAX_CONTENT_LENGTH // CHARS_PER_TOKEN."""
        expected_token_budget = MAX_CONTENT_LENGTH // CHARS_PER_TOKEN
        # Create messages just under the budget -> no trimming
        msg_chars = (expected_token_budget - 1) * CHARS_PER_TOKEN
        msgs = [_make_msg("user", "a" * msg_chars)]
        result = trim_messages_to_token_limit(msgs)
        assert len(result) == 1

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", MAX_CONTENT_LENGTH)
    def test_result_fits_within_token_budget(self):
        """After trimming, estimated tokens must be <= budget."""
        token_budget = MAX_CONTENT_LENGTH // CHARS_PER_TOKEN
        # 20 messages each ~2500 tokens = 50000 tokens, well over 25000 budget
        msgs = [_make_msg("user" if i % 2 == 0 else "assistant", "x" * 10000)
                for i in range(20)]
        result = trim_messages_to_token_limit(msgs)
        result_tokens = sum(_estimate_tokens(m["content"]) for m in result)
        assert result_tokens <= token_budget

    @patch("chatbot.agent_utils.Config.MAX_CONTENT_LENGTH", 400)
    def test_does_not_mutate_original(self):
        """The original message list must not be modified."""
        msgs = [
            _make_msg("user", "a" * 200),
            _make_msg("assistant", "b" * 200),
            _make_msg("user", "c" * 200),
        ]
        original_len = len(msgs)
        trim_messages_to_token_limit(msgs)
        assert len(msgs) == original_len
