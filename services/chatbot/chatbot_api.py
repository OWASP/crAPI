
from flask import Flask
from flask import request, jsonify
from langchain.embeddings import OpenAIEmbeddings
from langchain.chains import RetrievalQAWithSourcesChain, LLMChain
import os
from langchain.memory import  ConversationBufferWindowMemory
from langchain.vectorstores import Chroma
from langchain_openai import OpenAI
from langchain.document_loaders import DirectoryLoader
from langchain.memory import  ConversationBufferWindowMemory
from langchain.text_splitter import CharacterTextSplitter
from langchain.prompts import PromptTemplate
from langchain import PromptTemplate
from langchain_community.document_loaders import UnstructuredMarkdownLoader

app = Flask(__name__)

    
retriever = None
persist_directory = os.environ.get('PERSIST_DIRECTORY')
vulnerable_app_qa = None
target_source_chunks = int(os.environ.get('TARGET_SOURCE_CHUNKS',4))

def document_loader():
    loader = DirectoryLoader('../../docs/retrieval_docs', glob="./*.md", loader_cls=UnstructuredMarkdownLoader)
    documents = loader.load()
    text_splitter = CharacterTextSplitter(chunk_size=1000, chunk_overlap=100)
    texts = text_splitter.split_documents(documents)
    embeddings = get_embeddings()
    os.system("rm -rf ./db")
    db = Chroma.from_documents(texts, embeddings, persist_directory="./db")
    db.persist()
    retriever = db.as_retriever(search_kwargs={"k": target_source_chunks})
    return retriever

def get_embeddings():
    return OpenAIEmbeddings()


def get_llm():
    llm = OpenAI(temperature=0.6, model_name="gpt-3.5-turbo-instruct")
    return llm

def get_qa_chain(llm, retriever):
    PROMPT = None
    prompt_template="""
            You are a helpful AI Assistant.
            {summaries}              
            Previous Conversations till now: {chat_history}
            Reply to this Human question/instruction: {question}.
            Chatbot: """
    PROMPT = PromptTemplate(template=prompt_template, input_variables=["question","chat_history"])
    chain_type_kwargs = {"prompt": PROMPT}
    qa = RetrievalQAWithSourcesChain.from_chain_type(llm=llm, chain_type="stuff", retriever=retriever,chain_type_kwargs=chain_type_kwargs, memory=ConversationBufferWindowMemory(memory_key="chat_history", input_key="question", output_key="answer",k=6))
    #qa = LLMChain(prompt=PROMPT, llm=llm, retriever= retriever , memory=ConversationBufferWindowMemory(memory_key="chat_history", input_key="question", k=6), verbose = False)
    return qa



def qa_app(qa, query):
    result = qa(query)
    return result["answer"]

@app.route("/genai/init", methods=["POST"])
def init_bot():
    try:
        if "openai_api_key" in request.json:
            os.environ["OPENAI_API_KEY"] = request.json["openai_api_key"]
            global vulnerable_app_qa, retriever
            retriever = document_loader()
            llm = get_llm()
            vulnerable_app_qa = get_qa_chain(llm, retriever)
        else:
            raise Exception("Open AI API key not provided")
    except Exception as e:
        print("Error initializing bot ", e)
        return jsonify({'message': 'Not able to initialize model '+ str(e)}), 405
    return jsonify({'message': 'Model Initialized'}), 200

@app.route("/genai/ask", methods=["POST"])
def ask_bot():
    question = request.json["question"]
    global vulnerable_app_qa
    answer = qa_app(vulnerable_app_qa, question)
    print("###########################################")
    print("Test Attacker Question: " + str(question))
    print("Vulnerability App Answer: " + str(answer))
    print("###########################################")
    return jsonify({'answer': answer}), 200


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5002)