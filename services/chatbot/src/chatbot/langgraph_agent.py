import logging
import textwrap

from langchain.agents import create_agent
from langchain_anthropic import ChatAnthropic
from langchain_aws import ChatBedrock
from langchain_cohere import ChatCohere
from langchain_community.agent_toolkits import SQLDatabaseToolkit
from langchain_google_vertexai import ChatVertexAI
from langchain_groq import ChatGroq
from langchain_mistralai import ChatMistralAI
from langchain_openai import AzureChatOpenAI, ChatOpenAI

from .agent_utils import trim_messages_to_token_limit, truncate_tool_messages
from .aws_credentials import get_bedrock_credentials_kwargs
from .config import Config
from .extensions import postgresdb
from .mcp_client import get_mcp_client
from .retriever_utils import get_retriever_tool

logger = logging.getLogger(__name__)

DEFAULT_MODELS = {
    "openai": "gpt-4o-mini",
    "anthropic": "claude-sonnet-4-20250514",
    "azure_openai": "",  # uses deployment name
    "bedrock": "anthropic.claude-3-sonnet-20240229-v1:0",
    "vertex": "gemini-1.5-flash",
    "groq": "llama-3.3-70b-versatile",
    "mistral": "mistral-large-latest",
    "cohere": "command-r-plus",
}


def _get_default_model(provider: str) -> str:
    default_model = DEFAULT_MODELS.get(provider, "gpt-4o-mini")
    logger.debug(
        "Getting default model for provider - provider: %s, default_model: %s",
        provider,
        default_model,
    )
    return default_model


def _build_llm(api_key, model_name):
    provider = Config.LLM_PROVIDER
    original_model_name = model_name
    model_name = model_name or _get_default_model(provider)

    # Log AI config derivation
    logger.info(
        "=== AI CONFIG DERIVATION === provider: %s, requested_model: %s, derived_model: %s, model_source: %s",
        provider,
        original_model_name or "(none)",
        model_name,
        "user_specified" if original_model_name else "default",
    )
    logger.info(
        "AI Config Details - LLM_PROVIDER: %s, LLM_MODEL_NAME (env): %s, EMBEDDINGS_MODEL: %s",
        Config.LLM_PROVIDER,
        Config.LLM_MODEL_NAME or "(not set)",
        Config.EMBEDDINGS_MODEL or "(not set)",
    )

    if provider == "openai":
        kwargs = {"api_key": api_key, "model": model_name}
        if Config.OPENAI_BASE_URL:
            kwargs["base_url"] = Config.OPENAI_BASE_URL
            logger.info(
                "OpenAI Config - model: %s, base_url: %s, has_api_key: %s",
                model_name,
                Config.OPENAI_BASE_URL,
                bool(api_key),
            )
        else:
            logger.info(
                "OpenAI Config - model: %s, base_url: (default), has_api_key: %s",
                model_name,
                bool(api_key),
            )
        return ChatOpenAI(**kwargs)
    if provider == "azure_openai":
        deployment = Config.AZURE_OPENAI_CHAT_DEPLOYMENT or model_name
        kwargs = {
            "azure_endpoint": Config.AZURE_OPENAI_ENDPOINT,
            "api_version": Config.AZURE_OPENAI_API_VERSION,
            "azure_deployment": deployment,
        }
        if Config.AZURE_AD_TOKEN:
            kwargs["azure_ad_token"] = Config.AZURE_AD_TOKEN
            auth_method = "azure_ad_token"
        else:
            kwargs["api_key"] = Config.AZURE_OPENAI_API_KEY
            auth_method = "api_key"
        logger.info(
            "Azure OpenAI Config - deployment: %s, endpoint: %s, api_version: %s, auth_method: %s",
            deployment,
            Config.AZURE_OPENAI_ENDPOINT,
            Config.AZURE_OPENAI_API_VERSION,
            auth_method,
        )
        return AzureChatOpenAI(**kwargs)
    if provider == "bedrock":
        import os as _os

        logger.info(
            "[BUILD_LLM] Bedrock provider - model_id: %s, assume_role_arn: %s, region: %s",
            model_name,
            Config.AWS_ASSUME_ROLE_ARN or "(not configured)",
            _os.getenv("AWS_REGION"),
        )

        try:
            bedrock_kwargs = get_bedrock_credentials_kwargs()
            logger.info(
                "[BUILD_LLM] Got Bedrock kwargs - keys: %s, has_explicit_creds: %s",
                list(bedrock_kwargs.keys()),
                bool(bedrock_kwargs.get("aws_access_key_id")),
            )
        except Exception as e:
            logger.error("[BUILD_LLM] Failed to get Bedrock credentials: %s", str(e))
            raise

        try:
            llm = ChatBedrock(model_id=model_name, **bedrock_kwargs)
            logger.info(
                "[BUILD_LLM] ChatBedrock created successfully with explicit credentials"
            )
            return llm
        except Exception as e:
            logger.error(
                "[BUILD_LLM] Failed to create ChatBedrock: %s - %s",
                type(e).__name__,
                str(e),
            )
            raise
    if provider == "vertex":
        logger.info(
            "Vertex AI Config - model: %s, project: %s, location: %s",
            model_name,
            Config.VERTEX_PROJECT or "(not set)",
            Config.VERTEX_LOCATION or "(not set)",
        )
        return ChatVertexAI(
            model_name=model_name,
            project=Config.VERTEX_PROJECT or None,
            location=Config.VERTEX_LOCATION or None,
        )
    if provider == "anthropic":
        logger.info(
            "Anthropic Config - model: %s, has_api_key: %s", model_name, bool(api_key)
        )
        return ChatAnthropic(api_key=api_key, model=model_name)
    if provider == "groq":
        logger.info(
            "Groq Config - model: %s, has_api_key: %s",
            model_name,
            bool(Config.GROQ_API_KEY),
        )
        return ChatGroq(api_key=Config.GROQ_API_KEY, model=model_name)
    if provider == "mistral":
        logger.info(
            "Mistral Config - model: %s, has_api_key: %s",
            model_name,
            bool(Config.MISTRAL_API_KEY),
        )
        return ChatMistralAI(api_key=Config.MISTRAL_API_KEY, model=model_name)
    if provider == "cohere":
        logger.info(
            "Cohere Config - model: %s, has_api_key: %s",
            model_name,
            bool(Config.COHERE_API_KEY),
        )
        return ChatCohere(api_key=Config.COHERE_API_KEY, model=model_name)
    logger.error("Unsupported LLM provider: %s", provider)
    raise ValueError(f"Unsupported provider {provider}")


