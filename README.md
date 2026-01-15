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

### Запуск тестов

```bash
mvn test


---

```markdown
## 📡 Полный сценарий тестирования (PowerShell)

```powershell
# 1. Авторизация (Получение JWT токена)
$authRes = Invoke-RestMethod -Method Post -Uri "http://localhost:8085/api/users/login" `
    -ContentType "application/json" `
    -Body '{"username":"admin", "password":"password"}'

$token = $authRes.token
echo "Токен получен: $token"

# 2. Тест SAGA: Создание бронирования
$bookingBody = @{
    hotelId = 1
    userId = 1
    startDate = "2026-05-01T14:00:00"
    endDate = "2026-05-10T12:00:00"
} | ConvertTo-Json

$newBooking = Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8085/api/bookings/create" `
    -Headers @{Authorization="Bearer $token"} `
    -ContentType "application/json" `
    -Body $bookingBody

echo "Бронирование создано. Итоговый статус SAGA: $($newBooking.status)"

# 3. Просмотр всех бронирований (Admin)
Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8085/api/bookings/all" `
    -Headers @{Authorization="Bearer $token"} | Format-Table

# 4. Проверка рекомендаций
Invoke-RestMethod -Method Get `
    -Uri "http://localhost:8085/api/recommendations/1" `
    -Headers @{Authorization="Bearer $token"}
    
```markdown

🧪 Полный сценарий тестирования (cURL)
# 1. Авторизация и сохранение токена в переменную TOKEN
TOKEN=$(curl -s -X POST http://localhost:8085/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"password"}' | grep -oP '(?<="token":")[^"]*')

echo "Токен получен: $TOKEN"

# 2. Создание бронирования (SAGA)
curl -X POST http://localhost:8085/api/bookings/create \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "hotelId": 1,
    "userId": 1,
    "startDate": "2026-05-01T14:00:00",
    "endDate": "2026-05-10T12:00:00"
  }'

# 3. Просмотр всех бронирований
curl -X GET http://localhost:8085/api/bookings/all -H "Authorization: Bearer $TOKEN"

# 4. Получение рекомендаций
curl -X GET http://localhost:8085/api/recommendations/1 -H "Authorization: Bearer $TOKEN"

```markdown
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