import os

MONGO_USER = os.environ.get("MONGO_DB_USER", "admin")
MONGO_PASSWORD = os.environ.get("MONGO_DB_PASSWORD", "crapisecretpassword")
MONGO_HOST = os.environ.get("MONGO_DB_HOST", "mongodb")
MONGO_PORT = os.environ.get("MONGO_DB_PORT", "27017")
MONGO_DB_NAME = os.environ.get("MONGO_DB_NAME", "crapi")

MONGO_CONNECTION_URI = "mongodb://%s:%s@%s:%s/?directConnection=true" % (
    MONGO_USER,
    MONGO_PASSWORD,
    MONGO_HOST,
    MONGO_PORT,
)

MONGO_CONNECTION_URI_ATLAS = "mongodb+srv://%s:%s@%s?retryWrites=true&w=majority" % (
    MONGO_USER,
    MONGO_PASSWORD,
    MONGO_HOST,
)

POSTGRES_HOST = os.environ.get("DB_HOST", "postgresdb")
POSTGRES_PORT = os.environ.get("DB_PORT", "5432")
POSTGRES_USER = os.environ.get("DB_USER", "admin")
POSTGRES_PASSWORD = os.environ.get("DB_PASSWORD", "crapisecretpassword")
POSTGRES_DB = os.environ.get("DB_NAME", "crapi")

POSTGRES_URI = "postgresql+psycopg2://%s:%s@%s:%s/%s?sslmode=disable" % (
    POSTGRES_USER,
    POSTGRES_PASSWORD,
    POSTGRES_HOST,
    POSTGRES_PORT,
    POSTGRES_DB,
)

CHROMA_HOST = os.environ.get("CHROMA_HOST", "chromadb")
CHROMA_PORT = os.environ.get("CHROMA_PORT", "8000")
