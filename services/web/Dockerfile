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


# Node build
FROM node:12 as build
RUN mkdir /app
WORKDIR /app
COPY ./package.json /app/package.json
COPY ./package-lock.json /app/package-lock.json
RUN npm install --silent
COPY ./src /app/src
COPY ./public /app/public
RUN cp /app/src/config.js.template /app/src/config.js
RUN NODE_OPTIONS=--max_old_space_size=768 npm run build
RUN ls /app/build

# Main Image
FROM openresty/openresty:1.17.8.2-alpine-fat
RUN apk add gettext curl gcc make bash diffutils findutils grep coreutils sed 
ENV PATH /usr/local/openresty/openssl/bin:$PATH
RUN /usr/local/openresty/luajit/bin/luarocks install lua-resty-auto-ssl
RUN mkdir -p  /etc/resty-auto-ssl/library/file
RUN chown -R nobody /etc/resty-auto-ssl
ENV OPENSSL_CONF /etc/ssl/openssl.cnf
RUN openssl req -new -newkey rsa:2048 -days 3650 -nodes -x509 \
   -subj '/CN=sni-support-required-for-valid-ssl' \
    -keyout /etc/ssl/resty-auto-ssl-fallback.key \
   -out /etc/ssl/resty-auto-ssl-fallback.crt


# React
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /app/build /usr/share/nginx/html
COPY ./nginx-wrapper.sh /etc/nginx/nginx-wrapper.sh
COPY ./crapi.conf.template /etc/nginx/conf.d/crapi.conf.template

COPY ./nginx.conf /usr/local/openresty/nginx/conf/nginx.conf
# RUN echo "daemon off;" >> /usr/local/openresty/nginx/conf/nginx.conf
EXPOSE 80
EXPOSE 443

CMD /etc/nginx/nginx-wrapper.sh