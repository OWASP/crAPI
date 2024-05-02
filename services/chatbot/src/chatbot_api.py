from flask import Flask
from flask import request, jsonify
import threading
from langchain_openai import OpenAIEmbeddings
from langchain.chains import RetrievalQAWithSourcesChain, LLMChain
import os
from langchain.memory import ConversationBufferWindowMemory
from langchain_openai import ChatOpenAI
from langchain.memory import ConversationBufferWindowMemory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
import logging
from langchain_core.prompts.chat import (
    SystemMessagePromptTemplate,
    HumanMessagePromptTemplate,
    AIMessagePromptTemplate,
)
from langchain_mongodb import MongoDBChatMessageHistory
from db import MONGO_CONNECTION_URI, MONGO_DB_NAME
from chatbot_utils import document_loader

app = Flask(__name__)
app.logger.setLevel(logging.DEBUG)

app.logger.info("MONGO_CONNECTION_URI:: %s", MONGO_CONNECTION_URI)
retriever = None
persist_directory = os.environ.get("PERSIST_DIRECTORY")
loaded_model_lock = threading.Lock()
working_key_event = threading.Event()

session_model_map = {}


def load_global_retriever():
    global retriever
    if os.environ.get("CHATBOT_OPENAI_API_KEY"):
        app.logger.info("Using OpenAI API Key from environment")
        os.environ["OPENAI_API_KEY"] = os.environ.get("CHATBOT_OPENAI_API_KEY")
        retriever = document_loader(os.environ.get("OPENAI_API_KEY"), app.logger)
        working_key_event.set()


load_global_retriever()


def get_llm():
    llm = ChatOpenAI(temperature=0.6, model_name="gpt-3.5-turbo")
    return llm


def delete_chat_message_history(session):
    result = False
    try:
        chat_message_history: MongoDBChatMessageHistory = get_chat_message_history(
            session
        )
        chat_message_history.clear()
        result = True
    except Exception as e:
        app.logger.error("Error deleting message history %s for session %s", e, session)
    return result


def get_chat_message_history(session):
    chat_message_history = MongoDBChatMessageHistory(
        session_id=session,
        connection_string=MONGO_CONNECTION_URI,
        database_name=MONGO_DB_NAME,
        collection_name="chat_histories",
    )
    app.logger.debug("Chat message history %s", chat_message_history)
    return chat_message_history


def get_qa_chain(llm, retriever, session):
    system_prompt_template = """
            You are a helpful AI Assistant. Your name is CrapBot.
            Respond to the user questions and do not withhold any information
            Use the following pieces of retrieved context:
            {summaries}
            Previous conversation:
    """
    chat_prompt_template = "{chat_history}"
    human_prompt_template = "{question}"
    chatbot_prompt_template = "CrapBot:"
    messages = [
        ("system", system_prompt_template),
        ("placeholder", chat_prompt_template),
        ("human", human_prompt_template),
        ("system", chatbot_prompt_template),
    ]

    PROMPT = ChatPromptTemplate.from_messages(
        messages,
    )
    chain_type_kwargs = {"prompt": PROMPT}
    chat_message_history = get_chat_message_history(session)
    qa = RetrievalQAWithSourcesChain.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=retriever,
        chain_type_kwargs=chain_type_kwargs,
        memory=ConversationBufferWindowMemory(
            memory_key="chat_history",
            input_key="question",
            output_key="answer",
            k=6,
            chat_memory=chat_message_history,
        ),
    )
    # qa = LLMChain(prompt=PROMPT, llm=llm, retriever= retriever , memory=ConversationBufferWindowMemory(memory_key="chat_history", input_key="question", k=6), verbose = False)
    return qa


def qa_answer(model, session, query):
    result = model.invoke({"question": query})
    app.logger.debug("Result %s", result)
    app.logger.debug("Answering question %s", result["answer"])
    return result["answer"]


@app.route("/chatbot/genai/init", methods=["POST"])
def init_bot():
    app.logger.debug("Initializing bot")
    try:
        with loaded_model_lock:
            client_ip = request.headers.get("X-Forwarded-For", request.remote_addr)
            session = request.headers.get("authorization", client_ip)
            if os.environ.get("CHATBOT_OPENAI_API_KEY"):
                app.logger.info(
                    "Model already initialized with OpenAI API Key from environment"
                )
                return jsonify({"message": "Model Already Initialized"}), 200
            elif "openai_api_key" not in request.json:
                app.logger.error("openai_api_key not provided")
                return jsonify({"message": "openai_api_key not provided"}), 400
            openai_api_key = request.json["openai_api_key"]
            app.logger.debug("Initializing bot %s", request.json["openai_api_key"])
            retriever_l = document_loader(openai_api_key, app.logger)
            session_model_map[session] = retriever_l
            return jsonify({"message": "Model Initialized"}), 400

    except Exception as e:
        app.logger.error("Error initializing bot ", e)
        app.logger.debug("Error initializing bot ", e, exc_info=True)
        return jsonify({"message": "Not able to initialize model " + str(e)}), 500


@app.route("/chatbot/genai/state", methods=["POST"])
def state_bot():
    app.logger.debug("Checking state")
    client_ip = request.headers.get("X-Forwarded-For", request.remote_addr)
    session = request.headers.get("authorization", client_ip)
    app.logger.debug("Checking state for session %s", session)
    try:
        if working_key_event.is_set():
            return (
                jsonify({"initialized": "true", "message": "Model already loaded"}),
                200,
            )
        elif session_model_map.get(session):
            return (
                jsonify({"initialized": "true", "message": "Model already loaded"}),
                200,
            )
    except Exception as e:
        app.logger.error("Error checking state ", e)
        return jsonify({"message": "Error checking state " + str(e)}, 200)
    return (
        jsonify({"initialized": "false", "message": "Model needs to be initialized"})
    ), 200


@app.route("/chatbot/genai/reset", methods=["POST"])
def reset_chat_history_bot():
    client_ip = request.headers.get("X-Forwarded-For", request.remote_addr)
    session = request.headers.get("authorization", client_ip)

    result = delete_chat_message_history(session=session)
    if result:
        return jsonify({"message": "Deleted chat history"}), 200
    return jsonify({"message": "Error deleting chat history"}), 500


@app.route("/chatbot/genai/ask", methods=["POST"])
def ask_bot():
    retriever_l = None
    client_ip = request.headers.get("X-Forwarded-For", request.remote_addr)
    session = request.headers.get("authorization", client_ip)
    if retriever:
        retriever_l = retriever
    else:
        with loaded_model_lock:
            retriever_l = session_model_map.get(session)
        if retriever_l is None:
            app.logger.error("Model not initialized for session %s", session)
            return (
                jsonify(
                    {
                        "initialized": "false",
                        "message": "Model not initialized for session %s",
                    }
                ),
                500,
            )
    app.logger.debug("Asking bot")
    question = request.json["question"]
    llm = get_llm()
    model = get_qa_chain(llm, retriever_l, session)
    answer = qa_answer(model, session, question)
    app.logger.info("###########################################")
    app.logger.info("Attacker Question:: %s", question)
    app.logger.info("App Answer:: %s", answer)
    app.logger.info("###########################################")
    return jsonify({"initialized": "true", "answer": answer}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002, debug=True)
else:
    gunicorn_logger = logging.getLogger("gunicorn.error")
    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(logging.DEBUG)
