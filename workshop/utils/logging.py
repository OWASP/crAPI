import logging


def log_error(url, params, status_code, message):
    """
    :param url: The URL of the request API.
    :param params: Parameters of the request if any
    :param status_code: The return status code of the API
    :param message: The message of the error.
    :return:
    """
    logging.getLogger().error(f"{url} - {params} - {status_code} -{message}")