async def build_langgraph_agent(api_key, model_name, user_jwt):
    logger.info(
        "Building LangGraph agent - has_api_key: %s, model_name: %s, has_user_jwt: %s",
        bool(api_key),
        model_name or "(will use default)",
        bool(user_jwt),
    )
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
    llm = _build_llm(api_key, model_name)
    logger.debug("LLM instance created successfully")

    toolkit = SQLDatabaseToolkit(db=postgresdb, llm=llm)
    logger.debug("SQL Database toolkit created")

    mcp_tools = []
    try:
        mcp_client = get_mcp_client(user_jwt)
        mcp_tools = await mcp_client.get_tools()
        logger.debug("MCP tools loaded: %d tools", len(mcp_tools))
    except Exception as e:
        logger.error("Failed to load MCP tools, continuing without them: %s", e)

    db_tools = toolkit.get_tools()
    logger.debug("Database tools loaded: %d tools", len(db_tools))

    tools = mcp_tools + db_tools
    retriever_tool = get_retriever_tool(api_key, Config.LLM_PROVIDER, model_name)
    tools.append(retriever_tool)
    logger.info(
        "Agent tools prepared - mcp_tools: %d, db_tools: %d, retriever_tool: 1, total: %d",
        len(mcp_tools),
        len(db_tools),
        len(tools),
    )

    agent_node = create_agent(
        model=llm,
        tools=tools,
        system_prompt=system_prompt,
        middleware=[truncate_tool_messages],
    )
    logger.info("LangGraph agent built successfully")
    return agent_node


async def execute_langgraph_agent(
    api_key, model_name, messages, user_jwt, session_id=None
):
    logger.info(
        "Executing LangGraph agent - session_id: %s, model_name: %s, message_count: %d",
        session_id,
        model_name or "(default)",
        len(messages),
    )
    agent = await build_langgraph_agent(api_key, model_name, user_jwt)
    messages = trim_messages_to_token_limit(messages)
    logger.debug("Invoking agent with %d messages", len(messages))
    response = await agent.ainvoke({"messages": messages})
    logger.info(
        "Agent execution completed - session_id: %s, response_message_count: %d",
        session_id,
        len(response.get("messages", [])),
    )
    return response
