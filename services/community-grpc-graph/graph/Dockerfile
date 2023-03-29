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
RUN go mod tidy
RUN go mod download
COPY ./graph ./graph
COPY ./grpc ./grpc
COPY ./server.go ./main.go
RUN go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
RUN go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
RUN go get github.com/favadi/protoc-go-inject-tag
RUN go install github.com/favadi/protoc-go-inject-tag
RUN apk add --no-cache protobuf
RUN apk add protobuf-dev
RUN protoc -I /build/grpc/v1/ community.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc -I /build/grpc/v1/ community_requests.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc -I /build/grpc/v1/ community_responses.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc -I /build/grpc/v1/ community_service.proto --go_out=/build/grpc/ --go-grpc_out=/build/grpc
RUN protoc-go-inject-tag -input=/build/grpc/proto/*.pb.go
RUN go mod tidy
RUN go mod vendor
RUN ls -al
RUN go build -o main .
WORKDIR /dist
RUN cp /build/main .

# Main Image
FROM alpine:3

RUN mkdir /app
RUN apk update && apk add --no-cache curl
COPY --from=builder /dist/main /app/main
RUN ls -al /app

ARG SERVER_PORT 
EXPOSE ${SERVER_PORT}
CMD /app/main
