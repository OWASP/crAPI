"""Regression tests for the chatbot authentication and tool boundary."""

import ast
import asyncio
import importlib
import sys
from pathlib import Path
from types import ModuleType


class _FakeResponse:
    def __init__(self, status_code):
        self.status_code = status_code


class _FakeAsyncClient:
    response_status = 200
    request_error = None

    def __init__(self, **kwargs):
        self.kwargs = kwargs

    async def __aenter__(self):
        return self

    async def __aexit__(self, exc_type, exc, tb):
        return False

    async def post(self, *args, **kwargs):
        if self.request_error:
            raise self.request_error
        return _FakeResponse(self.response_status)


httpx_stub = ModuleType("httpx")
httpx_stub.AsyncClient = _FakeAsyncClient
httpx_stub.RequestError = type("RequestError", (Exception,), {})
sys.modules.setdefault("httpx", httpx_stub)

dotenv_stub = ModuleType("dotenv")
dotenv_stub.load_dotenv = lambda *args, **kwargs: None
sys.modules.setdefault("dotenv", dotenv_stub)

dbconnections_stub = ModuleType("chatbot.dbconnections")
dbconnections_stub.CHROMA_HOST = "localhost"
dbconnections_stub.CHROMA_PORT = 8000
dbconnections_stub.MONGO_CONNECTION_URI = "mongodb://localhost"
sys.modules.setdefault("chatbot.dbconnections", dbconnections_stub)

validate_user_jwt = importlib.import_module("chatbot.auth").validate_user_jwt


def test_valid_identity_token_is_accepted():
    _FakeAsyncClient.response_status = 200
    _FakeAsyncClient.request_error = None
    assert asyncio.run(validate_user_jwt("valid-token")) is True


def test_invalid_identity_token_is_rejected():
    _FakeAsyncClient.response_status = 401
    _FakeAsyncClient.request_error = None
    assert asyncio.run(validate_user_jwt("invalid-token")) is False


def test_identity_service_failure_is_rejected():
    _FakeAsyncClient.request_error = httpx_stub.RequestError("unavailable")
    assert asyncio.run(validate_user_jwt("unverified-token")) is False
    _FakeAsyncClient.request_error = None


def test_chat_route_validates_token_before_processing_message():
    source = (Path(__file__).parents[1] / "chat_api.py").read_text()
    tree = ast.parse(source)
    chat = next(
        node
        for node in tree.body
        if isinstance(node, ast.AsyncFunctionDef) and node.name == "chat"
    )
    calls = {
        node.func.id: node.lineno
        for node in ast.walk(chat)
        if isinstance(node, ast.Call)
        and isinstance(node.func, ast.Name)
        and node.func.id in {"validate_user_jwt", "process_user_message"}
    }
    assert calls["validate_user_jwt"] < calls["process_user_message"]


def test_agent_has_no_privileged_tool_sources():
    source = (Path(__file__).parents[1] / "langgraph_agent.py").read_text()
    assert "SQLDatabaseToolkit" not in source
    assert "get_mcp_client" not in source
    assert "postgresdb" not in source

    tree = ast.parse(source)
    build_agent = next(
        node
        for node in tree.body
        if isinstance(node, ast.AsyncFunctionDef)
        and node.name == "build_langgraph_agent"
    )
    tool_assignments = [
        node
        for node in ast.walk(build_agent)
        if isinstance(node, ast.Assign)
        and any(
            isinstance(target, ast.Name) and target.id == "tools"
            for target in node.targets
        )
    ]
    assert len(tool_assignments) == 1
    assert isinstance(tool_assignments[0].value, ast.List)
    assert [item.id for item in tool_assignments[0].value.elts] == ["retriever_tool"]
