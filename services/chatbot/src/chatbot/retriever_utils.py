import chromadb
from langchain_chroma import Chroma as ChromaClient
from langchain_core.documents import Document
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


def get_embedding_function(api_key):
    return OpenAIEmbeddings(
        openai_api_key=api_key,
        model="text-embedding-3-large",
    )


def get_chroma_vectorstore(api_key):
    chroma_client = get_chroma_client()
    vectorstore = ChromaClient(
        client=chroma_client,
        collection_name="chats",
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
