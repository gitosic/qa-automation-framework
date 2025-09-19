# WireMock Setup Guide

## Что эмулирует наш WireMock:
- `GET /login` → HTML страница логина
- `POST /api/login` → JSON ответ (success/error)
- `GET /dashboard` → Dashboard page (в разработке)

## Как запустить тесты:
1. Тесты автоматически запускают WireMock сервер
2. Сервер стартует на http://localhost:8080
3. Все тесты используют этот URL как базовый

## Добавление новых моков:
1. Положите JSON-файл в `src/test/resources/wiremock/mappings/`
2. Положите статические файлы в `src/test/resources/wiremock/__files/`
3. Перезапустите тесты