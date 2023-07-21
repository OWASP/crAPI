FROM golang:1.17-buster as builder

WORKDIR /app

COPY go.* ./
RUN go mod download

COPY . ./

# Build the binary.
RUN go build -v -o server

RUN wget https://github.com/minio/certgen/releases/latest/download/certgen-linux-amd64 -O /tmp/certgen \
    && chmod +x /tmp/certgen

RUN /tmp/certgen -host "127.0.0.1,gateway-service,api.mypremiumdealership.com,mypremiumdealership.com"
RUN ls -la

FROM debian:buster-slim
WORKDIR /app

RUN set -x && apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
    ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Copy the binary to the production image from the builder stage.
COPY --from=builder /app/server /app/server
COPY --from=builder /app/public.crt /app/server.crt
COPY --from=builder /app/private.key /app/server.key

EXPOSE 443

CMD ["/app/server"]

