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
from langchain_core.prompts import PromptTemplate
from langchain import PromptTemplate
from langchain_community.document_loaders import UnstructuredMarkdownLoader
import logging

app = Flask(__name__)

retriever = None
persist_directory = os.environ.get("PERSIST_DIRECTORY")
vulnerable_app_qa = None
target_source_chunks = int(os.environ.get("TARGET_SOURCE_CHUNKS", 4))
loaded_model_lock = threading.Lock()
loaded_model = threading.Event()
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


def document_loader():
    try:
        load_dir = "retrieval"
        logger.debug("Loading documents from %s", load_dir)
        loader = DirectoryLoader(
            load_dir,
            exclude=["**/*.png", "**/images/**", "**/images/*", "**/*.pdf"],
            recursive=True,
            loader_cls=UnstructuredMarkdownLoader,
        )
        documents = loader.load()
        logger.debug("Loaded %s documents", len(documents))
        text_splitter = CharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
        texts = text_splitter.split_documents(documents)
        embeddings = get_embeddings()
        os.system("rm -rf ./db")
        db = Chroma.from_documents(texts, embeddings, persist_directory="./db")
        db.persist()
        retriever = db.as_retriever(search_kwargs={"k": target_source_chunks})
        return retriever
    except Exception as e:
        logger.error("Error loading documents %s", e, exc_info=True)
        raise e


def get_embeddings():
    return OpenAIEmbeddings()


def get_llm():
    llm = OpenAI(temperature=0.6, model_name="gpt-3.5-turbo-instruct")
    return llm


def get_qa_chain(llm, retriever):
    PROMPT = None
    prompt_template = """
            You are a helpful AI Assistant.
            {summaries}
            Previous Conversations till now: {chat_history}
            Reply to this Human question/instruction: {question}.
            Chatbot: """
    PROMPT = PromptTemplate(
        template=prompt_template, input_variables=["question", "chat_history"]
    )
    chain_type_kwargs = {"prompt": PROMPT}
    qa = RetrievalQAWithSourcesChain.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=retriever,
        chain_type_kwargs=chain_type_kwargs,
        memory=ConversationBufferWindowMemory(
            memory_key="chat_history", input_key="question", output_key="answer", k=6
        ),
    )
    # qa = LLMChain(prompt=PROMPT, llm=llm, retriever= retriever , memory=ConversationBufferWindowMemory(memory_key="chat_history", input_key="question", k=6), verbose = False)
    return qa


def qa_app(qa, query):
    result = qa(query)
    return result["answer"]


@app.route("/chatbot/genai/init", methods=["POST"])
def init_bot():
    try:
        with loaded_model_lock:
            if "openai_api_key" in request.json:
                logger.debug("Initializing bot %s", request.json["openai_api_key"])
                os.environ["OPENAI_API_KEY"] = request.json["openai_api_key"]
                global vulnerable_app_qa, retriever
                retriever = document_loader()
                llm = get_llm()
                vulnerable_app_qa = get_qa_chain(llm, retriever)
                loaded_model.set()
                return jsonify({"message": "Model Initialized"}), 200
            else:
                return jsonify({"message": "openai_api_key not provided"}, 400)
    except Exception as e:
        logger.error("Error initializing bot ", e)
        logger.debug("Error initializing bot ", e, exc_info=True)
        return jsonify({"message": "Not able to initialize model " + str(e)}), 400


@app.route("/chatbot/genai/state", methods=["GET"])
def state_bot():
    try:
        if loaded_model.is_set():
            return jsonify({"initialized": "true", "message": "Model already loaded"})
    except Exception as e:
        logger.error("Error checking state ", e)
        return jsonify({"message": "Error checking state " + str(e)}), 200
    return (
        jsonify({"initialized": "false", "message": "Model needs to be initialized"}),
        200,
    )


@app.route("/chatbot/genai/ask", methods=["POST"])
def ask_bot():
    question = request.json["question"]
    global vulnerable_app_qa
    answer = qa_app(vulnerable_app_qa, question)
    logger.info("###########################################")
    logger.info("Test Attacker Question: %s", question)
    logger.info("Vulnerability App Answer: %s", answer)
    logger.info("###########################################")
    return jsonify({"answer": answer}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002, debug=True)
