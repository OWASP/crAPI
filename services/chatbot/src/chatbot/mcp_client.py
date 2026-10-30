"""
Client side of the chatbot's MCP link.

get_mcp_client returns a LangChain MultiServerMCPClient pointed at the MCP
server that entrypoint.sh starts in the same container
(http://localhost:5500/mcp/, streamable HTTP). langgraph_agent.py calls
get_tools() on it to turn every crAPI operation into an agent tool. The
chatting user's JWT is attached as a Bearer header when present, but the
server's outbound calls to crAPI use its own admin API key (see
mcpserver/server.py), not this header.
"""

from langchain_mcp_adapters.client import MultiServerMCPClient


def get_mcp_client(user_jwt: str | None) -> MultiServerMCPClient:
    headers = {}
    if user_jwt:
        headers["Authorization"] = f"Bearer {user_jwt}"

    return MultiServerMCPClient(
        {
            "crapi": {
                "transport": "streamable_http",
                "url": "http://localhost:5500/mcp/",
                "headers": headers,
            }
        }
    )
