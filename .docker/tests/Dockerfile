FROM docker.io/openjdk:17-jdk-slim

# Встановлюємо Allure CLI
RUN apt-get update && apt-get install -y wget unzip curl \
 && wget -qO allure.zip https://github.com/allure-framework/allure2/releases/download/2.29.0/allure-2.29.0.zip \
 && unzip allure.zip -d /opt/ \
 && ln -s /opt/allure-2.29.0/bin/allure /usr/bin/allure \
 && rm allure.zip

# Прогріваємо Allure CLI без проєкту
RUN mkdir -p /tmp/allure-results /tmp/allure-dummy \
 && allure generate --clean --output /tmp/allure-dummy /tmp/allure-results || true

# Копіюємо проєкт
WORKDIR /app
COPY . .

RUN chmod +x ./gradlew \
 && ./gradlew clean build -x test --no-daemon

# Запускаємо в режимі життя для подальшого підключення і роботи або виконання тасок вручну.
CMD ["sh", "-c", " tail -f /dev/null"]