
```markdown
# 🎮 CSGO Items Viewer with Ktor + HTMX

A lightweight Kotlin Ktor + HTMX application to browse and search CSGO item data (e.g., skins, agents, crates, keys) with database persistence using PostgreSQL.

## ✨ Features

- Searchable endpoints by category (e.g., `/skins`, `/agents`, `/crates`, `/keys`)
- XML export support
- Basic Authentication for protected endpoints
- Fully dockerized
- Minimal setup — just run and go!

## 🐳 Dockerized Setup

You can run the project using **Docker** or **Docker Compose** without any manual setup.

### ✅ Using Docker Compose (Recommended)

```bash
docker-compose up -d --build
```

This will:
- Build the application image
- Start a PostgreSQL container (`csgo` DB)
- Start the Ktor server on `http://localhost:8085` *(or `http://0.0.0.0:8085` depending on your system)*

### ⚙️ Customizing Ports

By default, the app exposes port `8085`. You can change this in:
- `docker-compose.yml`: `ports: - "8085:8085"`
- `application.conf`: `port = 8085`

---

## 🧱 Project Structure

```
.
├── src/main/kotlin
│   ├── plugins/                # Ktor plugin configurations (e.g., database)
│   ├── routes/                 # Routing logic for item categories
│   ├── models/                 # Exposed table definitions
│   ├── pages/                  # HTMX HTML rendering
│   └── Application.kt          # Main application entry point
├── .env.default                # Environment variables
├── Dockerfile                  # Multi-stage Docker build
├── docker-compose.yml          # Compose file for running app + db
├── build.gradle.kts            # Gradle config
└── README.md                   # You're here!
```

---

## 📦 Environment Variables

The application uses the following `.env.default` file:

```env
LOOKUP_API_KEY=some_key
KTOR_DEVELOPMENT=true
AUTH_USER=admin
AUTH_PASSWORD=admin
DB_URL=jdbc:postgresql://db:5432/csgo
DB_USER=postgres
DB_PASSWORD=1234
```

These are loaded automatically inside the Docker container. If you're running locally, you can export them or copy `.env.default` to `.env.local`.

---

## 🔐 Authentication

The `/keys` endpoint is protected via **Basic Auth**.

- Username: `admin`
- Password: `admin`

You can change these values in the `.env.default` file and rebuild the container.

---

## 🗂 Example Routes

- `GET /skins` – list all skins
- `GET /agents` – list all agents
- `GET /crates` – list all crates
- `GET /keys` – list all keys (requires auth)
- `GET /skins/search?lookupValue=AK` – search skins
- `GET /xml?category=skin&lookupValue=AK` – export search results as XML

---

## 🧹 Rebuilding the App Only

If your database is already running and you only want to rebuild the app:

```bash
docker compose up -d --build --no-deps app
```

This skips rebuilding the DB container and speeds up deployment.

---

## 💬 Questions?

Feel free to reach out or open issues if you find bugs or have improvement ideas.

---

## 🧾 License

MIT – do whatever you want, but give credit if you can.
```

