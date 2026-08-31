# VALORANT Tactical Hub

A polished, static VALORANT-themed information site used as a **hands-on DevOps L1 practice lab**. The
application itself (Java + Spring Boot) is deliberately simple. The real subject matter is everything
around it: Git, Maven, Docker, Nginx, systemd, Jenkins CI/CD, and AWS EC2 - practiced end-to-end on a
single Ubuntu server, with a clear path to add more infrastructure later.

> This project is a non-commercial, fan-made practice project. It is not affiliated with or endorsed by
> Riot Games. No game artwork is reproduced; agents/maps are rendered with generated color badges instead
> of real artwork.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Technologies](#3-technologies)
4. [Local Setup](#4-local-setup)
5. [Git Workflow](#5-git-workflow)
6. [Maven Workflow](#6-maven-workflow)
7. [Testing](#7-testing)
8. [Docker](#8-docker)
9. [Docker Compose](#9-docker-compose)
10. [Nginx](#10-nginx)
11. [Linux Deployment (Ubuntu / EC2)](#11-linux-deployment-ubuntu--ec2)
12. [systemd](#12-systemd)
13. [Jenkins](#13-jenkins)
14. [AWS EC2](#14-aws-ec2)
15. [CI/CD](#15-cicd)
16. [Troubleshooting](#16-troubleshooting)
17. [DevOps L1 Concept Mapping](#17-devops-l1-concept-mapping)
18. [Practice Labs](#18-practice-labs)
19. [Interview Questions](#19-interview-questions)
20. [Future Extensions](#20-future-extensions)
21. [Local Setup - Exact Commands](#21-local-setup---exact-commands)
22. [GitHub Setup - Exact Commands](#22-github-setup---exact-commands)
23. [EC2 Setup - Exact Commands](#23-ec2-setup---exact-commands)
24. [Docker Deployment - Exact Commands](#24-docker-deployment---exact-commands)
25. [Jenkins Setup - Exact Commands](#25-jenkins-setup---exact-commands)
26. [CI/CD Flow](#26-cicd-flow)
27. [DevOps L1 Checklist](#27-devops-l1-checklist)
28. [Practice Sequence](#28-practice-sequence)
29. [Expected Final Architecture](#29-expected-final-architecture)

---

## 1. Project Overview

**VALORANT Tactical Hub** serves six static-content pages (Home, Agents, Maps, Weapons, Patch Notes,
About) plus a handful of small JSON endpoints. There is no database - content is served from in-memory
Java data classes, which keeps every layer of the DevOps pipeline (build, test, container, deploy) fast
and simple to reason about.

The point of the project is **not** to build a complex game site. It's to give you a real, deployable Java
application so you can practice:

- Version control (Git/GitHub)
- Build automation (Maven)
- Automated testing (JUnit)
- Containerization (Docker/Compose)
- Reverse proxying (Nginx)
- Process supervision (systemd)
- CI/CD (Jenkins)
- Cloud infrastructure (AWS EC2)
- Linux administration, Bash scripting, logging, monitoring, rollback

## 2. Architecture

```
                     ┌───────────────────────────────────────────────┐
                     │              AWS EC2 (Ubuntu)                 │
                     │                                                │
  Internet  ──80──▶  │  Security Group (22, 80)                      │
                     │        │                                      │
                     │        ▼                                      │
                     │  ┌───────────┐   proxy_pass    ┌────────────┐ │
                     │  │  Nginx    │ ───────────────▶ │ Spring Boot│ │
                     │  │  :80      │  (:8080 upstream) │  :8080     │ │
                     │  └───────────┘                  └────────────┘ │
                     │        ▲                                │      │
                     │        │                                ▼      │
                     │   access/error logs               app logs     │
                     │                                                │
                     │  Jenkins (localhost, not internet-exposed)     │
                     │  builds/tests/packages/deploys the app above   │
                     └───────────────────────────────────────────────┘
```

Two interchangeable deployment paths are provided for the same JAR:

- **Docker Compose** (primary): `nginx` + `app` containers on a shared Docker network.
- **systemd** (alternate): the JAR runs directly on the host as a supervised Linux service, with Nginx
  installed on the host instead of in a container.

## 3. Technologies

| Layer | Technology |
|---|---|
| Language / runtime | Java 17 (LTS) |
| Framework | Spring Boot 3 (Spring MVC, embedded Tomcat) |
| Build | Maven |
| Frontend | Static HTML/CSS/JavaScript (no frontend framework) |
| Testing | JUnit 5, Spring Boot Test (MockMvc) |
| Containerization | Docker, Docker Compose |
| Reverse proxy | Nginx |
| Process supervision | systemd |
| CI/CD | Jenkins (declarative pipeline) |
| Cloud | AWS EC2 (Ubuntu), Security Groups |
| Version control | Git, GitHub |
| Scripting | Bash |

## 4. Local Setup

Prerequisites: JDK 17+, Maven 3.9+, Git. See [section 21](#21-local-setup---exact-commands) for exact
commands. In short:

```bash
git clone <your-fork-url> valorant-devops-app
cd valorant-devops-app
mvn clean test        # compiles and runs all tests
mvn clean package      # produces target/valorant-devops-app.jar
java -jar target/valorant-devops-app.jar
# visit http://localhost:8080
```

## 5. Git Workflow

This project is meant to be practiced with real Git usage, not just `git init` once and forget it.
Commands you should be comfortable with by the end of this lab:

| Command | Purpose in this project |
|---|---|
| `git init` | Create the local repository (only if not cloning) |
| `git clone <url>` | Get a local copy of your GitHub repo |
| `git status` | See staged/unstaged changes before every commit |
| `git add <file>` | Stage changes |
| `git commit -m "..."` | Record a snapshot with a message |
| `git push` | Send commits to GitHub |
| `git pull` | Fetch + merge remote changes into your current branch |
| `git fetch` | Download remote changes without merging |
| `git branch` | List/create branches |
| `git checkout -b <name>` / `git switch -c <name>` | Create and move to a feature branch |
| `git log` | Inspect commit history |
| `git diff` | See exact line changes before staging/committing |
| `git reset` | Unstage files or move HEAD (use `--soft`/`--mixed`/`--hard` carefully) |
| `git revert <sha>` | Undo a commit by creating a new, opposite commit (safe on shared branches) |
| `git tag v1.0.0` | Mark a release commit |
| `git remote -v` | Inspect configured remotes |

**Realistic exercise - feature branch + merge:**

```bash
git switch -b feature/patch-note-update
# edit src/main/java/.../data/PatchNoteRepository.java - add a new PatchNote entry
git add src/main/java/com/valorant/devopshub/data/PatchNoteRepository.java
git commit -m "Add patch 9.11 sample entry"
git switch main
git pull
git merge feature/patch-note-update
git push
```

**Merge conflict exercise:** create two branches from `main` that both edit the same line of
`README.md`, merge the first one into `main`, then try merging the second - Git will report a conflict.
Open the file, look for `<<<<<<<` / `=======` / `>>>>>>>` markers, manually resolve, then
`git add README.md && git commit` to complete the merge.

## 6. Maven Workflow

```bash
mvn clean            # removes target/
mvn compile           # compiles src/main/java only
mvn test              # compiles + runs src/test/java (fails the build on any test failure)
mvn package           # runs tests, then packages target/valorant-devops-app.jar
mvn clean package      # the combination you'll use most often
mvn clean install      # package + install into your local ~/.m2 repository
```

The Maven lifecycle in this project, phase by phase:

1. **validate/compile** - `src/main/java` is compiled against Java 17.
2. **test** - JUnit 5 runs everything under `src/test/java`; a failing test stops the build here.
3. **package** - `spring-boot-maven-plugin` repackages the classes + dependencies into a single
   executable JAR (`target/valorant-devops-app.jar`) and embeds `META-INF/build-info.properties`
   (via the `build-info` goal), which is how `/info` reports the application version without it being
   duplicated anywhere else in the code.
4. **install** - copies the built JAR into your local Maven repository (rarely needed for this project
   since nothing else depends on this artifact).

The JAR is "executable" because Spring Boot repackages it with an embedded servlet container - you run it
directly with `java -jar`, no external application server required.

## 7. Testing

Run with `mvn test`. The suite includes:

- **Unit test** - `AgentRepositoryTest` (pure data validation, no Spring context, runs almost instantly)
- **Application startup test** - `DevopsHubApplicationTests` (`@SpringBootTest`, fails if the Spring
  context can't start - e.g. a misconfigured bean)
- **Endpoint tests** - `ContentApiControllerTest` (MockMvc against `/api/agents`, `/api/maps`,
  `/api/weapons`, `/api/patch-notes`, `/info`)
- **Health check test** - `HealthControllerTest` (asserts `/health` returns `200` and `{"status":"UP"}`)

**Intentional failure exercise (practice debugging a failed pipeline):** open
`src/test/java/com/valorant/devopshub/data/AgentRepositoryTest.java` and temporarily change:

```java
assertTrue(agents.size() >= 10, ...);
```

to:

```java
assertTrue(agents.size() >= 999, ...);
```

Run `mvn test` (or push and let Jenkins run it) and watch it fail with a clear assertion message. This is
exactly the kind of failure the Jenkins **Unit Tests** stage is designed to catch before a broken build
ever reaches Docker. Revert the change afterward with `git checkout -- <file>` or `git revert`.

## 8. Docker

The `Dockerfile` uses a two-stage build:

1. **build stage** (`maven:3.9.9-eclipse-temurin-17`) compiles and packages the JAR.
2. **runtime stage** (`eclipse-temurin:17-jre-alpine`) copies only the JAR into a slim image, runs as a
   non-root `valorant` user, and defines a `HEALTHCHECK` against `/health`.

```bash
docker build -t valorant-devops-app:local .
docker images
docker run -d --name valorant-app -p 8080:8080 -e APP_ENV=docker valorant-devops-app:local
docker ps
docker logs -f valorant-app
docker exec -it valorant-app sh
docker inspect valorant-app
docker stop valorant-app
docker start valorant-app
docker restart valorant-app
docker rm valorant-app
docker rmi valorant-devops-app:local
```

## 9. Docker Compose

`docker-compose.yml` defines two services on a shared bridge network (`valorant-net`):

- `app` - the Spring Boot container, **not** published to the host directly (only `expose`d on the
  internal network) - this forces all traffic through Nginx, matching the intended architecture.
- `nginx` - published on host port `80`, proxies to `app:8080`, depends on `app` being healthy.

```bash
docker compose up            # foreground, logs streaming
docker compose up -d          # detached
docker compose ps
docker compose logs -f app
docker compose logs -f nginx
docker compose restart app
docker compose down
```

Concepts in play: **services** (app, nginx), **networks** (`valorant-net`, isolates the pair from other
Docker workloads on the host), **volumes** (bind-mounted log directories under `docker/data/`),
**environment variables** (`APP_ENV`, `APP_VERSION`, `BUILD_NUMBER`, `GIT_COMMIT`, `APP_IMAGE_TAG`),
**port mapping** (`80:80` on the nginx service only), and **depends_on** with a `condition:
service_healthy` gate so Nginx doesn't start routing traffic before the app is actually ready.

## 10. Nginx

Nginx sits in front of the Spring Boot app and is the only thing directly reachable from the internet on
port 80. Reasons to use it here (see `nginx/nginx.conf`):

- terminates on the standard web port so the JVM never needs root/privileged-port access
- centralizes **access logs** and **error logs** in one place, separate from application logs
- adds basic security headers (`X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`)
- gives you a single place to add TLS/caching/rate-limiting later without touching Java code

**Troubleshooting exercises** (see [docs/troubleshooting.md](docs/troubleshooting.md) for full detail):
Nginx not running, wrong upstream port, app unavailable, port 80 already in use, and a broken
`nginx.conf` that fails `nginx -t`.

## 11. Linux Deployment (Ubuntu / EC2)

Assume the target is a fresh Ubuntu EC2 instance. Full command sequence in
[section 23](#23-ec2-setup---exact-commands). Diagnostic commands you'll use throughout this lab, and what
they're for **in this project specifically**:

| Command | Used for |
|---|---|
| `ps aux` | List running processes - confirm `java` or the container process is alive |
| `top` / `htop` | Live CPU/memory usage - spot a runaway process |
| `df -h` | Disk space - Docker images/logs can fill a small EC2 root volume |
| `du -sh <dir>` | Find what's consuming disk space (logs, old Docker images) |
| `free -h` | Available memory - the JVM needs headroom beyond `-Xmx` |
| `uptime` | Load average and how long the box has been up |
| `systemctl status/start/stop/restart/enable` | Manage `valorant-app.service` |
| `journalctl -u valorant-app -f` | Follow the app's logs when running under systemd |
| `ss -tulpn` | See which process is bound to which port (classic "port already in use" debug) |
| `curl -i http://localhost:8080/health` | Verify the app locally before blaming Nginx/network |
| `wget -qO- <url>` | Alternative to curl, useful on minimal images |
| `grep`, `cat`, `less`, `tail -f`, `head` | Read/search log files |
| `find` | Locate files (old releases, stray logs) |
| `chmod` / `chown` | Fix script executable bits and file ownership |
| `cp` / `mv` / `rm` / `mkdir` | General file management for releases/backups |
| `tar` | Create/extract the backup archives produced by `scripts/backup.sh` |

## 12. systemd

`systemd/valorant-app.service` is the **alternate** deployment path (Docker Compose is primary). It runs
the JAR directly on the host under a dedicated `valorant` system user, restarts automatically on failure,
and reads non-secret config from `config/app.env` via `EnvironmentFile=`.

```bash
sudo cp systemd/valorant-app.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable valorant-app
sudo systemctl start valorant-app
sudo systemctl status valorant-app
journalctl -u valorant-app -f
sudo systemctl restart valorant-app
sudo systemctl stop valorant-app
```

**Java process vs. systemd service vs. Docker container** - the same JAR, three different supervision
models:

- **Bare Java process**: `java -jar app.jar` in a terminal. Dies when the terminal closes or the process
  crashes. No automatic restart, no standard logging integration.
- **systemd service**: the OS supervises the process, restarts it on failure, starts it on boot, and
  routes its output to the journal - but it still runs directly on the host with host-level dependencies
  (whatever JDK is installed system-wide).
- **Docker container**: the process runs inside an isolated filesystem/network namespace with its exact
  runtime (JRE version, OS packages) bundled into the image; Docker (or Compose) supervises restarts
  instead of systemd, and `docker logs` replaces the journal.

## 13. Jenkins

Jenkins runs on the **same EC2 instance** as the application for this base lab (not exposed to the
internet - see [section 25](#25-jenkins-setup---exact-commands)). The `Jenkinsfile` defines a declarative
pipeline with these stages:

1. **Checkout** - pulls the repo, makes scripts executable
2. **Environment Info** - prints Java/Maven/Docker versions and the build number/commit
3. **Build** - `mvn clean compile`
4. **Unit Tests** - `mvn test` (pipeline fails here if any test fails)
5. **Package** - `mvn package -DskipTests`, archives the JAR
6. **Docker Build** - builds `valorant-devops-app:<BUILD_NUMBER>` and `:latest`
7. **Docker Image Validation** - runs the freshly built image on a throwaway port and health-checks it
   *before* touching the real deployment
8. **Deployment** - `scripts/deploy.sh --mode docker --tag <BUILD_NUMBER>`
9. **Health Check** - `scripts/health-check.sh` against the public Nginx URL
10. **Cleanup** - `scripts/cleanup.sh` (dangling images, build cache)

No credentials or secrets are hardcoded anywhere in the `Jenkinsfile` - `BUILD_NUMBER` and `GIT_COMMIT` are
supplied automatically by Jenkins itself.

## 14. AWS EC2

Core concepts you need for this lab:

- **EC2 instance** - a virtual machine in AWS; you SSH into it like any Linux box.
- **AMI** - the template (Ubuntu 22.04/24.04 LTS) your instance is created from.
- **Security Group** - a stateful virtual firewall attached to the instance. This project needs only:
  - `22/tcp` (SSH) - restrict the source to your own IP where possible
  - `80/tcp` (HTTP) - open to the internet so Nginx is reachable
  - Jenkins's port (commonly `8080`) is **not** opened publicly - see [section 16](#16-troubleshooting)
    and [section 25](#25-jenkins-setup---exact-commands) for secure alternatives (SSH tunnel).
- **Public/private IP** - the public IP is what you SSH to and what clients hit over the internet; the
  private IP is used for intra-VPC traffic (relevant once you add a second instance).
- **IAM** (basic) - the identity/permission system controlling who can manage the EC2 instance itself;
  this project doesn't need the *application* to call AWS APIs, so no instance role is required.
- **EBS** (basic) - the persistent block storage volume backing your instance's disk (`df -h` shows this).

## 15. CI/CD

- **Continuous Integration (CI)**: every push triggers an automated build + test cycle, catching breakage
  early. In this project: the Jenkins **Build** and **Unit Tests** stages.
- **Continuous Delivery**: every change that passes CI produces a deployable, validated artifact (the
  Docker image), but a human still decides when to actually deploy it.
- **Continuous Deployment**: the pipeline automatically deploys every change that passes all checks with
  no manual approval step. This project's Jenkinsfile implements continuous deployment - a passing
  pipeline deploys automatically via the **Deployment** stage.

See [section 26](#26-cicd-flow) for the full push-to-running flow and how to wire up a GitHub webhook.

## 16. Troubleshooting

See **[docs/troubleshooting.md](docs/troubleshooting.md)** for 15+ realistic, guided failure labs (symptoms,
investigation commands, hints, and solutions) covering Maven, Docker, Nginx, Jenkins, EC2 networking, and
more.

## 17. DevOps L1 Concept Mapping

See **[docs/devops-l1-map.md](docs/devops-l1-map.md)** for a full table mapping every component in this
repository to the DevOps concept it teaches.

## 18. Practice Labs

The project is structured into progressive levels - see [section 28](#28-practice-sequence) for the exact
lab order.

- **Level 1 - Fundamentals**: Git, Linux basics, Java, Maven, HTTP, EC2 basics
- **Level 2 - Build & Package**: Maven lifecycle, testing, JAR, env vars, Bash
- **Level 3 - Containers**: Docker, Dockerfile, Docker Compose
- **Level 4 - Web Infrastructure**: Nginx, reverse proxy, ports, logs, networking
- **Level 5 - CI/CD**: Jenkins, pipeline stages, GitHub webhook, automated tests
- **Level 6 - Operations**: systemd, health checks, monitoring, rollback, backup, troubleshooting
- **Level 7 - Advanced Extensions (optional, not required)**: second EC2 instance, load balancing, HTTPS,
  AWS ALB, Route 53, Terraform, Ansible, Kubernetes, Prometheus, Grafana

## 19. Interview Questions

See **[docs/interview-questions.md](docs/interview-questions.md)** for 50+ DevOps L1 interview questions
and answers, grounded directly in this project.

## 20. Future Extensions

Deliberately **not** required for the base project, but natural next steps once you're comfortable:

- A second EC2 instance (split Jenkins from the running application)
- An Application Load Balancer across two+ app instances
- HTTPS via Let's Encrypt / ACM + a real domain in Route 53
- Infrastructure as Code with Terraform
- Configuration management with Ansible
- Kubernetes (EKS or self-managed) as a container orchestration exercise
- Prometheus + Grafana for real metrics/dashboards instead of plain Linux commands

---

## 21. Local Setup - Exact Commands

```bash
# Prerequisites: JDK 17+, Maven 3.9+, Git
java -version
mvn -version
git --version

git clone <your-fork-url> valorant-devops-app
cd valorant-devops-app

mvn clean test
mvn clean package

java -jar target/valorant-devops-app.jar
# Now open http://localhost:8080
# In another terminal:
curl http://localhost:8080/health
curl http://localhost:8080/info
```

## 22. GitHub Setup - Exact Commands

```bash
# 1. Create an empty repository on GitHub (via the web UI), then locally:
git init
git add .
git commit -m "Initial commit: VALORANT Tactical Hub"
git branch -M main

# HTTPS remote:
git remote add origin https://github.com/<your-username>/valorant-devops-app.git

# OR SSH remote (requires an SSH key added to your GitHub account):
git remote add origin git@github.com:<your-username>/valorant-devops-app.git

git push -u origin main

# Everyday workflow:
git pull
git fetch
git status
git log --oneline --graph --decorate

# Releases:
git tag -a v1.0.0 -m "First working release"
git push origin v1.0.0
```

Pull requests: push a feature branch, open a PR on GitHub comparing it against `main`, review the diff,
then merge (prefer "Squash and merge" or "Merge commit" consistently across the project).

GitHub Actions is **not** the primary CI/CD system here - Jenkins is. If you want to compare the two, a
minimal `.github/workflows/ci.yml` running `mvn test` is a reasonable side-by-side exercise, but it should
not replace the Jenkins pipeline for this project.

## 23. EC2 Setup - Exact Commands

```bash
# From your local machine, SSH into the instance:
chmod 400 your-key.pem
ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>

# --- On the EC2 instance ---

# Update packages
sudo apt update && sudo apt upgrade -y

# Install Git
sudo apt install -y git

# Install Java 17
sudo apt install -y openjdk-17-jdk
java -version

# Install Maven
sudo apt install -y maven
mvn -version

# Install Docker
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
docker --version
docker compose version

# Install Nginx
sudo apt install -y nginx
sudo systemctl enable nginx

# Install Jenkins (Java must already be installed above)
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee \
  /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
  "https://pkg.jenkins.io/debian-stable binary/" | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install -y jenkins
sudo usermod -aG docker jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins

# Clone the project
git clone <your-fork-url> valorant-devops-app
cd valorant-devops-app
chmod +x scripts/*.sh
```

Security group (configure via the AWS Console or CLI) should allow only:

```
22/tcp  (SSH)   - source: your IP or a bastion, not 0.0.0.0/0 if avoidable
80/tcp  (HTTP)  - source: 0.0.0.0/0
```

Do **not** open Jenkins's port (8080 by default) to the internet. Access the Jenkins UI via an SSH tunnel
instead:

```bash
# From your local machine:
ssh -i your-key.pem -L 8080:localhost:8080 ubuntu@<EC2_PUBLIC_IP>
# Now open http://localhost:8080 in your local browser
```

## 24. Docker Deployment - Exact Commands

```bash
cd valorant-devops-app

# Build the app + tag the image
./scripts/build.sh --tag local

# Bring the full stack up (Nginx + app)
./scripts/deploy.sh --mode docker --tag local

# Verify
curl http://localhost/health
curl http://<EC2_PUBLIC_IP>/health

docker compose ps
docker compose logs -f app
```

## 25. Jenkins Setup - Exact Commands

```bash
# Get the initial admin password (first-time setup only)
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# Via the SSH tunnel from section 23, open http://localhost:8080, paste the
# password, install suggested plugins, create your admin user.

# In the Jenkins UI:
#   New Item -> Pipeline -> name it "valorant-devops-app"
#   Pipeline section -> Definition: "Pipeline script from SCM"
#   SCM: Git, Repository URL: your GitHub repo URL
#   Script Path: Jenkinsfile
#   Save, then click "Build Now" for a manual first run.
```

**GitHub webhook (for automatic builds on push):**

1. In GitHub: repo Settings -> Webhooks -> Add webhook
2. Payload URL: `http://<EC2_PUBLIC_IP>:8080/github-webhook/` (or through a secure tunnel/reverse proxy
   path if you choose not to expose Jenkins directly - see below)
3. Content type: `application/json`
4. In the Jenkins job: enable "GitHub hook trigger for GITScm polling"

If you don't want to expose Jenkins's port publicly at all, prefer one of these secure options instead of
opening it to the internet:
- Poll SCM on a schedule (`H/5 * * * *`) instead of using a webhook
- Put Jenkins behind the same Nginx instance on a path with authentication, only reachable over a VPN/SSH
  tunnel
- Use a reverse SSH tunnel or a managed webhook relay so GitHub never talks directly to your EC2 instance

## 26. CI/CD Flow

```
GitHub (push to main or a feature branch)
   │
   ▼
Jenkins job triggered (webhook or manual "Build Now")
   │
   ▼
Checkout ──▶ Environment Info ──▶ Build ──▶ Unit Tests ──▶ Package
                                                              │
                                                              ▼
                                                        Docker Build
                                                              │
                                                              ▼
                                              Docker Image Validation
                                                              │
                                                              ▼
                                                        Deployment
                                                     (scripts/deploy.sh)
                                                              │
                                                              ▼
                                                        Health Check
                                                              │
                                                              ▼
                                                          Cleanup
                                                              │
                                                              ▼
                                        Application running on EC2, behind Nginx
```

If **any** stage fails (compile error, failing test, bad Docker build, failed deploy, failed health check),
the pipeline stops and is marked failed - it will not silently continue to deploy a broken build.

## 27. DevOps L1 Checklist

- [ ] Cloned the repo and ran it locally with `mvn` + `java -jar`
- [ ] Made a feature branch, committed, and merged it back into `main`
- [ ] Caused and resolved a merge conflict
- [ ] Tagged a release (`v1.0.0`)
- [ ] Ran the full Maven lifecycle (`clean`, `compile`, `test`, `package`, `install`)
- [ ] Intentionally broke a test and watched it fail, then fixed it
- [ ] Built and ran the Docker image manually
- [ ] Brought the stack up with `docker compose up -d` and verified through Nginx
- [ ] Deployed to a real Ubuntu EC2 instance
- [ ] Ran the app as a systemd service (alternate path) and inspected `journalctl`
- [ ] Configured and tested the Nginx reverse proxy, including a deliberately broken config
- [ ] Set up Jenkins, ran the pipeline, and watched it fail on a bad commit and pass after a fix
- [ ] Configured a GitHub webhook (or documented why you chose polling instead)
- [ ] Practiced at least 5 Linux diagnostic commands (`ps`, `top`, `df`, `free`, `journalctl`, `ss`, etc.)
- [ ] Performed a backup and a rollback
- [ ] Worked through at least 5 exercises in `docs/troubleshooting.md`

## 28. Practice Sequence

1. **Git basics** - clone, branch, commit, merge, resolve a conflict, tag a release
2. **Local Java/Maven** - build, test, package, run the JAR directly
3. **Break and fix a test** - the intentional-failure exercise in [section 7](#7-testing)
4. **Bash scripting** - read and run every script in `scripts/`, understand exit codes and logging
5. **Docker basics** - build the image, run it standalone, inspect logs/health
6. **Docker Compose** - bring up the full app + Nginx stack locally
7. **Nginx** - break the upstream port on purpose, observe a 502, fix it
8. **AWS EC2** - launch the instance, configure the security group, SSH in
9. **Linux server setup** - install Java/Maven/Git/Docker/Nginx on EC2
10. **systemd** - run the app as a Linux service instead of Docker, compare the two
11. **Deploy to EC2 manually** - `scripts/build.sh` + `scripts/deploy.sh` over SSH
12. **Monitoring basics** - `top`, `df`, `free`, `journalctl`, `docker logs`, `/health`
13. **Backup and rollback** - `scripts/backup.sh`, then `scripts/rollback.sh`
14. **Jenkins setup** - install Jenkins on the same EC2 instance, run the pipeline manually
15. **GitHub webhook** - trigger Jenkins automatically from a push
16. **Full CI/CD** - push a real change end-to-end and watch it deploy automatically
17. **Troubleshooting lab** - work through `docs/troubleshooting.md` deliberately breaking things
18. **(Optional) Advanced extensions** - pick from [section 20](#20-future-extensions)

## 29. Expected Final Architecture

```
                                   ┌─────────────────────────┐
                                   │        GitHub           │
                                   │  (source of truth, PRs, │
                                   │   tags/releases)         │
                                   └────────────┬─────────────┘
                                                │ push / webhook
                                                ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                         AWS EC2 Instance (Ubuntu)                         │
│                                                                             │
│   ┌───────────────┐         builds / tests / deploys        ┌──────────┐  │
│   │   Jenkins     │ ─────────────────────────────────────▶  │  Docker  │  │
│   │ (localhost)   │                                         │  Engine  │  │
│   └───────────────┘                                         └────┬─────┘  │
│                                                                    │        │
│                                                     ┌──────────────┴─────┐ │
│                                                     │  valorant-net      │ │
│                                                     │  (Docker network)  │ │
│                                                     │                    │ │
│  Internet ──80──▶ Security Group ──▶ ┌───────────┐  │  ┌──────────────┐ │ │
│                                       │  Nginx    │──┼─▶│ Spring Boot  │ │ │
│                                       │  :80      │  │  │ app :8080    │ │ │
│                                       └───────────┘  │  └──────────────┘ │ │
│                                                     └────────────────────┘ │
│                                                                             │
│   Alternate path: systemd runs the same JAR directly on the host,          │
│   with host-installed Nginx in front, instead of the Docker network.       │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## Repository Structure

```
valorant-devops-app/
├── src/
│   ├── main/java/com/valorant/devopshub/
│   │   ├── DevopsHubApplication.java
│   │   ├── config/AppInfoProperties.java
│   │   ├── controller/ (ContentApiController, HealthController, InfoController)
│   │   ├── data/ (Agent/Map/Weapon/PatchNote repositories)
│   │   ├── model/ (Agent, Ability, GameMap, Weapon, PatchNote)
│   │   └── service/RuntimeInfoService.java
│   ├── main/resources/
│   │   ├── application.yml, application-dev.yml, application-prod.yml
│   │   └── static/ (index.html, agents.html, maps.html, weapons.html,
│   │       patch-notes.html, about.html, css/, js/)
│   └── test/java/com/valorant/devopshub/ (unit, startup, endpoint, health tests)
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── README.md
├── .gitignore
├── .dockerignore
├── scripts/ (build.sh, deploy.sh, health-check.sh, backup.sh, rollback.sh, cleanup.sh, lib.sh)
├── nginx/nginx.conf
├── systemd/valorant-app.service
├── docker/README.md
├── config/app.env.example
└── docs/ (troubleshooting.md, devops-l1-map.md, interview-questions.md)
```
