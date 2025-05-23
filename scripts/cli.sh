#!/bin/bash

set -e

# Завантажуємо змінні з .env
source "$(dirname "$0")/../.docker/grid/.env"

COMMAND="$1"
ACTION="$2"

WEB_CONTAINER_NAME="$WEB_CONTAINER"
TEST_CONTAINER_NAME="$TEST_CONTAINER"


if [ "$(basename "$PWD")" = "scripts" ]; then
  cd ..
fi

PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
#PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
TEST_PROJECT_TARGET_PATH="/app"

build_image() {
  COMPONENT="$1"
  EXPECTED_PATH="$PROJECT_ROOT/.docker/$COMPONENT"
  DOCKERFILE_PATH="$EXPECTED_PATH/Dockerfile"

  if [ ! -d "$EXPECTED_PATH" ] || [ ! -f "$DOCKERFILE_PATH" ]; then
    echo "❌ Очікується структура: $EXPECTED_PATH/Dockerfile"
    echo "   Але вона не знайдена. Перевір структуру проекту."
    exit 1
  fi

  if [ "$COMPONENT" = "tests" ]; then
    IMAGE_NAME="$TEST_IMAGE"
  else
    IMAGE_NAME="$WEB_IMAGE"
  fi

  echo "🔨 Створюємо образ '$IMAGE_NAME' з Dockerfile '$DOCKERFILE_PATH'"
  podman build -f "$DOCKERFILE_PATH" -t "$IMAGE_NAME" "$PROJECT_ROOT"
  echo "✅ Образ зібрано: $IMAGE_NAME"
}

run_web_server() {
  HOST_REPORT_PATH="$HOST_WEB_REPORT_PATH"

  if [ "$USE_NAMED_VOLUME" = "true" ]; then
    echo "📦 Використовується named volume: $VOLUME_NAME"
    podman run --rm --name "$WEB_CONTAINER_NAME" -d -p "${REPORT_PORT}:${REPORT_PORT}" \
      -v "$VOLUME_NAME":"$WEB_REPORT_TARGET_PATH":Z \
      "$WEB_IMAGE"
  else
    if [ ! -d "$HOST_REPORT_PATH" ]; then
      echo "❌ Папка не знайдена: $HOST_REPORT_PATH"
      exit 1
    fi
    echo "Монтуємо папку з Allure-репортом: $HOST_REPORT_PATH"
    echo "🚀 Запускаємо вебсервер $WEB_CONTAINER_NAME з репортами з: $HOST_REPORT_PATH"
    podman run --rm --name "$WEB_CONTAINER_NAME" -d -p "${REPORT_PORT}:${REPORT_PORT}" \
      -v "$HOST_REPORT_PATH":"$WEB_REPORT_TARGET_PATH":Z \
      "$WEB_IMAGE"
  fi
}

run_tests_with_project_dir() {
  HOST_PROJECT_PATH="$HOST_TEST_PROJECT_PATH"
  HOST_REPORT_PATH="$HOST_WEB_REPORT_PATH"

  echo "Монтуємо код проєкту: $HOST_PROJECT_PATH"

  if [ "$USE_NAMED_VOLUME" = "true" ]; then
    echo "📦 Використовується named volume: $VOLUME_NAME"
    podman run --rm --name "$TEST_CONTAINER_NAME" -d --network selenium-grid-net \
      -v "$HOST_PROJECT_PATH":"$TEST_PROJECT_TARGET_PATH":Z \
      -v "$VOLUME_NAME":"$TEST_REPORT_TARGET_PATH":Z \
      "$TEST_IMAGE"
  else
    if [ ! -d "$HOST_PROJECT_PATH" ]; then
      echo "❌ Помилка: папка проєкту $HOST_PROJECT_PATH не існує. Вкажи правильний шлях у .env"
      exit 1
    fi
    if [ ! -d "$HOST_REPORT_PATH" ]; then
      echo "📂 Папка з репортами $HOST_REPORT_PATH не існує. Створюю."
      mkdir -p "$HOST_REPORT_PATH"
    fi
    echo "🚀 Запускаємо контейнер тестів $TEST_CONTAINER_NAME з кодом з: $HOST_PROJECT_PATH"
    podman run --rm --name "$TEST_CONTAINER_NAME" -d --network selenium-grid-net \
      -v "$HOST_PROJECT_PATH":"$TEST_PROJECT_TARGET_PATH":Z \
      -v "$HOST_REPORT_PATH":"$TEST_REPORT_TARGET_PATH":Z \
      "$TEST_IMAGE"
  fi
  echo "📦 Підключитися до контейнеру: cli.sh tests conn"
}


