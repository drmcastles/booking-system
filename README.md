# 🏨 Hotel Booking & Recommendation System

Микросервисная система на базе **Spring Cloud**, реализующая распределённое бронирование с использованием паттерна **SAGA** и алгоритма умных рекомендаций.

---

## 🏗 Архитектура системы

### Eureka Server (8761)
- Регистрация и обнаружение всех сервисов

### API Gateway (8085)
- Единая точка входа
- Маршрутизация запросов
- Проверка JWT

### Hotel Service (8081)
- Управление отелями и комнатами
- Хранение данных о популярности номеров (`timesBooked`)

### Booking Service (8082)
- Оркестратор бронирований
- Логика SAGA
- Алгоритм рекомендаций

---

## 🚀 Инструкция по запуску (Step-by-Step)

⚠️ **Запускайте сервисы строго в следующем порядке**

1. **Eureka Server**
    - Запустите `EurekaApplication`
    - Порт: `8761`
    - Дождитесь загрузки панели Eureka

2. **Hotel Service**
    - Запустите `HotelServiceApplication`
    - Порт: `8081`
    - Инициализация базы отелей

3. **Booking Service**
    - Запустите `BookingServiceApplication`
    - Порт: `8082`
    - При старте создаётся администратор:
      ```
      admin / password
      ```

4. **API Gateway**
    - Запустите `GatewayApplication`
    - Порт: `8085`
    - Система готова к работе
## 🧪 Тестирование (Unit & Integration Tests)

### Реализованные тесты

- **BookingSagaTest**
    - Проверяет логику отката SAGA
    - При ошибке статус брони меняется на `REJECTED`

- **SecurityAccessTest**
    - Проверка разграничения прав
    - Пользователь получает `403 Forbidden` при доступе к admin-эндпоинтам

- **Hotel & Room Controller Tests**
    - CRUD-операции
    - Поиск свободных номеров

### Запуск Unit тестов

```bash
mvn test
```
###🧪 Сквозное тестирование (End-to-End)
```
🧪 Полный сценарий тестирования (PowerShell)
# Настройки
$gatewayUrl = "http://localhost:8085/api"
$id = Get-Random
$uBody = @{ username="user$id"; password="p"; role="USER" } | ConvertTo-Json

Write-Host "--- Starting E2E Test ---" -ForegroundColor Cyan

# 1. Регистрация и получение реального ID пользователя
$uLogin = Invoke-RestMethod -Uri "$gatewayUrl/users/register" -Method Post -ContentType "application/json" -Body $uBody
$uLogin = Invoke-RestMethod -Uri "$gatewayUrl/users/login" -Method Post -ContentType "application/json" -Body $uBody
$headers = @{ "Authorization"="Bearer $($uLogin.token)"; "Content-Type"="application/json" }

# 2. Тест распределенной транзакции (SAGA)
# Бронируем комнату в предустановленном отеле (ID: 1)
$bReq = @{ userId=$uLogin.userId; hotelId=1; startDate="2026-10-01T10:00"; endDate="2026-10-05T10:00" } | ConvertTo-Json
$res = Invoke-RestMethod -Uri "$gatewayUrl/bookings/create" -Method Post -Headers $headers -Body $bReq
Write-Host "Status SAGA: $($res.status)" -ForegroundColor Green

# 3. Тест алгоритма рекомендаций
# Комнаты должны вернуться отсортированными по популярности (timesBooked)
$rooms = Invoke-RestMethod -Uri "$gatewayUrl/hotels/rooms/hotel/1/available?checkIn=2026-12-01T00:00:00&checkOut=2026-12-10T00:00:00" -Method Get -Headers $headers
Write-Host "Рекомендованные ID комнат: $($rooms -join ', ')" -ForegroundColor Magenta

# 4. Проверка безопасности (RBAC)
try { 
    Invoke-RestMethod -Uri "$gatewayUrl/hotels" -Method Post -Headers $headers -Body (@{name="X"}|ConvertTo-Json) 
} catch { Write-Host "RBAC: Доступ запрещен (Ок)" -ForegroundColor Yellow }
```

🧪 Полный сценарий тестирования (cURL)
cat << 'EOF' > full_test_8085.sh
#!/bin/bash
```
#!/bin/bash

GATEWAY_URL="http://localhost:8085/api"
RAND_ID=$RANDOM
USER_JSON="{\"username\":\"user$RAND_ID\",\"password\":\"p\",\"role\":\"USER\"}"

echo -e "\e[36m=== STARTING cURL E2E TEST ===\e[0m"

# 1. РЕГИСТРАЦИЯ
echo -e "\n\e[33m[1/4] Registering User...\e[0m"
curl -s -X POST "$GATEWAY_URL/users/register" \
     -H "Content-Type: application/json" \
     -d "$USER_JSON" > /dev/null

# 2. ЛОГИН (извлекаем токен и userId)
echo -e "\e[33m[2/4] Logging in...\e[0m"
LOGIN_RES=$(curl -s -X POST "$GATEWAY_URL/users/login" \
     -H "Content-Type: application/json" \
     -d "$USER_JSON")

# Для работы этого шага желательно наличие утилиты jq (sudo apt install jq)
TOKEN=$(echo $LOGIN_RES | jq -r '.token')
USER_ID=$(echo $LOGIN_RES | jq -r '.userId')

echo -e "\e[32m✅ Logged in! UserID: $USER_ID\e[0m"

# 3. ТЕСТ SAGA (Бронирование отеля ID 1)
echo -e "\n\e[33m[3/4] Testing SAGA (Hotel ID: 1)...\e[0m"
BOOKING_JSON="{\"userId\":$USER_ID,\"hotelId\":1,\"startDate\":\"2026-10-01T10:00:00\",\"endDate\":\"2026-10-05T10:00:00\"}"

SAGA_RES=$(curl -s -X POST "$GATEWAY_URL/bookings/create" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "$BOOKING_JSON")

echo -e "\e[35mSAGA Response: $SAGA_RES\e[0m"

# 4. ТЕСТ РЕКОМЕНДАЦИЙ
echo -e "\n\e[33m[4/4] Testing Recommendations...\e[0m"
curl -s -X GET "$GATEWAY_URL/hotels/rooms/hotel/1/available?checkIn=2026-12-01T00:00:00&checkOut=2026-12-10T00:00:00" \
     -H "Authorization: Bearer $TOKEN" | jq '.'

echo -e "\n\e[36m=== TEST FINISHED ===\e[0m"
```

## 🔐 Права доступа

### Admin
- Полный CRUD-доступ
- Просмотр всех бронирований

### User
- Создание и просмотр **только своих** данных
- Доступ к чужим ID → `403 Forbidden`

---

## 📌 Итог

Проект демонстрирует:
- Spring Cloud Microservices
- Eureka + API Gateway
- JWT Security
- SAGA orchestration
- Интеграционные тесты
- Готовность к масштабированию