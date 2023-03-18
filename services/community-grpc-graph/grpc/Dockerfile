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


# GoLang Build
FROM golang:alpine AS builder
ENV GO111MODULE=on \
    CGO_ENABLED=0
WORKDIR /build
COPY ./go.mod .
COPY ./graph ./graph
COPY ./grpc ./grpc
RUN go mod tidy
RUN go mod download
RUN cp /build/grpc/main/main.go .
RUN go mod vendor
RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
RUN apt-get install protobuf-compiler
RUN protoc -I /build/grpc/v1/ community.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc -I /build/grpc/v1/ community_requests.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc -I /build/grpc/v1/ community_responses.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc -I /build/grpc/v1/ community_service.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN ls -al
RUN go build -o main .
WORKDIR /dist
RUN cp /build/main .

# Main Image
FROM alpine:3

# go
ENV MONGO_DB_HOST=localhost
ENV MONGO_DB_PASSWORD=root
ENV MONGO_DB_PORT=27017
ENV MONGO_DB_USER=root
ENV MONGO_DB_NAME=crapi
RUN mkdir /app
RUN apk update && apk add --no-cache curl
COPY --from=builder /dist/main /app/main
RUN ls -al /app

ARG SERVER_PORT 
EXPOSE ${SERVER_PORT}
CMD /app/main
