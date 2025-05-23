# 📦 parallel-vs-sequential-test

Цей репозиторій містить систему для запуску UI-тестів на базі Selenide + JUNIT5 у Podman середовищі з підтримкою Selenium Grid, Allure Report та CLI-керування.

---

## ⚙️ Структура проєкту

У репозиторії використовуються такі основні директорії та пакети:

* `setup.remote` — класи для конфігурації:

    * Remote WebDriver
    * Selenide
    * Allure
    * Окремо реалізовані класи для `@TestInstance(TestInstance.Lifecycle.PER_METHOD)` і `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`

* `datareader` — класи для читання тестових даних з CSV/JSON/файлів. Застосовується у "FromFile"-тасках.

* `resources/test-data/` — файли з вхідними даними для тестів, які виконуються з тегами типу `FromFile`

```
parallel-vs-sequential-test/
├── .docker/
│   ├── grid/                  # Конфігурація Selenium Grid та CLI
│   ├── tests/                 # Dockerfile для тестового контейнера
│   └── web/                   # Dockerfile для веб-сервера Allure
├── scripts/
│   └── cli.sh                 # Універсальний скрипт керування
├── build.gradle               # Gradle-конфігурація
├── settings.gradle
└── README.md
```

---

## 🚀 Швидкий старт

### 📋 Передумови

Для запуску тестового середовища необхідно мати встановлені:

* [Podman](https://podman.io/) — як основний контейнерний рушій
* (Опційно) [podman-compose](https://github.com/containers/podman-compose) — для запуску через `podman-compose.yml`

> Без встановленого Podman запуск тестів неможливий

### 1. Підготовка

* Встанови [Podman](https://podman.io/)
* Переконайся, що `podman` та `podman network` працюють
* (Опційно) Використай `podman-compose`, якщо хочеш запускати через YAML

### 2. Налаштування

> ⚠️ ВАЖЛИВО:
>
> * Перед першим запуском **обов’язково потрібно вказати абсолютний шлях до проєкту** у змінній `HOST_TEST_PROJECT_PATH`. Інакше контейнер з тестами **не зможе змонтувати код і не стартує**.
>
> * Змінну `HOST_WEB_REPORT_PATH` потрібно заповнювати **тільки якщо відмовляєтесь від named volume**. У такому разі встановіть `USE_NAMED_VOLUME=false`.

Встанови змінні у `.docker/grid/.env`:

```env
TEST_IMAGE=selenide-compare-tests-image
WEB_IMAGE=allure-report-web-image
TEST_CONTAINER=selenide-compare-tests-server
WEB_CONTAINER=allure-web-server
VOLUME_NAME=grid_allure-results-volume
SELENIUM_HUB_URL=http://selenium-hub:4444/wd/hub
REPORT_PORT=8080
SELENIUM_PORT=4444
USE_NAMED_VOLUME=true
WEB_REPORT_TARGET_PATH=/app/build/allure-report
TEST_REPORT_TARGET_PATH=/app/allure-report
HOST_WEB_REPORT_PATH=/абсолютний/шлях/до/allure-звітів
HOST_TEST_PROJECT_PATH=/абсолютний/шлях/до/проєкту
```

---

## 🧪 Основні сценарії запуску

Цей проєкт підтримує два основних способи запуску середовища:

* 🔹 Через скрипт `cli.sh` (ручне керування з Podman)
* 🔸 Через `cli-composer.sh` (комплексне керування з podman-compose)

---

### 🔹 CLI (ручний контроль через Podman)

Скрипт запуску: `./scripts/cli.sh`

### 🔼 Запуск усієї системи

```bash
./scripts/cli.sh run_all
```

### 🔽 Зупинка всієї системи

```bash
./scripts/cli.sh down_all
```

### 🧩 Компонентно

#### Grid (Hub + Chrome Nodes)

```bash
./scripts/cli.sh up-grid
./scripts/cli.sh down-grid
./scripts/cli.sh status-grid
```

#### Web (Allure Web Server)

```bash
./scripts/cli.sh web create
./scripts/cli.sh web run
./scripts/cli.sh web stop
```

#### Tests

```bash
./scripts/cli.sh tests create
./scripts/cli.sh tests run
./scripts/cli.sh tests stop
./scripts/cli.sh tests conn
```

#### Clean (очистка ресурсів)

```bash
./scripts/cli.sh clean all
```

---

### 🔸 CLI-COMPOSER (автоматизований запуск через podman-compose)

Скрипт запуску: `./cli-composer.sh (запускати з .docker/grid/ інакше компосер буде сваритися)`

#### 🔼 Запуск середовища

```bash
cd .docker/grid/
```
```bash
./cli-composer.sh up
```

#### 🔽 Зупинка середовища

```bash
./cli-composer.sh down
```

#### 📋 Доступні профілі (параметри --profile):

* `tests` — запуск лише тестового контейнера + selenium grid
* `report` — запуск лише веб-сервера зі звітом
* `all` (за замовчуванням) — запуск повного середовища

#### 🔗 Підключення до контейнера:

```bash
./cli-composer.sh conn <name>
```
За замовчуванням підключення до контейнера тестів:
```bash
./cli-composer.sh conn selenide-compare-tests-server
```

> ❗ Для роботи cli-composer потрібно встановлений `podman-compose`

---

## 🧪 Запуск тестів вручну

### 🔗 Підключення до контейнера з тестами:

```bash
./scripts/cli.sh tests conn
```

### 🏃 Запуск Gradle-тестів всередині контейнера:

```bash
./gradlew <назва_таски>
```

### 📋 Доступні таски:

| Таска                       | Опис                                                        |
| --------------------------- |-------------------------------------------------------------|
| `oneByOnePMITests`          | PER\_METHOD Inline — послідовно, дані в коді                |
| `oneByOnePMFTests`          | PER\_METHOD FromFile — послідовно, дані з файлів            |
| `oneByOnePCITests`          | PER\_CLASS Inline — послідовно, дані в коді                 |
| `oneByOnePCFTests`          | PER\_CLASS FromFile — послідовно, дані з файлів             |
| `parallelForkPMITests`      | PER\_METHOD Inline — паралельно (forks), дані в коді        |
| `parallelForkPMFTests`      | PER\_METHOD FromFile — паралельно (forks), дані з файлів    |
| `parallelForkPCITests`      | PER\_CLASS Inline — паралельно (forks), дані в коді         |
| `parallelForkPCFTests`      | PER\_CLASS FromFile — паралельно (forks), дані з файлів     |
| `parallelConcurrentIlTests` | CONCURRENT Inline — @Execution(CONCURRENT), дані в коді     |
| `parallelConcurrentFFTests` | CONCURRENT FromFile — @Execution(CONCURRENT), дані з файлів |

> ⚠️ Усі таски запускаються лише в середині контейнера `tests`

---

## 📊 Робота з Allure

Після запуску тестів:

* Звіти зберігаються у `build/allure-report`
* Web-сервер відкриває звіт на `http://localhost:8080`
* Доступ до Allure Report здійснюється через браузер:

```
http://localhost:8080
```

---

## 📎 Примітки

* Під час запуску Grid вручну (без compose) усі контейнери повинні бути в одній мережі `selenium-grid-net` (налаштовано за замовчуванням)
* Запуск тестів здійснюється у виділеному контейнері, підключеному до тієї ж мережі, що і Grid

