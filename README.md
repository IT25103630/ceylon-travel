# Ceylon Travel

> **Discover thoughtfully. Travel locally.**

Ceylon Travel helps visitors discover Sri Lankan destinations and plan trips with local guides. The platform brings destination listings, guide profiles, availability, bookings, messaging, reviews, a community gallery, support reports, and moderation into one web application.

Built with Java 17 and Spring Boot 3.5, the application serves a modular HTML, CSS, and JavaScript interface alongside a session-based JSON API.

## Contents

- [Features](#features)
- [Technology and requirements](#technology)
- [Get started](#get-started)
- [Demo accounts](#demo-accounts)
- [Configuration](#configuration-reference)
- [API reference](#api-reference)
- [Project structure](#project-structure)
- [Tests and contributing](#tests)

## Features

- Browse and search approved places and public guide profiles.
- Register as a tourist, guide, or community member; manage a profile and guide availability.
- Request and manage bookings with guide availability and date conflict checks.
- Start conversations with guides and exchange messages, with live server-sent events.
- Submit and manage reviews, community gallery images, and support or emergency reports.
- Use role-restricted administrative tools to moderate content, guides, users, and reports.
- Upload JPG and PNG images for use in the gallery and content submissions.

## Technology

- Java 17
- Spring Boot 3.5.16: Web MVC, JDBC, Security, and Validation
- Maven
- Microsoft SQL Server (default runtime configuration)
- Static HTML, CSS, and JavaScript; no frontend package manager is required
- H2 for the automated test database

## Requirements

Install or provide:

1. A 64-bit JDK 17 or later, with `JAVA_HOME` configured.
2. Maven 3.6.3 or later, or the Maven wrapper/distribution supplied by your development environment.
3. Microsoft SQL Server accessible to the application. The default configuration expects a server at `localhost:46065` and a database named `ceylon_travel`.

The Maven build downloads dependencies from Maven Central, so network access is needed for the first build. No Node.js installation is needed.

## Configure the database

The checked-in `src/main/resources/application.properties` contains the current local SQL Server URL, username, and password. Before running against your own database, create `ceylon_travel`, configure SQL Server to accept connections on the selected port, and update the datasource URL, username, and password in that file. The SQL Server driver is included in `pom.xml`. The project also declares MySQL/MariaDB drivers, but the supplied schema and default configuration target SQL Server.

The application initializes the database from `src/main/resources/schema.sql` (`spring.sql.init.mode=always`). Keep a suitable database backup when rerunning the app against data you need to retain. Demo data is enabled by default with `app.demo-data=true`; set it to `false` in `application.properties` to disable seed data.

For a shared or deployed environment, do not use the sample local credentials from the properties file. Supply credentials through your deployment's secret/configuration mechanism and avoid committing secrets.

## Get started

From the repository root:

```powershell
mvn spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080). The server port can be changed with the `PORT` environment variable. Maven downloads dependencies from Maven Central on the first build; Node.js is not needed.

To create and run the executable JAR:

```powershell
mvn clean package
java -jar target/ceylon-travel-1.0.0.jar
```

Uploaded images are stored in `uploads/` by default and served under `/media/`. Set `UPLOAD_DIR` to change the storage path. Uploads are limited to 5 MB per image and 6 MB per request; JPG and PNG files are validated before storage.

## Demo accounts

With the default `app.demo-data=true` setting, the app seeds these accounts only when the database has no users. They are for local demonstration only.

| Role | Username (email) | Password |
|---|---|---|
| Administrator | `admin@ceylon.test` | `Ceylon123!` |
| Tourist | `tourist@ceylon.test` | `Ceylon123!` |
| Guide | `guide@ceylon.test` | `Ceylon123!` |
| Guide | `dilini@ceylon.test` | `Ceylon123!` |
| Guide | `ravindu@ceylon.test` | `Ceylon123!` |
| Community member | `community@ceylon.test` | `Ceylon123!` |

Do not use demo credentials in a public deployment.

## Configuration reference

| Setting | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP server port |
| `spring.datasource.url` | `jdbc:sqlserver://localhost:46065;databaseName=ceylon_travel;encrypt=false;` | JDBC connection URL; edit in `application.properties` for your environment |
| `spring.datasource.username` | `sa` | Database login; replace for your environment |
| `spring.datasource.password` | `1234` | Database password; replace for your environment |
| `app.demo-data` | `true` | Whether demo seed data is enabled |
| `UPLOAD_DIR` | `uploads` | Local image upload directory |

Uploaded files are stored in `uploads/` by default and served at `/media/`. Uploads are limited to 5 MB per file and 6 MB per request. The current `application.properties` has local SQL Server settings; replace them for your database. For shared deployments, keep real database credentials in a secrets mechanism rather than publishing them in source control.

## API reference

All API routes use the `/api` prefix. JSON request bodies are expected where a body is shown. Routes marked **Session** require an authenticated session; state-changing requests require a valid CSRF token. Obtain the token from `GET /api/auth/csrf` and send it using the returned header name (normally `X-CSRF-TOKEN`). Login uses a form-encoded `POST /api/auth/login` with `username` (email) and `password`; logout is `POST /api/auth/logout`.

### Authentication and profile

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/auth/csrf` | Public | Get CSRF token and header name |
| GET | `/api/auth/me` | Public | Get the current user, or an empty object when signed out |
| POST | `/api/auth/register` | Public | Register a tourist, guide, or community account |
| POST | `/api/auth/login` | Public | Sign in with email and password (form data) |
| POST | `/api/auth/logout` | Session | Sign out |
| PUT | `/api/auth/profile` | Session | Update the current user's name and phone |
| GET | `/api/guides/profile` | Guide | Get the current guide profile |
| PUT | `/api/guides/profile` | Guide | Update guide details and rates |

### Places and guides

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/public/places?q=&category=` | Public | Search approved places; both filters are optional |
| GET | `/api/public/places/{id}` | Public | Get an approved place |
| GET | `/api/places/mine` | Session | List places submitted by the current user |
| POST | `/api/places` | Session | Submit a place for review |
| PUT | `/api/places/{id}` | Owner or admin | Edit a place |
| DELETE | `/api/places/{id}` | Owner or admin | Archive a place |
| GET | `/api/public/guides?q=` | Public | Search public guide profiles |
| GET | `/api/public/guides/{id}` | Public | Get a public guide profile |
| GET | `/api/public/guides/{id}/availability` | Public | Get a guide's availability |
| POST | `/api/availability` | Guide | Add an available date |
| DELETE | `/api/availability/{date}` | Guide | Remove an available date (`date` is ISO `YYYY-MM-DD`) |

### Bookings and chat

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/bookings` | Session | List bookings for the current user |
| POST | `/api/bookings` | Tourist | Request a guide booking |
| PATCH | `/api/bookings/{id}` | Session, role and ownership rules apply | Change booking status |
| GET | `/api/conversations` | Session | List current user's conversations |
| POST | `/api/conversations` | Tourist | Start a guide conversation with an initial message |
| GET | `/api/conversations/{id}/messages` | Conversation participant | List messages |
| POST | `/api/conversations/{id}/messages` | Conversation participant | Send a message |
| PUT | `/api/messages/{id}` | Message author | Edit a message |
| DELETE | `/api/messages/{id}` | Message author | Delete a message |
| GET | `/api/events` | Session, SSE | Subscribe to live events (`text/event-stream`) |

### Reviews, gallery, reports, and uploads

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/public/reviews?guide=&place=` | Public | List published reviews with optional filters |
| GET | `/api/reviews/mine` | Session | List current user's reviews |
| POST | `/api/reviews` | Tourist | Submit a review |
| PUT | `/api/reviews/{id}` | Owner | Edit a review |
| DELETE | `/api/reviews/{id}` | Owner or admin | Delete a review |
| GET | `/api/public/gallery?place=` | Public | List gallery images; place filter is optional |
| POST | `/api/gallery` | Tourist or guide | Add an uploaded image to the gallery |
| DELETE | `/api/gallery/{id}` | Owner or admin | Remove a gallery image |
| POST | `/api/uploads` | Session | Upload a JPG or PNG as multipart field `file`; returns its `/media/...` URL |
| GET | `/api/reports` | Session | List current user's reports |
| POST | `/api/reports` | Session | Submit an emergency, complaint, or support report |
| GET | `/api/notifications` | Session | List current user's notifications |
| POST | `/api/notifications/read` | Session | Mark notifications as read |

### Administration

All administration routes require the `ADMIN` role.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/admin/overview` | Get the moderation overview |
| PATCH | `/api/admin/places/{id}` | Approve or reject a place |
| PATCH | `/api/admin/guides/{id}` | Enable or disable guide verification |
| PATCH | `/api/admin/users/{id}` | Enable or disable a user |
| PATCH | `/api/admin/reviews/{id}` | Publish or reject a review |
| PATCH | `/api/admin/reports/{id}` | Update report status and response |

The API validates required fields and enforces roles, ownership, and workflow rules in its services/controllers. For the exact request DTO fields and allowed state transitions, see the corresponding controller under `src/main/java/lk/ceylontravel/controller/`.

## Project structure

```text
.
├── pom.xml
├── README.md
├── GIT_WORKFLOW.md
└── src
    ├── main
    │   ├── java/lk/ceylontravel
    │   │   ├── Application.java
    │   │   ├── config/        # Security and upload policy
    │   │   ├── controller/    # HTTP API endpoints
    │   │   ├── dao/           # SQL-backed data access
    │   │   ├── exception/     # API error handling
    │   │   ├── model/         # Domain models and access helpers
    │   │   ├── repository/    # Shared JDBC store
    │   │   └── service/       # Business rules and event dispatch
    │   └── resources
    │       ├── application.properties
    │       ├── schema.sql
    │       └── static/        # HTML shell, CSS, JS, images, vendor assets
    └── test
        ├── java/lk/ceylontravel/WorkflowTest.java
        └── resources/schema-test.sql
```

The static frontend is served from `src/main/resources/static/`. Its page shell is `index.html`; styles and browser modules are split under `css/` and `js/`.

## Tests

Run the test suite with:

```powershell
mvn test
```

`WorkflowTest` starts the application against an H2 in-memory database configured in MySQL compatibility mode, so tests do not require a running SQL Server instance.
