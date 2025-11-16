# Roadmap реализации life-auth-microservice

## 0. Определить публичный API контроллера

### 0.1. Список конечных точек AuthController

Спроектировать и зафиксировать контракт (URL + HTTP метод + DTO):

- `POST /auth/register` — регистрация пользователя
    - Request: `RegisterRequest`
    - Response: `MessageResponse` или `AuthResponse` (если сразу логинить)
- `GET /auth/verify` или `POST /auth/verify` — подтверждение регистрации по verification token
    - Request: query param `token` или DTO
    - Response: `MessageResponse`
- `POST /auth/login` — аутентификация
    - Request: `LoginRequest`
    - Response: `AuthResponse` (access + refresh token, базовая инфа о пользователе)
- `POST /auth/refresh` — обновление access token
    - Request: `RefreshTokenRequest`
    - Response: `AuthResponse`
- `POST /auth/logout` — логаут
    - Request: опционально refresh token или работать по текущему пользователю
    - Response: `MessageResponse`
- `POST /auth/password/reset-request` — запрос на сброс пароля
    - Request: DTO с email
    - Response: `MessageResponse`
- `POST /auth/password/reset` — сброс пароля по токену
    - Request: DTO с токеном и новым паролем
    - Response: `MessageResponse`

### 0.2. Решить формат ошибок

- Единый формат для всех эндпоинтов (например: `code`, `message`, `timestamp`, `details`).
- Описать это в комментариях/документации (позже — в Swagger/OpenAPI).

---

## 1. Контроллер: слой web (AuthController)

### 1.1. Реализация методов с учётом уже существующего AuthService

Для каждого эндпоинта:

- Описать сигнатуру метода (аннотации `@PostMapping`, `@GetMapping`, `@RequestBody`, `@RequestParam` и т.д.).
- Вызвать соответствующий метод сервиса (`AuthService`, при необходимости `UserService`, `VerificationService`).
- Преобразовать результат сервиса в DTO ответа (`AuthResponse`, `MessageResponse`).
- Настроить коды ответов:
    - 200/201 — успешные операции;
    - 400 — валидационные и бизнес‑ошибки;
    - 401/403 — проблемы аутентификации/авторизации.

### 1.2. Валидация входных данных

- Повесить `@Valid` на входные DTO.
- Убедиться, что в DTO стоят нужные аннотации Bean Validation (см. раздел про DTO).

---

## 2. Сервисный слой (AuthService, UserService, VerificationService)

### 2.1. AuthService — ядро аутентификации

Реализовать (или доработать) методы:

- `register(RegisterRequest)`
    - Проверка уникальности пользователя (email/username) → `DuplicateUserException`.
    - Создание пользователя с зашифрованным паролем.
    - Назначение роли по умолчанию.
    - Создание и сохранение `VerificationToken`.
    - Вызов `VerificationService` для отправки письма.
- `verifyRegistration(token)`
    - Поиск `VerificationToken` и связанного `User`.
    - Проверка срока действия токена.
    - Активация пользователя.
    - Удаление/деактивация использованного токена.
- `login(LoginRequest)`
    - Поиск пользователя.
    - Проверка статуса (активирован / неактивирован → `UserNotRegisterException`).
    - Проверка пароля.
    - Генерация JWT access/refresh токенов.
    - Создание и сохранение `RefreshToken`.
    - Возврат `AuthResponse`.
- `refreshToken(RefreshTokenRequest)`
    - Валидация refresh token (наличие в БД, срок действия, соответствие пользователю).
    - Генерация нового access token (и по необходимости нового refresh token).
    - Обновление/создание записи `RefreshToken`.
- `logout(...)`
    - Инвалидация/удаление соответствующих refresh токенов (по пользователю или по конкретному токену).
- `requestPasswordReset(email)`
    - Поиск пользователя по email.
    - Создание `PasswordResetToken`, сохранение.
    - Вызов `VerificationService` или отдельного сервисного метода для отправки письма.
- `resetPassword(token, newPassword)`
    - Поиск `PasswordResetToken`, проверка срока действия.
    - Смена пароля пользователю (с шифрованием).
    - Инвалидация всех активных refresh токенов пользователя.
    - Удаление использованного `PasswordResetToken`.

Во всех методах:
- Логировать ключевые события через `SecurityLog` / `SecurityEvent` (успех и неуспех).

### 2.2. UserService — управление пользователями

Реализовать/доработать методы:

- Поиск пользователя по:
    - id;
    - email;
    - username.
- Обновление профиля пользователя (минимальный набор полей).
- (При необходимости) блокировка/разблокировка пользователя:
    - учёт блокировки при логине.
- Отдача данных пользователя для security (если используется `UserDetailsService` / аналог).

### 2.3. VerificationService — работа с токенами подтверждения

Реализовать/доработать:

- Создание `VerificationToken` для пользователя (с генерацией значения и датой истечения).
- Валидация verification token (поиск в БД, проверка срока, статуса).
- Отправка писем пользователю:
    - при регистрации (ссылка вида `/auth/verify?token=...`);
    - при необходимости — для смены email.
- Повторная отправка письма, если пользователь не подтвердил аккаунт.

---

## 3. DTO и валидация

### 3.1. Проверка и корректировка DTO

- `RegisterRequest`:
    - Поля: email, username, password, подтверждение пароля (если нужно), доп. данные.
    - Валидация: `@NotBlank`, `@Email`, длина пароля, формат.
