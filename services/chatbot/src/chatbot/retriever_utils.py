"""
RAG layer: ChromaDB vector store over past chat messages.

get_chroma_vectorstore connects to the chromadb container (CHROMA_HOST/PORT)
and picks the embedding function from Config.EMBEDDINGS_PROVIDER: OpenAI's
text-embedding-3-large in the "chats" collection, or Chroma's bundled ONNX
MiniLM (LocalEmbeddings, no API needed) in "chats_local". The two collections
are separate because vector sizes differ.

add_to_chroma_collection is called by chat_service.py after every exchange to
index the user message and the reply, tagged with the session id and role.
get_retriever_tool wraps a similarity search as the ``chat_rag`` tool that
langgraph_agent.py gives the model for "what did I ask earlier" questions.
Documents are not filtered by session, so one session's history is
retrievable from another.
"""

import chromadb
from chromadb.utils.embedding_functions import DefaultEmbeddingFunction
from langchain_chroma import Chroma as ChromaClient
from langchain_core.documents import Document
from langchain_core.embeddings import Embeddings
from langchain_core.tools import StructuredTool
from langchain_openai import OpenAIEmbeddings

from .config import Config


def get_chroma_client():
    chroma_client = chromadb.HttpClient(
        host=Config.CHROMA_HOST,
        port=Config.CHROMA_PORT,
        ssl=False,
        headers=None,
    )
    return chroma_client


class LocalEmbeddings(Embeddings):
    """Chroma's bundled ONNX all-MiniLM-L6-v2, so no embeddings API is needed."""

    def __init__(self):
        self._fn = DefaultEmbeddingFunction()

    def embed_documents(self, texts):
        return [[float(x) for x in vector] for vector in self._fn(texts)]

    def embed_query(self, text):
        return self.embed_documents([text])[0]


def get_embedding_function(api_key):
    if Config.EMBEDDINGS_PROVIDER == "local":
        return LocalEmbeddings()
    return OpenAIEmbeddings(
        openai_api_key=api_key,
        model="text-embedding-3-large",
    )


def get_chroma_vectorstore(api_key):
    chroma_client = get_chroma_client()
    vectorstore = ChromaClient(
        client=chroma_client,
        # Vector sizes differ per embedding model, so each provider gets its own collection.
        collection_name=(
            "chats" if Config.EMBEDDINGS_PROVIDER == "openai" else "chats_local"
        ),
        embedding_function=get_embedding_function(api_key),
        create_collection_if_not_exists=True,
    )
    return vectorstore


def add_to_chroma_collection(
    api_key, session_id, new_messages: list[dict[str, str]]
) -> list:
    vectorstore = get_chroma_vectorstore(api_key)
    print("new_messages", new_messages)
    # new_messages = [{'user': 'hi'}, {'assistant': 'Hello! How can I assist you today?'}]
    documents = []
    for message in new_messages:
        for role, content in message.items():
            documents.append(
                Document(
                    page_content=content,
                    metadata={"session_id": session_id, "role": role},
                )
            )
    print("documents", documents)
    res: list = vectorstore.add_documents(documents=documents)
    return res


def get_retriever_tool(api_key):
    vectorstore = get_chroma_vectorstore(api_key)
    retriever = vectorstore.as_retriever()

    def chat_rag(query: str) -> str:
        docs = retriever.invoke(query)
        return "\n\n".join(doc.page_content for doc in docs)

    return StructuredTool.from_function(
        func=chat_rag,
        name="chat_rag",
        description="""
        Use this to answer questions based on user chat history (summarized and semantically indexed).
        Use this when the user asks about prior chats, what they asked earlier, or wants a summary of past conversations.

        Use this tool when the user refers to anything mentioned before, asks for a summary of previous messages or sessions,
        or references phrases like 'what I said earlier', 'things we discussed', 'my earlier question', 'until now', 'till date', 'all my conversations' or 'previously mentioned'.
        The chat history is semantically indexed and summarized using vector search.
        """,
    )
