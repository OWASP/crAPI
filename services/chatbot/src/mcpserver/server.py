import json
import logging
import os
import ssl
import time

import httpx
from fastmcp import FastMCP

from .tool_helpers import fix_array_responses_in_spec, OpenAPIRefResolver
from .config import Config

# Configure logging
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

BASE_URL = f"{'https' if Config.TLS_ENABLED else 'http'}://{Config.WEB_SERVICE}"
BASE_IDENTITY_URL = (
    f"{'https' if Config.TLS_ENABLED else 'http'}://{Config.IDENTITY_SERVICE}"
)
API_KEY = None


def get_api_key():
    global API_KEY
    # Try 5 times to get client auth
    MAX_ATTEMPTS = 5
    for i in range(MAX_ATTEMPTS):
        logger.info(f"Attempt {i+1} to get API key...")
        if API_KEY is None:
            login_body = {"email": Config.API_USER, "password": Config.API_PASSWORD}
            auth_url = f"{BASE_IDENTITY_URL}/identity/management/user/apikey"
            headers = {
                "Content-Type": "application/json",
            }
            _ssl_ctx = ssl.create_default_context()
            _ssl_ctx.check_hostname = False
            _ssl_ctx.verify_mode = ssl.CERT_NONE
            with httpx.Client(
                headers=headers,
                verify=_ssl_ctx,
                trust_env=False,
            ) as client:
                response = client.post(auth_url, json=login_body)
                if response.status_code != 200:
                    if i == MAX_ATTEMPTS - 1:
                        logger.error(
                            f"Failed to get API key after {i+1} attempts: {response.status_code} {response.text}"
                        )
                        raise Exception(
                            f"Failed to get API key after {i+1} attempts: {response.status_code} {response.text}"
                        )
                    logger.error(
                        f"Failed to get API key in attempt {i+1}: {response.status_code} {response.text}. Sleeping for {i} seconds..."
                    )
                    time.sleep(i)
                response_json = response.json()
                logger.info(f"Response: {response_json}")
                API_KEY = response_json.get("apiKey")
                if API_KEY:
                    logger.debug("MCP Server API Key obtained successfully.")
                return API_KEY
    return API_KEY


# Async HTTP client for API calls
def get_http_client():
    """Create and configure the HTTP client with appropriate authentication."""
    headers = {
        "Authorization": "ApiKey " + get_api_key(),
    }
    return httpx.AsyncClient(
        base_url=BASE_URL,
        headers=headers,
        verify=False,
    )


# Load your OpenAPI spec
with open(Config.OPENAPI_SPEC, "r") as f:
    openapi_spec = json.load(f)
OpenAPIRefResolver(openapi_spec).format_openapi_spec()
fix_array_responses_in_spec(openapi_spec)

# Create the MCP server
mcp = FastMCP.from_openapi(
    openapi_spec=openapi_spec, client=get_http_client(), name="My crAPI MCP Server"
)

if __name__ == "__main__":
    mcp_server_port = int(os.environ.get("MCP_SERVER_PORT", 5500))
    mcp.run(
        transport="streamable-http",
        host="0.0.0.0",
        port=mcp_server_port,
    )