- `LoginRequest`:
    - Поля: email/username + password.
    - Валидация: `@NotBlank` на обязательных полях.
- `RefreshTokenRequest`:
    - Поле: refreshToken (строка).
    - Валидация: `@NotBlank`.
- `AuthResponse`:
    - Поля: accessToken, refreshToken, тип токена (например, `Bearer`), базовая инфа о пользователе (id, email, roles).
    - Без пароля и других чувствительных данных.
- `MessageResponse`:
    - Поле: `message` + при желании `code`.

### 3.2. Единый формат ошибок

- Продумать DTO для ошибок (например, `ErrorResponse`).
- Использовать его в глобальном обработчике (см. следующий раздел).

---

## 4. Исключения и глобальный обработчик

### 4.1. Использование существующих исключений

- `DuplicateUserException` — при регистрации уже существующего пользователя.
- `UserNotFoundException` — когда пользователь не найден.
- `UserNotRegisterException` — когда пользователь не подтвердил регистрацию/неактивирован.

### 4.2. Глобальный обработчик (@ControllerAdvice)

Реализовать:

- Метод обработки бизнес‑исключений (выше перечисленных) → возврат понятного `ErrorResponse`.
- Обработку:
    - `MethodArgumentNotValidException` — ошибки валидации DTO;
    - `AccessDeniedException` / ошибки аутентификации/авторизации.
- Соответствие HTTP‑кодов:
    - 400 — валидация / некорректный запрос;
    - 401 — неаутентифицирован;
    - 403 — доступ запрещён;
    - 404 — не найдено;
    - 409 — конфликты (например, дубликат пользователя).

---

## 5. Репозитории (repo)

### 5.1. UserRepository

- Методы:
    - `findByEmail(...)` / `findByUsername(...)`;
    - проверка существования (`existsByEmail`, `existsByUsername`).

### 5.2. RefreshTokenRepository

- Методы:
    - `findByToken(...)`;
    - `deleteByUser(...)` или `deleteByUserId(...)` (для логаута).
- При необходимости — методы для удаления просроченных токенов.

### 5.3. VerificationTokenRepository

- Методы:
    - `findByToken(...)`;
    - поиск/удаление просроченных токенов.

### 5.4. SecurityLogRepository

- Методы:
    - сохранение и поиск логов по типу события/пользователю (для аудита и аналитики).

---

## 6. Модели (model) и связи

### 6.1. User, Role

- Убедиться, что:
    - корректные аннотации JPA (таблицы, колонки, связи `@ManyToMany`/`@OneToMany` и т.п.);
    - учтены статусы пользователя (активен, заблокирован, не подтверждён).

### 6.2. RefreshToken, VerificationToken, PasswordResetToken

- Поля:
    - значение токена (строка);
    - владелец (`User`);
    - дата создания/истечения;
    - при необходимости — статус (активен/использован).
- Связи с `User` (обычно `@ManyToOne`).

### 6.3. SecurityEvent, SecurityLog

- Хранить:
    - тип события (логин, failed login, logout, password_reset_request и т.д.);
    - пользователя (если применимо);
    - IP/UA (по возможности);
    - время события.
- Использовать в `AuthService` и других сервисах для логирования.

---

## 7. Безопасность и JWT (security + config)

### 7.1. Конфигурация Security

- В существующем классе конфигурации:
    - Разрешить без аутентификации: `/auth/register`, `/auth/login`, `/auth/verify`, `/auth/refresh`, `/auth/password/*`.
    - Требовать JWT‑аутентификацию для остальных защищённых эндпоинтов.
- Настроить:
    - `AuthenticationManager` / `AuthenticationProvider` (загрузка пользователя из `UserService`/`UserRepository`);
    - парольный энкодер (например, `BCryptPasswordEncoder`).

### 7.2. JWT‑утилиты и фильтр

- Реализовать компонент работы с JWT:
    - генерация access/refresh токенов;
    - извлечение subject (id/email) и ролей;
    - проверка срока действия и подписи.
- Реализовать JWT‑фильтр:
    - получение токена из заголовка Authorization;
    - валидация токена;
    - установка аутентификации в SecurityContext.
- Интегрировать фильтр в цепочку Spring Security (до `UsernamePasswordAuthenticationFilter`).

---

## 8. Тестирование (controller → service → repo)

### 8.1. Тесты контроллера (WebMvc / интеграционные)

- Тесты для:
    - регистрации;
    - подтверждения регистрации;
    - логина;
    - обновления токена;
    - сброса пароля.
- Проверка статусов, структуры ответов и ошибок.

### 8.2. Тесты сервисов

- `AuthService`:
    - успешная/неуспешная регистрация;
    - логин (правильные/неправильные креды);
    - refresh token (валидный/невалидный/просроченный);
    - логаут;
    - восстановление пароля.
- `UserService`, `VerificationService`:
    - сценарии поиска, создания токенов, валидации токенов.

### 8.3. Тесты репозиториев

- Проверка корректной работы методов поиска и удаления:
    - пользователей;
    - токенов;
    - логов безопасности.

---

## 9. Документация и финальные штрихи

- Описать в README:
    - все эндпоинты `AuthController`, их запросы и ответы;
    - типы токенов, времена жизни, правила refresh/логина/логаута.
- Подключить Swagger/OpenAPI и задокументировать методы контроллера.
- Проверить, что:
    - все параметры конфигурации (JWT секрет, срок, БД) берутся из настроек;
    - сервис корректно стартует в Docker‑окружении (по `docker-compose.yml`).
