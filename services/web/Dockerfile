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
FROM openresty/openresty:1.17.8.2-alpine
RUN apk add gettext curl
# React
RUN rm /etc/nginx/conf.d/default.conf
COPY --from=build /app/build /usr/share/nginx/html
COPY ./nginx-wrapper.sh /etc/nginx/nginx-wrapper.sh
COPY ./nginx.conf.template /etc/nginx/conf.d/default.conf.template

RUN echo "daemon off;" >> /usr/local/openresty/nginx/conf/nginx.conf
EXPOSE 80


ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

CMD /wait && /etc/nginx/nginx-wrapper.sh
