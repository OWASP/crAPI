"""
Quart application object for the chatbot API (the uvicorn process started by
entrypoint.sh).

Mounts chat_api.chat_bp under /chatbot/genai, so the full paths are
/chatbot/genai/{init,model,ask,state,history,reset,health}, which is what
crapi-web's /chatbot/ location proxies and what the web UI's
components/bot/Bot.tsx calls. Also enables CORS for any origin and loads
Config. Importing this module imports extensions, which opens the Mongo and
Postgres connections.
"""

from quart import Blueprint, Quart, jsonify
from quart_cors import cors

from .chat_api import chat_bp
from .config import Config
from .extensions import init_mongo

root_bp = Blueprint("root", __name__, url_prefix="/chatbot")
session_api_key_map = {}
root_bp.register_blueprint(chat_bp)


@root_bp.route("/", methods=["GET"])
async def index():
    return jsonify({"message": "Hello from chatbot!"}), 200


app = Quart(__name__)
app.config.from_object(Config)
app = cors(app, allow_origin="*")
app.register_blueprint(root_bp)

# Initialize MongoDB
init_mongo()

if __name__ == "__main__":
    app.run(debug=True, port=5555)
