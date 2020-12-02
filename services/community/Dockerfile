# GoLang Build
FROM golang:alpine AS builder
ENV GO111MODULE=on \
    CGO_ENABLED=0 \
    GOOS=linux \
    GOARCH=amd64
WORKDIR /build
COPY ./go.mod .
COPY ./go.sum .
RUN go mod download
COPY ./main.go ./main.go
COPY ./api ./api
RUN go mod vendor
RUN ls -al
RUN go build -o main .
WORKDIR /dist
RUN cp /build/main .

# Main Image
FROM alpine:3

# go
RUN mkdir /app
RUN apk update && apk add --no-cache curl
COPY --from=builder /dist/main /app/main
RUN ls -al /app
EXPOSE 8087

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

CMD /wait && /app/main
