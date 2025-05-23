# Образ для локального запуску веб-сервера для перегляду Allure-репорта

FROM docker.io/openjdk:17-slim AS runtime

RUN apt-get update && apt-get install -y \
    python3 \
    --no-install-recommends && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

CMD ["sh", "-c", "python3 -m http.server 8080 --directory build/allure-report", "--bind", "0.0.0.0"]