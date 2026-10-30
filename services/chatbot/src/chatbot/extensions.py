"""
Shared database handles for both chatbot processes.

``db`` is the async Mongo database ("chatgpt") that holds the ``sessions`` and
``chat_sessions`` collections used by session_service.py and chat_service.py.
``postgresdb`` is a LangChain SQLDatabase over the shared crAPI Postgres
database, handed to the agent as a SQL toolkit in langgraph_agent.py.

Both connect at import time. chatbot.app and mcpserver.tool_helpers import
this module, so if either connection string in dbconnections.py is wrong the
whole container fails to start.
"""

from langchain_community.utilities.sql_database import SQLDatabase
from motor.motor_asyncio import AsyncIOMotorClient

from .dbconnections import MONGO_CONNECTION_URI, POSTGRES_URI

mongo_client = AsyncIOMotorClient(MONGO_CONNECTION_URI)
db = mongo_client.chatgpt
postgresdb = SQLDatabase.from_uri(POSTGRES_URI)


def init_mongo():
    # Create collections if they don't exist
    # db.create_collection("chat_sessions")
    # db.create_collection("sessions")
    pass