run_grid() {
  echo "🔧 Запускаємо Selenium Hub + 2 Chrome Node (без podman-compose, із загальною мережею)"

  # Створюємо мережу, якщо її ще нема
  if ! podman network exists selenium-grid-net; then
    podman network create selenium-grid-net
  fi

  # Hub
  podman run -d --name selenium-hub --network selenium-grid-net -p 4444:4444 docker.io/selenium/hub:4.21.0

  # Очікуємо запуск hub
  sleep 5

  # Chrome Nodes
  for i in 1 2; do
    podman run -d --name chrome$i --network selenium-grid-net --shm-size=2g \
      -e SE_EVENT_BUS_HOST=selenium-hub \
      -e SE_EVENT_BUS_PUBLISH_PORT=4442 \
      -e SE_EVENT_BUS_SUBSCRIBE_PORT=4443 \
      -p 790$i:7900 \
      docker.io/selenium/node-chrome:4.21.0
  done

  echo "✅ Grid піднято вручну: http://localhost:4444"
}



down_grid() {
  echo "🛑 Зупиняємо вручну запущений Selenium Grid (hub + 2 chrome ноди)"
  for name in chrome1 chrome2 selenium-hub; do
    if podman ps -a --format "{{.Names}}" | grep -q "^$name$"; then
      echo "⛔ Зупиняємо контейнер: $name"
      podman stop "$name" && podman rm "$name"
    else
      echo "ℹ️ Контейнер $name не знайдено"
    fi
  done
  echo "✅ Grid вручну зупинено"
}



status_grid() {
  echo "📋 Статус вручну запущеного Selenium Grid:"
  for name in selenium-hub chrome1 chrome2; do
    if podman ps -a --format "{{.Names}}" | grep -q "^$name$"; then
      status=$(podman inspect -f '{{.State.Status}}' "$name")
      echo "🔹 $name: $status"
    else
      echo "❌ $name: не знайдено"
    fi
  done
}



run_all() {
  echo "🚀 Запускаємо Grid (hub + 2 chrome ноди) та контейнер тестів"

  run_web_server
  run_grid
  sleep 10
  run_tests_with_project_dir

  echo "✅ Усі сервіси запущено вручну"
}


down_all() {
  echo "🛑 Зупиняємо Grid і контейнер тестів"

  down_grid

  if podman ps -a --format "{{.Names}}" | grep -q "^$TEST_CONTAINER_NAME$"; then
    echo "⛔ Зупиняємо контейнер тестів: $TEST_CONTAINER_NAME"
    podman stop "$TEST_CONTAINER_NAME" || true
    podman rm "$TEST_CONTAINER_NAME" || true
  fi

  if podman ps -a --format "{{.Names}}" | grep -q "^$WEB_CONTAINER_NAME$"; then
    echo "⛔ Зупиняємо контейнер вебсерверу: $WEB_CONTAINER_NAME"
    podman stop "$WEB_CONTAINER_NAME" || true
    podman rm "$WEB_CONTAINER_NAME" || true
  fi

  echo "✅ Усі сервіси зупинено"
}


stop_container(){
  container_name="$1"
  echo "Зупиняємо контейнер: $container_name"
  podman stop "$container_name"
  echo "Контейнер: ${container_name} зупинено"
}

connect_toContainer(){
  container_name="$1"
  echo "Коннектимось до контейнеру: $container_name"
  podman exec -it "$container_name" /bin/bash
}

