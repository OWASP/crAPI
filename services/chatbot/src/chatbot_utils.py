import hashlib
from flask import Flask
from flask import request, jsonify
import threading
from langchain_openai import OpenAIEmbeddings
from langchain.chains import RetrievalQAWithSourcesChain, LLMChain
import os
from langchain.memory import ConversationBufferWindowMemory
from langchain_community.vectorstores import Chroma
from langchain_openai import OpenAI
from langchain_community.document_loaders import DirectoryLoader
from langchain.memory import ConversationBufferWindowMemory
from langchain.text_splitter import CharacterTextSplitter
from langchain_core.prompts import PromptTemplate, ChatPromptTemplate
from langchain_community.document_loaders import UnstructuredMarkdownLoader
import logging
from langchain.schema import HumanMessage, SystemMessage, AIMessage
from langchain_core.prompts.chat import (
    SystemMessagePromptTemplate,
    HumanMessagePromptTemplate,
)
import logging
from langchain_community.chat_message_histories import MongoDBChatMessageHistory
from db import MONGO_CONNECTION_URI, MONGO_DB_NAME

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

TARGET_SOURCE_CHUNKS = int(os.environ.get("TARGET_SOURCE_CHUNKS", 4))


def get_embeddings(openai_api_key):
    return OpenAIEmbeddings(openai_api_key=openai_api_key)


def document_loader(openai_api_key, logger_p=None):
    logger_l = logger_p or logger
    try:
        key_hash = hashlib.md5(openai_api_key.encode()).hexdigest()
        load_dir = "retrieval"
        logger_l.info("Loading documents from %s", load_dir)
        loader = DirectoryLoader(
            load_dir,
            exclude=["**/*.png", "**/images/**", "**/images/*", "**/*.pdf"],
            recursive=True,
            loader_cls=UnstructuredMarkdownLoader,
        )
        documents = loader.load()
        logger_l.info("Loaded %s documents in db", len(documents))
        text_splitter = CharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        texts = text_splitter.split_documents(documents)
        embeddings = get_embeddings(openai_api_key)
        db_path = "./db%s" % key_hash
        db = Chroma.from_documents(texts, embeddings, persist_directory=db_path)
        db.persist()
        retriever = db.as_retriever(search_kwargs={"k": TARGET_SOURCE_CHUNKS})
        logger_l.info("Retriever ready")
        return retriever
    except Exception as e:
        logger_l.error("Error loading documents %s", e, exc_info=True)
        raise e
