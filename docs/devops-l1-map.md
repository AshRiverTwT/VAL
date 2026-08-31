# DevOps L1 Concept Mapping

Every major component in this repository exists to teach (and let you practice) a specific DevOps L1
concept. Nothing here is decorative.

| Component in this project | DevOps Concept | How it's actually used here |
|---|---|---|
| `.git/`, feature branches, PRs | Version Control | All source changes flow through Git; see README section 5 for the full command set and merge-conflict exercise |
| GitHub repository | Remote Collaboration / Hosting | Remote for push/pull, pull requests, releases/tags |
| `pom.xml`, Maven lifecycle | Build Automation | `clean/compile/test/package/install` produce a reproducible JAR every time |
| JUnit 5 tests (`src/test/java`) | Automated Testing | Unit, startup, endpoint, and health tests gate every build; a failing test fails the whole pipeline |
| `spring-boot-maven-plugin` build-info | Build Metadata / Traceability | Embeds the Maven version into the JAR so `/info` never hardcodes it |
| `Dockerfile` | Containerization | Multi-stage build, non-root user, healthcheck, small runtime image |
| `.dockerignore` | Build Context Hygiene | Keeps the Docker build context small and free of secrets/docs |
| `docker-compose.yml` | Multi-Container Orchestration (local/single-host) | Wires `app` + `nginx` together with a shared network, env vars, healthchecks |
| `nginx/nginx.conf` | Reverse Proxy / Ingress | Terminates port 80, proxies to the app, centralizes logs, adds security headers |
| `systemd/valorant-app.service` | Process / Service Management | Alternate deployment: restart-on-failure, boot-time start, journald logging |
| AWS EC2 instance | Cloud Infrastructure | The actual compute the whole stack runs on |
| AWS Security Groups | Networking / Firewall | Least-privilege inbound rules (22, 80 only) |
| SSH (`.pem` key) | Secure Remote Access | How you administer the EC2 instance |
| `Jenkinsfile` | CI/CD Pipeline-as-Code | Declarative, version-controlled pipeline definition |
| Jenkins stages (Build/Test/Package/Docker/Deploy/Health/Cleanup) | Continuous Integration & Deployment | Automates the exact manual steps a human would otherwise run by hand |
| GitHub webhook -> Jenkins | Event-Driven Automation | Push-to-deploy without manual intervention |
| `APP_ENV`, `APP_VERSION`, `BUILD_NUMBER`, `GIT_COMMIT`, `SERVER_PORT` | Configuration Management | Externalized config - same artifact, different behavior per environment |
| `config/app.env.example` + `.gitignore` | Secrets Hygiene | Real config/secrets are never committed; only an example template is |
| `/health` endpoint | Health Monitoring | Used by Docker, Compose, Jenkins, and `scripts/health-check.sh` uniformly |
| `/info` endpoint | Observability / Diagnostics | Surfaces version, build, environment, hostname, container status live |
| Application logs (`logging.*` in `application.yml`) | Application Observability | Structured console/file logging with configurable levels per profile |
| Nginx access/error logs | Edge Observability | Separate from application logs; first place to check for 502s |
| `journalctl -u valorant-app` | Service Observability | systemd-mode log inspection, equivalent to `docker logs` for the container path |
| `ps`, `top`, `df`, `free`, `ss` | System Monitoring | Manual, Linux-native monitoring L1 relies on before reaching for Prometheus/Grafana |
| `scripts/backup.sh` | Release/Data Safety | Archives the JAR, config, and logs before risky changes |
| `scripts/rollback.sh` | Release Management | Switches back to a known-good previous artifact - distinct from restart/rebuild/redeploy |
| `scripts/cleanup.sh` | Resource Management | Prevents disk exhaustion from images, releases, and backups accumulating |
| `scripts/deploy.sh` | Deployment Automation | Single entry point for both Docker and systemd deployment paths |
| `docs/troubleshooting.md` | Incident Response Practice | Structured, realistic failure/diagnosis/fix exercises |
| Git tags (`v1.0.0`) | Release Versioning | Marks exactly which commit is running in production at any time |
