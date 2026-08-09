import logging

import httpx

from .config import Config

logger = logging.getLogger(__name__)


async def validate_user_jwt(token: str) -> bool:
    """Validate a caller token with the identity service, failing closed."""
    scheme = "https" if Config.TLS_ENABLED else "http"
    verify_url = f"{scheme}://{Config.IDENTITY_SERVICE}/identity/api/auth/verify"

    try:
        async with httpx.AsyncClient(verify=Config.TLS_ENABLED) as client:
            response = await client.post(
                verify_url,
                json={"token": token},
                headers={"Content-Type": "application/json"},
                timeout=10.0,
            )
    except httpx.RequestError as exc:
        logger.warning("Identity service token validation failed: %s", exc)
        return False

    return response.status_code == 200
