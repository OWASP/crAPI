# Copyright 2020 Traceable, Inc.
#
# Licensed under the Apache License, Version 2.0 (the “License”);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an “AS IS” BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


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
