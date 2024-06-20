from base64 import b64encode


def basic_auth(username, password=""):
    token = b64encode(f"{username}:{password}".encode("utf-8")).decode("ascii")
    return f"Basic {token}"
