"""
Runtime configuration for the chatbot API process, read from environment
variables (docker-compose plus the optional deploy/docker/llm.env.local).

The LLM settings are the ones that matter for running the lab: LLM_PROVIDER,
DEFAULT_MODEL, LLM_BASE_URL, the API key (CHATBOT_LLM_API_KEY, or upstream's
CHATBOT_OPENAI_API_KEY) and EMBEDDINGS_PROVIDER. Database locations come from
dbconnections.py. Consumed by langgraph_agent.py (model), retriever_utils.py
(embeddings) and session_service.py (default key and model).
"""

import os

from dotenv import load_dotenv

from .dbconnections import CHROMA_HOST, CHROMA_PORT, MONGO_CONNECTION_URI

load_dotenv()


class Config:
    SECRET_KEY = os.getenv("SECRET_KEY", "super-secret")
    MONGO_URI = MONGO_CONNECTION_URI
    DEFAULT_MODEL_NAME = os.getenv("DEFAULT_MODEL", "gpt-4o-mini")
    # LangChain model_provider: "openai" (also any OpenAI-compatible API such as xAI,
    # Groq, OpenRouter or Ollama, via LLM_BASE_URL) or "anthropic".
    LLM_PROVIDER = os.getenv("LLM_PROVIDER", "openai")
    # Overrides the provider's default endpoint, e.g. https://api.x.ai/v1. Empty keeps it.
    LLM_BASE_URL = os.getenv("LLM_BASE_URL") or None
    # CHATBOT_OPENAI_API_KEY is upstream's name for the same key and still works.
    LLM_API_KEY = os.getenv("CHATBOT_LLM_API_KEY") or os.getenv(
        "CHATBOT_OPENAI_API_KEY"
    )
    # "openai" (text-embedding-3-large) or "local" (Chroma's bundled all-MiniLM-L6-v2).
    # Only OpenAI itself serves text-embedding-3-large, so every other setup defaults to local.
    EMBEDDINGS_PROVIDER = os.getenv("EMBEDDINGS_PROVIDER") or (
        "openai" if LLM_PROVIDER == "openai" and not LLM_BASE_URL else "local"
    )
    MAX_CONTENT_LENGTH = int(os.getenv("MAX_CONTENT_LENGTH", 50000))
    CHROMA_HOST = CHROMA_HOST
    CHROMA_PORT = CHROMA_PORT
