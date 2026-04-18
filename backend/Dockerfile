# CGO required for github.com/mattn/go-sqlite3
FROM golang:1.22-bookworm AS build

RUN apt-get update \
  && apt-get install -y --no-install-recommends gcc libc6-dev \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /src

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=1 GOOS=linux go build -o /out/zero-backend ./cmd/server

FROM debian:bookworm-slim

RUN apt-get update \
  && apt-get install -y --no-install-recommends ca-certificates \
  && rm -rf /var/lib/apt/lists/*

COPY --from=build /out/zero-backend /usr/local/bin/zero-backend
# ApplyMigrations 使用相对路径 migrations/（相对进程工作目录）
WORKDIR /app
COPY --from=build /src/migrations ./migrations

ENV APP_ENV=production
EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/zero-backend"]
