# Smart Task Management System API

A Spring Boot REST API demonstrating a full CI/CD pipeline with Jenkins, Docker, SonarQube, Trivy, and Prometheus/Grafana monitoring.

## Tech Stack

| Component     | Technology              |
|---------------|-------------------------|
| Backend       | Spring Boot 3.2         |
| Database      | PostgreSQL 15           |
| ORM           | Spring Data JPA         |
| Build Tool    | Maven                   |
| Security      | Spring Security + JWT   |
| Testing       | JUnit 5 + Mockito       |
| Containerize  | Docker / Docker Compose |
| CI/CD         | Jenkins                 |
| Code Quality  | SonarQube               |
| Security Scan | Trivy                   |
| Monitoring    | Prometheus + Grafana    |

## API Endpoints

### Auth
| Method | Endpoint              | Description       | Auth |
|--------|-----------------------|-------------------|------|
| POST   | /api/auth/register    | Register new user | No   |
| POST   | /api/auth/login       | Login, get JWT    | No   |

### Users
| Method | Endpoint         | Description      | Auth |
|--------|------------------|------------------|------|
| GET    | /api/users/{id}  | Get user profile | Yes  |
| GET    | /api/users       | List all users   | Yes  |

### Projects
| Method | Endpoint            | Description       | Auth |
|--------|---------------------|-------------------|------|
| POST   | /api/projects       | Create project    | Yes  |
| GET    | /api/projects       | List all projects | Yes  |
| GET    | /api/projects/{id}  | Get project       | Yes  |
| PUT    | /api/projects/{id}  | Update project    | Yes  |
| DELETE | /api/projects/{id}  | Delete project    | Yes  |

### Tasks
| Method | Endpoint                        | Description           | Auth |
|--------|---------------------------------|-----------------------|------|
| POST   | /api/tasks                      | Create task           | Yes  |
| GET    | /api/tasks                      | List all tasks        | Yes  |
| GET    | /api/tasks/{id}                 | Get task              | Yes  |
| PUT    | /api/tasks/{id}                 | Update task           | Yes  |
| DELETE | /api/tasks/{id}                 | Delete task           | Yes  |
| GET    | /api/tasks/project/{projectId}  | Tasks by project      | Yes  |

### Dashboard
| Method | Endpoint               | Description     | Auth |
|--------|------------------------|-----------------|------|
| GET    | /api/dashboard/summary | Summary metrics | Yes  |

### Monitoring
| Method | Endpoint                | Description           |
|--------|-------------------------|-----------------------|
| GET    | /actuator/health        | Health check          |
| GET    | /actuator/prometheus    | Prometheus metrics    |

## Quick Start

### Run with Docker Compose

```bash
# Build the jar first
mvn clean package -DskipTests

# Start all services
docker compose up -d
```

Services:
- API: http://localhost:8080
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090

### Run locally

```bash
# Start PostgreSQL
docker run -d -e POSTGRES_DB=taskdb -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password -p 5432:5432 postgres:15

# Run app
mvn spring-boot:run
```

## Example API Usage

### Register a user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","password":"secret123"}'
```

### Login and get token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"secret123"}' | jq -r .token)
```

### Create a project
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Project","description":"A test project"}'
```

### Create a task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"First Task","priority":"HIGH","status":"TODO","projectId":1}'
```

### Get dashboard summary
```bash
curl http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"
```

## CI/CD Pipeline Stages

```
Checkout → Build → Test → SonarQube → Quality Gate → Trivy Scan → Push → Deploy → Health Check → Tag Release
```

## Task Status Values
- `TODO`
- `IN_PROGRESS`
- `DONE`

## Task Priority Values
- `LOW`
- `MEDIUM`
- `HIGH`
