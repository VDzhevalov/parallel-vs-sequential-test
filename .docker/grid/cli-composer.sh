#!/bin/bash

set -e

source ~/.venvs/podman/bin/activate

COMPOSE_FILE="podman-compose.yml"
COMPOSE_PATH="$(dirname "$0")"
FULL_COMPOSE_PATH="$COMPOSE_PATH/$COMPOSE_FILE"
ENV_PATH="$COMPOSE_PATH/.env"

if [ ! -f "$FULL_COMPOSE_PATH" ]; then
  echo "❌ Не знайдено файл $FULL_COMPOSE_PATH"
  exit 1
fi

# Завантажуємо змінні з .env
if [ -f "$ENV_PATH" ]; then
  source "$ENV_PATH"
else
  echo "⚠️ Файл .env не знайдено: $ENV_PATH"
fi

COMMAND="$1"
ACTION="$2"
PROFILE="$3"

compose() {
  podman-compose -f "$FULL_COMPOSE_PATH" "$@"
}

case "$COMMAND" in
  up)
    if [[ "$PROFILE" != "" ]]; then
      echo "🚀 Підіймаємо профіль: $PROFILE"
      podman-compose --profile "$PROFILE" -f "$FULL_COMPOSE_PATH" up -d
    else
      echo "🚀 Підіймаємо всі сервіси"
      compose --profile tests --profile report up -d
      podman ps
    fi
    ;;
  down)
    if [[ "$PROFILE" != "" ]]; then
      echo "💪 Зупиняємо профіль: $PROFILE  $FULL_COMPOSE_PATH"
      podman pod stop pod_grid || true
      podman-compose --profile "$PROFILE" -f "$FULL_COMPOSE_PATH" down
    else
      echo "💪 Зупиняємо всі сервіси  $FULL_COMPOSE_PATH"
      podman pod stop pod_grid || true
      compose down
    fi
    ;;
  build)
    if [[ "$PROFILE" != "" ]]; then
      echo "🔨 Збираємо образи для профілю: $PROFILE"
      podman-compose --profile "$PROFILE" -f "$FULL_COMPOSE_PATH" build
    else
      echo "🔨 Збираємо всі образи"
      podman-compose --profile tests --profile report -f "$FULL_COMPOSE_PATH" build
    fi
    ;;
  ps)
    echo "📦 Перелік запущених контейнерів (через podman)"
    podman ps --format "🔹 {{.Names}} — {{.Status}} — {{.Ports}}"
    ;;
  logs)
    echo "📜 Логи сервісів"
    compose logs -f
    ;;
  exec)
    SERVICE="$ACTION"
    if [ -z "$SERVICE" ]; then
      echo "❌ Вкажи ім'я сервісу (napр., tests або web)"
      exit 1
    fi
    shift 2
    echo "⚙️ Виконуємо команду у $SERVICE: $*"
    podman-compose -f "$FULL_COMPOSE_PATH" exec "$SERVICE" "$@"
    ;;
  conn)
    SERVICE="$ACTION"
    if [ -z "$SERVICE" ]; then
      echo "❌ Вкажи ім'я сервісу (napр., tests або web)"
      exit 1
    fi
    echo "🔐 Підключаємося до $SERVICE"
    podman  exec -it "$SERVICE" /bin/bash
    ;;
  volume-path)
    if [ -z "$VOLUME_NAME" ]; then
      echo "❌ Змінна VOLUME_NAME не задана. Перевір файл .env"
      exit 1
    fi

    echo "🔍 Шукаємо інформацію про volume: $VOLUME_NAME"

    VOLUME_INFO=$(podman volume inspect "$VOLUME_NAME" 2>/dev/null)

    if [ $? -ne 0 ]; then
      echo "❌ Volume $VOLUME_NAME не знайдено"
      exit 1
    fi

    MOUNTPOINT=$(echo "$VOLUME_INFO" | grep -oP '"Mountpoint":\s*"\K[^"]+')
    echo "📂 Volume $VOLUME_NAME зберігається за шляхом:"
    echo "$MOUNTPOINT"

    if [[ "$ACTION" == "open" ]]; then
      echo "📁 Відкриваємо директорію..."
      xdg-open "$MOUNTPOINT" >/dev/null 2>&1 || open "$MOUNTPOINT" || echo "⚠️ Не вдалося відкрити директорію автоматично"
    fi
    ;;
  *)
    echo "❓ Невідома команда: $COMMAND"
    echo "Доступні команди:"
    echo "  up [profile]       — Підняти контейнери. Профайли: tests(grid на 2 ноди + тести), report - перегляд репортів"
    echo "  down [profile]     — Зупинити контейнери"
    echo "  build [profile]    — Збірка образів"
    echo "  ps                 — Стан контейнерів"
    echo "  logs               — Логи"
    echo "  exec <service>     — Виконати команду у контейнері"
    echo "  conn <service>     — Підключитися до контейнера service=CONTAINER_NAME"
    echo "  volume-path [open] — Шлях до volume $VOLUME_NAME (опційно відкриває)"
    ;;
esac