clean_volumes() {
  echo "🧹 Видаляємо volume '$VOLUME_NAME'..."
  podman volume rm -f "$VOLUME_NAME" 2>/dev/null || echo "ℹ️ Volume вже не існує або не використовується"
  echo "✅ Volume очищено."
}

clean_test_container() {
  echo "🧹 Видаляємо контейнер тестів '$TEST_CONTAINER_NAME'..."

  if podman container exists "$TEST_CONTAINER_NAME"; then
    podman rm -f "$TEST_CONTAINER_NAME"
    echo "✅ Контейнер тестів очищено."
  else
    echo "ℹ️ Контейнер $TEST_CONTAINER_NAME вже не існує."
  fi
}

clean_web_container() {
  echo "🧹 Видаляємо контейнер веб-сервера '$WEB_CONTAINER_NAME'..."
  podman rm -f "$WEB_CONTAINER_NAME" 2>/dev/null || echo "ℹ️ Контейнер вже не існує або зупинений"
  echo "✅ Контейнер веб-сервера очищено."
}

clean_test_image() {
  echo "🧹 Видаляємо образ тестів '$TEST_IMAGE'..."
  podman rmi -f "$TEST_IMAGE" 2>/dev/null || echo "ℹ️ Образ вже не існує або використовується"
  echo "✅ Образ тестів очищено."
}

clean_web_image() {
  echo "🧹 Видаляємо образ веб-сервера '$WEB_IMAGE'..."
  podman rmi -f "$WEB_IMAGE" 2>/dev/null || echo "ℹ️ Образ вже не існує або використовується"
  echo "✅ Образ веб-сервера очищено."
}

clean_all() {
  echo "🚨 Повне очищення: volume, контейнери, образи"
  clean_test_container
  clean_web_container
  clean_test_image
  clean_web_image
  clean_volumes
  echo "✅ Всі компоненти очищено."
}

case "$COMMAND" in
  all-up)
    run_all
    ;;
  all-down)
    down_all
    ;;

  status-grid)
    status_grid
    ;;

  down-grid)
    down_grid
    ;;

  up-grid)
    run_grid
    ;;

  web)
    case "$ACTION" in
      create)
        build_image web
        ;;
      stop)
        stop_container "$WEB_CONTAINER_NAME"
        ;;
      run)
        run_web_server
        ;;
      *)
        echo "❓ Невідома дія: $ACTION. Доступно: create, run, stop"
        ;;
    esac
    ;;
  tests)
    case "$ACTION" in
      create)
        build_image tests
        ;;
      run)
        run_tests_with_project_dir
        ;;
      stop)
        stop_container "$TEST_CONTAINER_NAME"
        ;;
      conn)
        connect_toContainer "$TEST_CONTAINER_NAME"
        ;;
      *)
        echo "❓ Невідома дія: $ACTION. Доступно: create, run, conn, stop"
        ;;
    esac
    ;;
  clean)
    case "$ACTION" in
      volumes)
        clean_volumes
        ;;
      test-container)
        clean_test_container
        ;;
      web-container)
        clean_web_container
        ;;
      test-image)
        clean_test_image
        ;;
      web-image)
        clean_web_image
        ;;
      all)
        clean_all
        clean_web_image
        ;;
      *)
        echo "❓ Невідома дія: $ACTION. Доступно: volumes, test-container, web-container, test-image, web-image, all"
        ;;
    esac
    ;;
  
  *)
    echo "❓ Невідома команда: $COMMAND"
    echo "📌 Доступні команди:"
    echo "    tests               -> create, run, conn, stop"
    echo "    web                 -> create, run, stop"
    echo "    clean               -> volumes, test-container, web-container, test-image, web-image, all"
    echo "    up-grid             -> підняти Selenium Grid вручну через podman"
    echo "    down-grid           -> зупинити вручну запущений Selenium Grid"
    echo "    status-grid         -> перевірити статус Grid-контейнерів"
    echo "    all-up              -> підняти Grid + тести однією командою"
    echo "    all-down            -> зупинити все однією командою"
    ;;
esac