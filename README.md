# iskacame.mk

**iskacame.mk** is a full-stack social gathering platform designed to simplify the entire lifecycle of planning group events. From creating a gathering and inviting participants, 
to coordinating schedules through time slot voting, choosing a venue with AI-powered place suggestions and group polls, communicating in real-time group chat, and settling shared 
expenses after the event - iskacame.mk brings everything into one place. The platform supports both email/password and Google OAuth authentication, delivers real-time notifications 
and message receipts via WebSocket, and provides an intuitive mobile-first experience through Expo.

## Features

- **Gatherings** - Create and manage group events with participants, time slot scheduling, and RSVP responses
- **Real-time Chat** - WebSocket-based group messaging with delivery/read receipts per chat room
- **Place Polling** - AI-powered place suggestions (Google Gemini) with voting to decide where to meet
- **Expense Splitting** - Track shared costs, split expenses among participants, record payments, and calculate simplified debts
- **Authentication** - Email/password signup with email verification, Google OAuth sign-in, password reset flow
- **Notifications** - Notifications for gathering updates (invites, status changes, poll results)
- **Media Gallery** - Upload and browse photos per gathering via Cloudinary
- **User Profiles** - Avatars, profile editing, account management

## Tech Stack

### Backend

| Technology | Purpose |
|---|---|
| Kotlin + Spring Boot 3.5 | REST API framework |
| Java 21 | Runtime |
| PostgreSQL 17 | Primary database |
| Redis 7 | Caching |
| RabbitMQ 3 | STOMP message broker for WebSocket relay |
| Spring Security + JWT | Authentication and authorization |
| Spring WebSocket | Real-time chat via STOMP |
| Google Gemini (Spring AI) | AI-powered place suggestions |
| Cloudinary | Image storage and delivery |
| MapStruct | DTO mapping |
| SpringDoc OpenAPI | API documentation (Swagger UI) |

### Frontend

| Technology | Purpose |
|---|---|
| React Native + Expo 54 | Cross-platform mobile framework |
| TypeScript | Type safety |
| Expo Router | File-based navigation |
| @stomp/stompjs | WebSocket client for real-time chat |
| React 19 | UI library |

## Project Structure

```
iskacame.mk/
├── iskacame-backend/          # Spring Boot API
│   ├── src/main/kotlin/       # Kotlin source
│   │   └── mk/ukim/finki/iskacamebackend/
│   │       ├── web/           # REST & WebSocket controllers
│   │       ├── service/       # Business logic (intf + impl)
│   │       ├── model/         # JPA entities and enums
│   │       ├── repository/    # Spring Data repositories
│   │       ├── dto/           # Request/response DTOs
│   │       ├── mapper/        # MapStruct mappers
│   │       ├── config/        # Security, WebSocket, CORS config
│   │       ├── security/      # JWT filter, STOMP auth interceptors
│   │       └── exception/     # Exception handlers
│   ├── Dockerfile
│   └── .env-example
├── iskacame-frontend/
│   └── iskacame.mk/           # Expo React Native app
│       ├── app/               # Screens (file-based routing)
│       │   ├── (auth)/        # Login, register, verify, forgot password
│       │   ├── (tabs)/        # Home, notifications, add gathering, profile
│       │   └── gathering/     # Gathering details, create, manage, gallery
│       ├── components/ui/     # Reusable components (chat, gathering UI)
│       ├── context/           # Auth context provider
│       ├── service/           # API clients and DTOs
│       ├── Dockerfile
│       └── Dockerfile.dev
├── docker-compose.local.yml   # Local development (builds from source)
├── docker-compose.prod.yml    # Production (pulls from Docker Hub)
└── .env.prod.example          # Environment variable template
```

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Node.js 20+ (for frontend development)
- Java 21+ (for backend development without Docker)
- Expo Go app on your phone (for mobile testing)

### Environment Setup

1. Copy the environment template at the project root:

```bash
cp .env.prod.example .env
```

2. Fill in all values in `.env`:

| Variable | Description |
|---|---|
| `PG_DB`, `PG_DB_USER`, `PG_DB_PASSWORD` | PostgreSQL database credentials |
| `JWT_SECRET_KEY` | Secret key for signing JWT tokens (64+ hex chars) |
| `JWT_EXPIRATION_MS` | Access token expiry (default: `86400000` = 24h) |
| `CLOUDINARY_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Cloudinary account for image uploads |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Gmail account + app password for sending verification emails |
| `VERIFICATION_TOKEN_EXPIRATION_MS` | Verification code expiry (default: `1800000` = 30min) |
| `ENCRYPTION_SECRET_KEY` | 32-char AES encryption key |
| `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` | RabbitMQ credentials |
| `GEMINI_API_KEY` | Google Gemini API key for AI place suggestions |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | Google OAuth 2.0 credentials |

3. Create the frontend `.env` at `iskacame-frontend/iskacame.mk/.env`:

```
EXPO_PUBLIC_API_URL=http://<your-machine-ip>:8090
EXPO_PUBLIC_GOOGLE_CLIENT_ID=<your-google-client-id>
```

### Quick Start

1. Copy these files to the target machine:
   - `docker-compose.prod.yml`
   - `.env` (filled with values)
   - `iskacame-backend/rabbitmq/enabled_plugins`
   - `iskacame-frontend/iskacame.mk/.env` (for api url and google oauth)

2. Start the application:

```bash
docker compose -f docker-compose.prod.yml up

# Start Expo dev server
cd iskacame-frontend/iskacame.mk
npm install
npx expo start
```

Scan the QR code with Expo Go on your Android or iOS device.

> **Note:** Set `EXPO_PUBLIC_API_URL` to your machine's local IP (e.g. `http://192.168.1.254:8090`), not `localhost`, so your phone can reach the backend.

| Service | Port |
|---|---|
| Frontend | 8081 |
| Backend API | 8090 |
| PostgreSQL | 5433 |
| RabbitMQ STOMP | 61613 |
| RabbitMQ Management | 15672 |
| Redis | 6379 |

## API Documentation

When the backend is running, Swagger UI is available at:

```
http://localhost:8090/swagger-ui/index.html
```

### Key API Endpoints

| Endpoint | Description |
|---|---|
| `POST /api/auth/signUp` | Register a new account |
| `POST /api/auth/signIn` | Login with email/username + password |
| `POST /api/auth/google` | Sign in with Google OAuth |
| `POST /api/auth/refresh` | Refresh access token |
| `GET /api/users/me` | Get current user profile |
| `PATCH /api/users/me/update` | Update profile |
| `GET /api/gatherings/me` | List user's gatherings |
| `POST /api/gatherings` | Create a gathering |
| `GET /api/gatherings/{id}` | Get gathering details |
| `POST /api/gatherings/{id}/poll` | Create a place poll |
| `POST /api/gatherings/{id}/expenses` | Add an expense |
| `GET /api/chat/room/{id}/messages` | Fetch chat messages |
| `GET /api/notifications` | Get notifications |

## Google OAuth Setup

1. Create a project in [Google Cloud Console](https://console.cloud.google.com/)
2. Enable the **Google+ API** and **Google Identity** services
3. Create an **OAuth 2.0 Client ID** (Web application type)
4. Add authorized redirect URIs:
   - `https://auth.expo.io/@<your-expo-username>/iskacame.mk` (for Expo Go)
   - `http://localhost:8081` (for web development)
5. Set the client ID and secret in both backend `.env` and frontend `.env`

## License

This project is developed at the Faculty of Computer Science and Engineering (FINKI), Ss. Cyril and Methodius University in Skopje.
