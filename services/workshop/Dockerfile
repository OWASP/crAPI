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


FROM python:3.8-alpine3.15 as build
# Not using alpine based on suggestion
# https://pythonspeed.com/articles/alpine-docker-python/

# Python 
ENV PYTHONUNBUFFERED=1
RUN apk add --update --no-cache  gettext curl py3-pip postgresql-dev
RUN apk add --update --no-cache --virtual .build-deps \
        python3-dev openssl-dev \
        libffi-dev gcc py3-pip \
        python3-dev \
        libressl-dev \
        musl-dev \
        libffi-dev
    
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1
RUN mkdir /app
WORKDIR /app
COPY ./ /app
RUN pip install wheel && pip wheel . --wheel-dir /app/wheels

FROM python:3.8-alpine3.15
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait
COPY --from=build /app /app
WORKDIR /app
RUN apk update && apk add --no-cache postgresql-libs curl
RUN pip install --no-index --find-links=/app/wheels -r requirements.txt

EXPOSE 8000

CMD /wait && /app/runner.sh
