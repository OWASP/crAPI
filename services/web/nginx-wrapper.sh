#!/bin/sh

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


envsubst '${GO_SERVICE} ${JAVA_SERVICE} ${PYTHON_SERVICE}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
openresty
exec "$@"