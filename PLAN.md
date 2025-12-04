## План внедрения JWT (актуально на текущую реализацию)

### Этап 1. Конфигурация и окружение
- Проверить `pom.xml`: зависимости `spring-boot-starter-security`, `spring-boot-starter-validation`, `io.jsonwebtoken` уже добавлены/обновить версии при необходимости — чтобы использовать готовые утилиты и фильтры.
- В `application.properties` задать `jwt.secret`, `jwt.access-expiration`, `jwt.refresh-expiration`, `spring.security.filter.order` (если нужно) — чтобы сервис читал параметры TTL и ключи из конфига.
- Вынести секреты в переменные окружения/профили (`spring.profiles.active`, `application-dev.properties`) — чтобы не хранить ключи в гите и упростить деплой.

### Этап 2. Доменные сущности и репозитории
- `model/User`: убедиться, что есть поля статуса/ролей и добавить `tokenVersion` (целое) — чтобы можно было ревокать все access JWT при сбросе пароля.
- `model/RefreshToken`: сейчас есть `id/token/user/expiresAt/revoked`; добавить `createdAt`, `updatedAt`, `rotatedTokenId` (опционально) — чтобы отслеживать ротацию и чистку.
- `repo/RefreshTokenRepository`: расширить методами `deleteByUserId`, `findAllByUserIdAndRevokedFalse` — чтобы быстро инвалидировать токены при logout/compromise.
- Flyway/Liquibase миграции под новые поля (если миграции уже используются) — чтобы база соответствовала сущностям.

### Этап 3. JWT инфраструктура
- `JWT/JwtUtil`: реализовать генерацию access JWT (claims: `sub`, `roles`, `tokenVersion`), валидацию подписи и сроков, парсинг subject/role — чтобы выдавать и проверять токены без обращения к БД.
- `JWT/JwtFilter`: считать `Authorization: Bearer`, валидировать через `JwtUtil`, загружать пользователя через `UserDetailsService`, помещать `Authentication` в контекст — чтобы защищённые эндпоинты видели пользователя.
- Исключения (`JwtAuthenticationException` или reuse) и логирование в фильтре — чтобы корректно возвращать 401/403 и писать аудит.

### Этап 4. Refresh-токены и сервисный слой
- `service/RefreshTokenService`: реализовать создание токена (UUID, срок из настроек), сохранение в `RefreshTokenRepository`, валидацию (проверка `revoked`, срока), ротацию и удаление — чтобы централизовать управление refresh.
- Добавить scheduled cleanup (Spring `@Scheduled`) или метод для ручной чистки просроченных токенов — чтобы таблица не росла бесконтрольно.
- `service/VerificationService`, `SecurityLogService`: проверить что регистрируют события; при необходимости добавить методы, которые вызываются из auth-потока — чтобы аудит и верификация были согласованы с JWT.

### Этап 5. AuthService и контроллеры
- `service/AuthService`:
  - `login`: загрузить пользователя, проверить пароль (`PasswordEncoder`), вызвать `JwtUtil` и `RefreshTokenService`, вернуть `AuthResponse` с access/refresh — чтобы завершить основной поток входа.
  - `refreshToken`: найти refresh, проверить/роторовать, выдать новый access (и при необходимости новый refresh) — чтобы пользователь мог обновлять сессию.
  - `logout`: пометить refresh `revoked` (и удалить остальные по user) — чтобы завершать сессию.
  - `verifyRegistration`, `requestPasswordReset`, `resetPassword`: интегрировать с JWT (например, сброс повышает `tokenVersion`) — чтобы все пользовательские сценарии работали в stateless-модели.
- `controller/AuthController`: убедиться, что endpoints `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/register`, `/auth/verify`, `/auth/password/reset/*` принимают нужные DTO и возвращают корректные ответы/статусы — чтобы фронт имел стабильный API.
- DTO (`AuthResponse`, `LoginRequest`, `RefreshTokenRequest`, `MessageResponse`): дополнить полями `expiresIn`, `tokenType`, сообщениями об ошибках — чтобы клиент получал всю нужную информацию.

### Этап 6. SecurityConfig и инфраструктура безопасности
- `JWT/SecurityConfig`: настроить `SecurityFilterChain` (stateless, `csrf.disable()`, `authorizeHttpRequests` с разрешением `/auth/**`, запретом прочего), зарегистрировать `JwtFilter` перед `UsernamePasswordAuthenticationFilter`, определить `AuthenticationManager` и `BCryptPasswordEncoder` — чтобы Spring Security принимал JWT.
- `security/WebConfig`/CORS: разрешить домены фронтенда, включить `allowedHeaders`/`exposedHeaders` для `Authorization` — чтобы браузер мог отправлять/получать токены.
- Exception handling (`@ControllerAdvice` или `AuthenticationEntryPoint`): вернуть понятные ответы при 401/403 — чтобы клиенты понимали причину отказа.

### Этап 7. Тесты и проверка
- Unit-тесты `JwtUtil` (генерация, валидация), `RefreshTokenService` (создание/ротация), `AuthService` (login/refresh/logout, mock repositories) — чтобы покрыть бизнес-логику.
- Интеграционные MockMvc тесты для `AuthController`: регистрация → верификация → логин → доступ к защищённому ресурсу, refresh flow, logout — чтобы подтвердить end-to-end поток.
- (Опционально) нагрузочные/безопасностные проверки: множественные refresh, попытки с просроченными токенами — чтобы отловить узкие места.

### Этап 8. Операционные задачи
- Добавить мониторинг и аудит (`SecurityLogService`): количество логинов, неуспешных попыток, частота ротации refresh — чтобы наблюдать за безопасностью.
- Подготовить документацию (OpenAPI/Swagger, Postman коллекция) и чек-лист развертывания (переменные окружения, миграции) — чтобы команда могла быстро поднимать сервис.
- Продумать стратегию ротации `jwt.secret` и обновления `tokenVersion` (например, при смене пароля/compromise) — чтобы минимизировать последствия утечки ключей.
