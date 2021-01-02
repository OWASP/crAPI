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


"""
contains all methods which returns sample data
which can be used during testing
"""


def get_sample_mechanic_data():
    """
    gives mechanic data which can be used for testing
    :return: sample mechanic object
    """
    return {
        "name": "MechRaju",
        "email": "mechraju@crapi.com",
        "mechanic_code": "TRAC_MEC_3",
        "number": "9123456708",
        "password": "admin",
    }
