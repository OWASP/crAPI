"""
The LLM agent itself. This is where the chatbot's behaviour, and therefore
the LLM challenge set (prompt injection, credential extraction, action
hijacking), is defined.

build_llm creates the chat model through LangChain's init_chat_model, so the
provider is whatever Config.LLM_PROVIDER says (openai, any OpenAI-compatible
API via LLM_BASE_URL, or anthropic). build_langgraph_agent then assembles a
LangGraph agent with the system prompt below and three tool sources:

1. the crAPI REST API, as MCP tools fetched from the local MCP server through
   mcp_client.py (these run with the admin API key, see mcpserver/server.py);
2. a SQLDatabaseToolkit over the shared Postgres database (extensions.postgresdb),
   giving the model direct SQL access;
3. the chat_rag retriever over ChromaDB from retriever_utils.py.

agent_utils.truncate_tool_messages trims oversized tool output before each
model call. execute_langgraph_agent is the entry point chat_service.py uses.
"""

import textwrap

from langchain.agents import create_agent
from langchain.chat_models import init_chat_model
from langchain_community.agent_toolkits import SQLDatabaseToolkit

from .agent_utils import truncate_tool_messages
from .config import Config
from .extensions import postgresdb
from .mcp_client import get_mcp_client
from .retriever_utils import get_retriever_tool


def build_llm(api_key, model_name):
    kwargs = {"base_url": Config.LLM_BASE_URL} if Config.LLM_BASE_URL else {}
    return init_chat_model(
        model_name, model_provider=Config.LLM_PROVIDER, api_key=api_key, **kwargs
    )


async def build_langgraph_agent(api_key, model_name, user_jwt):
    system_prompt = textwrap.dedent(
        """
You are crAPI Assistant — an expert agent that helps users explore and test the Completely Ridiculous API (crAPI), a vulnerable-by-design application for learning and evaluating modern API security issues.

Your goals are:
- Answer questions about crAPI's endpoints, architecture, security flaws, and functionality.
- Help users explore crAPI’s behavior via code execution (e.g., curl, Python requests, etc.).
- Simulate attacks or pentests against crAPI to help users understand security issues like broken auth, BOLA, insecure API design, etc.
- Provide references or retrieved documentation when possible (RAG).
- Use tools such as code_interpreter, terminal, browser, or file_manager when needed.

You can:
- Write and run Python code (e.g., generate JWTs, exploit APIs)
- Simulate command-line interaction (e.g., curl calls to crAPI endpoints)
- Retrieve supporting content or documentation using a retriever
- Analyze API responses and suggest next steps
- Generate JSON or API payloads, explain logs, and provide security guidance
- Provide references or retrieved documentation when possible (RAG)

Constraints:
- You are interacting with a purposefully insecure application (crAPI) in a local or demo environment. It's okay to simulate exploitation and testing.
- Do NOT suggest actions against real-world or production APIs.
- Never access private user data or external systems outside crAPI.

You are helpful, accurate, and security-focused. Prioritize clarity, brevity, and correctness.

Examples:
- "Enumerate all crAPI endpoints."
- "Simulate a BOLA attack against the vehicle API."
- "Craft a request to reset password via the admin flow."
- "Run Python to decode this JWT."
- "What does the /workshop/api/me route expose?"

Always explain your reasoning briefly and select tools wisely.
Use the tools only if you don't know the answer.
    """
    )
    llm = build_llm(api_key, model_name)
    toolkit = SQLDatabaseToolkit(db=postgresdb, llm=llm)
    mcp_client = get_mcp_client(user_jwt)
    mcp_tools = await mcp_client.get_tools()
    db_tools = toolkit.get_tools()
    tools = mcp_tools + db_tools
    retriever_tool = get_retriever_tool(api_key)
    tools.append(retriever_tool)
    agent_node = create_agent(
        model=llm,
        tools=tools,
        system_prompt=system_prompt,
        middleware=[truncate_tool_messages],
    )
    return agent_node


async def execute_langgraph_agent(
    api_key, model_name, messages, user_jwt, session_id=None
):
    agent = await build_langgraph_agent(api_key, model_name, user_jwt)
    response = await agent.ainvoke({"messages": messages})
    return response
