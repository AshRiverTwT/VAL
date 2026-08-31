# DevOps L1 Interview Questions

50+ questions and answers, grounded directly in this project. Use them for self-testing after completing
the practice labs in the README.

## Git & GitHub

**1. What is Git, and why does this project use it?**
Git is a distributed version control system that tracks every change to the codebase as a series of
commits. It lets multiple people (or your future self) see exactly what changed, when, and why, and lets
you safely experiment on branches without risking the working `main` branch. In this project, Git also
drives the CI/CD pipeline: Jenkins builds whatever commit is checked out, and Git tags mark exactly which
commit is running in production.

**2. What's the difference between `git fetch` and `git pull`?**
`git fetch` downloads new commits from the remote but does not touch your working branch or files - it
just updates your local view of the remote. `git pull` does a fetch and then immediately merges (or rebases)
those changes into your current branch. Using `fetch` first lets you inspect incoming changes (e.g. with
`git log origin/main`) before merging them in.

**3. What is a merge conflict, and how do you resolve one?**
A merge conflict happens when two branches change the same lines of the same file differently, and Git
can't automatically decide which version to keep. Git marks the conflicting sections in the file with
`<<<<<<<`, `=======`, and `>>>>>>>` markers; you manually edit the file to the correct final content,
remove the markers, then `git add` the file and complete the merge with `git commit`. This project's README
includes a hands-on exercise for creating and resolving one deliberately.

**4. What's the difference between `git reset` and `git revert`?**
`git reset` moves your branch pointer (and optionally your working files) to an earlier commit, effectively
rewriting history - risky on a shared branch others have already pulled. `git revert` creates a *new*
commit that undoes the changes of a previous commit, leaving history intact and safe to push to a shared
branch. For this project, `revert` is the safer default once code has been pushed and possibly deployed.

**5. What is a feature branch workflow, and why use it here?**
Instead of committing directly to `main`, you create a separate branch for each change (e.g.
`feature/patch-note-update`), commit there, and merge back into `main` once it's ready. This keeps `main`
always in a deployable state, since Jenkins builds and deploys from it, and lets you review a diff before
merging. It also isolates in-progress, possibly broken work from the branch Jenkins is watching.

**6. What happens when you push code to GitHub?**
Your local commits are uploaded to the remote repository, updating the branch reference there so anyone
else who fetches/pulls sees the same history. If a GitHub webhook is configured (as described in the
README's Jenkins section), GitHub also sends an HTTP POST notification to Jenkins, which triggers a new
pipeline run automatically. If no webhook exists, the push just sits on GitHub until something (a person or
a scheduled poll) picks it up.

**7. What is a pull request, and why use one instead of pushing straight to `main`?**
A pull request (PR) proposes merging one branch into another and gives reviewers a chance to see the full
diff, leave comments, and approve or request changes before the merge happens. It creates a natural
checkpoint for code review and catches mistakes before they reach the branch Jenkins deploys from. GitHub
also shows CI status (if configured) directly on the PR, so you know a change is safe to merge.

## Linux

**8. How do you check what processes are running on a Linux server?**
`ps aux` lists all running processes with their PID, CPU/memory usage, and command line - useful for
confirming the `java` process (or `docker-proxy`/containerd processes) is actually running. You can narrow
it with `ps aux | grep java`. `top`/`htop` (see next question) show the same information live and
continuously updating.

**9. What's the difference between `top` and `htop`?**
`top` is a built-in, no-dependency tool that shows live CPU/memory usage per process in a text UI. `htop` is
an enhanced, usually not preinstalled, alternative with color, scrolling, and easier process
management (kill, renice) via function keys. Both are used the same way in this project: to spot a runaway
JVM process or confirm memory headroom before deploying.

**10. How do you check disk usage on the EC2 instance?**
`df -h` shows free/used space per mounted filesystem in human-readable units - the first thing to check
when a deploy fails with "no space left on device." `du -sh <directory>` then drills into *which* directory
is consuming space (commonly Docker's image storage or `scripts/backups/` in this project).

**11. How would you find out what process is using port 8080?**
`ss -tulpn | grep 8080` lists listening sockets with the owning process ID and name. This directly answers
the "port already in use" failure documented in `docs/troubleshooting.md` - once you have the PID, you can
decide whether to stop it or run your process on a different port.

**12. What's the difference between `journalctl -u valorant-app` and `tail -f app.log`?**
`journalctl -u valorant-app` reads the systemd journal for a specific service - it's the right tool when the
app is deployed via `systemd/valorant-app.service`. `tail -f` follows a plain text log file in real time and
works regardless of how the process is supervised (including inside a container via a bind-mounted log
file). Both let you watch logs live; which one you use depends on the deployment path (systemd vs.
file-based logging).

**13. Why do the scripts in this project need `chmod +x`?**
On Linux, a file needs the executable permission bit set before it can be run directly (e.g. `./deploy.sh`);
Git doesn't always preserve this bit across clones/checkouts on every platform. `chmod +x scripts/*.sh` sets
that bit for every script, which is why the Jenkinsfile's Checkout stage runs it explicitly before any
script is invoked.

**14. How do you check available memory on the server?**
`free -h` shows total, used, free, and available memory (and swap) in human-readable units. This matters
for this project because the JVM needs headroom beyond its own heap (`-Xmx`) for metaspace, thread stacks,
and OS-level buffers - a small EC2 instance can run out of memory if you deploy too many other services
alongside it.

## Maven / Java Build Process

**15. What happens during `mvn package`?**
Maven runs every lifecycle phase up to and including `package`: it compiles `src/main/java`, compiles and
runs all tests in `src/test/java` (failing the build if any test fails), and then packages the compiled
classes and resources into a JAR file. Because this project uses `spring-boot-maven-plugin`'s `repackage`
goal, that JAR is further repackaged into an executable "fat" JAR containing an embedded servlet container
and all dependencies.

**16. What's the difference between the `compile`, `test`, and `package` Maven phases?**
`compile` only turns `src/main/java` into `.class` files and does not touch tests. `test` additionally
compiles and executes everything in `src/test/java`, failing the build on any test failure. `package` runs
everything `test` does, then bundles the result into a distributable artifact (a JAR here) - each phase
includes all the work of the phases before it.

**17. What is an executable JAR, and how is this project's one built?**
A normal JAR just contains compiled classes and resources and needs an external classpath and application
server to run. An executable ("fat"/"uber") JAR bundles the application *and* all its dependencies *and* an
embedded server, so it can be run standalone with `java -jar app.jar`. This project's `pom.xml` configures
`spring-boot-maven-plugin`'s `repackage` goal to produce exactly that as `target/valorant-devops-app.jar`.

**18. Why does the Maven build run tests before packaging?**
Running tests before packaging ensures a broken JAR is never produced or deployed in the first place - if
you packaged before testing, you could accidentally ship an artifact that's already known to be broken.
This ordering is a core reason `mvn test` failing stops the entire pipeline before the Docker Build stage
ever runs.

**19. What is dependency management in Maven, and where is it configured here?**
Maven resolves and downloads the libraries your project needs (declared in `<dependencies>` in `pom.xml`)
from repositories (Maven Central by default), caching them in your local `~/.m2` repository. This project
inherits version management from `spring-boot-starter-parent`, so dependencies like `spring-boot-starter-web`
don't need explicit version numbers - the parent BOM keeps them all compatible.

**20. Why does the whole build fail if a single test fails?**
Maven's Surefire plugin returns a non-zero exit code when any test fails, and Maven treats that as a fatal
build error by default, stopping before later phases (package, install) run. This "fail fast" behavior is
intentional: it prevents a build with known-broken behavior from silently continuing to packaging,
Docker build, and deployment.

## CI/CD & Jenkins

**21. What's the difference between Continuous Integration, Continuous Delivery, and Continuous
Deployment?**
Continuous Integration (CI) means every code change is automatically built and tested, catching integration
issues early - the Jenkinsfile's Build and Unit Tests stages. Continuous Delivery goes further: every change
that passes CI produces a deployable artifact (a validated Docker image here), but a human still decides
when to actually release it. Continuous Deployment removes that manual gate entirely - this project's
pipeline implements Continuous Deployment, since a passing build deploys automatically via the Deployment
stage.

**22. How does Jenkins deploy this application?**
After Checkout, Build, Unit Tests, Package, and Docker Build/Validation all succeed, the pipeline's
Deployment stage runs `scripts/deploy.sh --mode docker --tag <BUILD_NUMBER>`, which updates the Docker
image tag used by `docker-compose.yml` and runs `docker compose up -d`. It then runs
`scripts/health-check.sh` against the public Nginx URL to confirm the new deployment is actually serving
traffic correctly before marking the pipeline successful.

**23. How would you troubleshoot a failed Jenkins pipeline?**
Open the failed build's Console Output and read from the first error, not the summary at the bottom -
Jenkins often prints a generic "exit code 1" message after the real error has already scrolled by. Match the
failing stage to the underlying shell command in the `Jenkinsfile` and try running that exact command
manually on the server to reproduce it outside Jenkins. Once reproduced, it's usually the same class of
issue documented in `docs/troubleshooting.md` (compilation, tests, Docker, health check, etc.).

**24. What is a Jenkinsfile, and why keep it in version control?**
A Jenkinsfile is a text file (using Groovy-based declarative syntax) that defines an entire CI/CD pipeline
as code - stages, steps, environment, and post-build actions. Storing it in the same Git repository as the
application means pipeline changes are reviewed, versioned, and tied to the exact commit they apply to,
instead of living as fragile manual configuration inside the Jenkins UI.

**25. Why does the pipeline validate the Docker image before deploying it?**
The "Docker Image Validation" stage runs the freshly built image on a throwaway port and health-checks it
*before* touching the real running deployment. This catches a broken image (bad entrypoint, missing
environment variable, crash on startup) without causing any downtime for the currently running, working
version.

**26. What is a GitHub webhook, and how does it trigger Jenkins?**
A webhook is a configuration on GitHub that sends an HTTP POST request to a specified URL whenever a chosen
event happens (like a push). Pointed at Jenkins's `/github-webhook/` endpoint, a push to the repository
causes GitHub to notify Jenkins immediately, which starts a new pipeline run - removing the need to
manually click "Build Now" or wait for scheduled polling.

**27. Why shouldn't Jenkins be exposed directly to the internet in this project?**
Jenkins can execute arbitrary code (that's its entire purpose) and often holds credentials for deployment -
exposing its web UI/port directly to the internet significantly widens the attack surface. This project's
Security Group only opens ports 22 and 80; Jenkins is reached instead via an SSH tunnel (or, later, a
authenticated path behind Nginx), keeping the same functionality without public exposure.

**28. Why does the pipeline archive the built JAR as an artifact?**
`archiveArtifacts` in the Package stage stores the exact JAR produced by that build inside Jenkins itself,
tied to that specific build number. This gives you a durable, retrievable record of precisely what was
built and tested for any past build, independent of whether that Git commit or Docker image is still around
locally.

## Docker & Docker Compose

**29. What's the difference between a Docker image and a Docker container?**
An image is a read-only, versioned template - the result of `docker build` - containing the application and
everything it needs to run. A container is a running (or stopped) instance of that image, with its own
writable layer, network namespace, and process. You can run many containers from the same image, exactly
like multiple JVM processes could be started from the same JAR file.

**30. Why does the Dockerfile use a multi-stage build?**
The first stage uses a full Maven+JDK image to compile and package the application, which is large and
includes tools never needed at runtime. The second stage copies only the finished JAR into a much smaller
JRE-only base image, so the final image that actually gets deployed and pulled doesn't carry the entire
build toolchain - smaller images pull faster and have a smaller attack surface.

**31. What happens if the Docker container crashes?**
Docker (or, under Compose, the `restart: unless-stopped` policy) can automatically restart the container,
but the crash itself will show up in `docker ps -a` as an `Exited` status with a non-zero exit code, and
`docker logs <name>` will show the stack trace that caused it. Compose's dependent services (like Nginx,
via `depends_on: condition: service_healthy`) won't route traffic to it again until its healthcheck passes.

**32. Why does the container run as a non-root user?**
Running as root inside a container means that if an attacker ever manages to escape the application process
(e.g. via a vulnerability), they have root privileges inside that container's namespace, which is a larger
blast radius than a limited user. The Dockerfile creates a dedicated `valorant` user and switches to it with
`USER valorant` before running the JAR, following the principle of least privilege.

**33. What's the purpose of the Docker `HEALTHCHECK` instruction?**
It tells Docker how to actively probe whether the containerized process is not just running, but actually
working correctly - here, by curling `/health` on an interval. Docker surfaces the result in `docker ps`
(as `healthy`/`unhealthy`) and Compose can use it to gate dependent services, like Nginx not routing to an
app container that hasn't passed its healthcheck yet.

**34. What is `docker-compose.yml` used for in this project?**
It declaratively defines and wires together the two services this project needs - `app` and `nginx` - on a
shared network, with environment variables, port mappings, volumes, and health-based startup ordering all
in one version-controlled file. Instead of remembering a long sequence of `docker run` commands with
matching flags, `docker compose up -d` recreates the entire stack consistently every time.

**35. What's the difference between `expose` and `ports` in Docker Compose?**
`expose` makes a container's port reachable to *other containers on the same Docker network* but does not
publish it to the host machine at all. `ports` (used only on the `nginx` service here, as `80:80`)
additionally binds the port on the host itself, making it reachable from outside Docker - which is why only
Nginx, not the `app` service, is reachable directly from the internet.

**36. What is a Docker network, and why does this project define one?**
A Docker network is a virtual network that containers can be attached to so they can resolve and reach each
other by service name (e.g. `app` resolves to the app container's internal IP). `docker-compose.yml` defines
a dedicated `valorant-net` bridge network so `nginx` can proxy to `http://app:8080` without either container
needing to know the other's actual IP address, and without interfering with unrelated containers on the same
host.

## AWS / EC2

**37. What is an EC2 instance?**
EC2 (Elastic Compute Cloud) is AWS's virtual machine service - an EC2 instance is a single virtual server you
provision, choosing its OS image, size (CPU/memory), storage, and network settings. This project's entire
stack (Nginx, the app container or JAR, and Jenkins) runs on one such Ubuntu EC2 instance for the base lab.

**38. What is a Security Group, and how is it configured for this project?**
A Security Group is a stateful virtual firewall attached to an EC2 instance's network interface, controlling
inbound and outbound traffic by port/protocol/source. This project's Security Group allows only `22/tcp`
(SSH, ideally restricted to your IP) and `80/tcp` (HTTP, open to the internet) inbound - notably, Jenkins's
port is deliberately *not* opened.

**39. What's the difference between an instance's public and private IP address?**
The public IP is reachable from the internet and is what you SSH to or point a browser at; it can change if
the instance stops/starts unless you attach an Elastic IP. The private IP is only reachable from within the
same VPC and is what you'd use for instance-to-instance traffic if you later add a second EC2 instance (e.g.
a database server) that shouldn't be internet-reachable at all.

**40. What is an AMI?**
An AMI (Amazon Machine Image) is the template used to launch an EC2 instance - it defines the OS, initial
software, and configuration the instance starts with. This project assumes an official Ubuntu Server LTS AMI
as the starting point, onto which Java, Maven, Docker, Nginx, and Jenkins are installed manually via the
commands in the README.

**41. Why shouldn't Jenkins's port be opened in the Security Group?**
Opening it would expose a powerful automation tool with code-execution capability directly to internet
scanners and attackers, which is a disproportionate risk for a personal lab. Instead, this project accesses
Jenkins through an SSH tunnel (`ssh -L 8080:localhost:8080 ...`), keeping all the same functionality without
adding an internet-facing attack surface.

**42. What is EBS, and how does it relate to disk space troubleshooting?**
EBS (Elastic Block Store) is the persistent, network-attached block storage that backs an EC2 instance's
root (and any additional) volumes - what `df -h` reports on. If a deployment starts failing with "no space
left on device," you're hitting the limit of the attached EBS volume size, which you can resolve by cleaning
up (see `scripts/cleanup.sh`) or resizing the volume in the AWS Console.

## Networking & Nginx

**43. The app works at `localhost:8080` but not through the public IP - how do you diagnose it?**
Work outward one layer at a time: confirm the app answers on `localhost:8080` on the instance itself (proves
the app is fine), then `localhost:80` (proves Nginx and its upstream config are fine), then finally the
public IP from an *external* machine. Whichever step first fails tells you exactly where the problem is -
usually either the Security Group blocking port 80 externally, or Nginx misconfigured/not running. This
exact exercise is documented in `docs/troubleshooting.md`.

**44. Why would Nginx return a 502 Bad Gateway?**
A 502 means Nginx itself is running and received the request, but got an invalid or no response from the
upstream it proxies to (the `app` service). Common causes here: the app container crashed or hasn't finished
starting, or Nginx is configured to proxy to the wrong host/port. Checking `docker compose logs app` and
curling the app directly on its own port quickly confirms which side is at fault.

**45. What is a reverse proxy, and why does this project use one?**
A reverse proxy sits in front of one or more backend servers and forwards client requests to them, returning
the backend's response as if it came from the proxy itself. Nginx does this here so the JVM never needs to
bind a privileged port (80) directly, so access/error logging is centralized in one place, and so TLS or
caching could be added later without touching the Java application at all.

**46. What's the difference between HTTP and HTTPS in the context of this project?**
HTTP transmits requests and responses in plain text, so anything in transit (in principle) could be read or
tampered with. HTTPS adds a TLS layer that encrypts that traffic and verifies the server's identity via a
certificate. This base project intentionally uses plain HTTP for simplicity (see the README's advanced
extensions); adding HTTPS via Let's Encrypt/ACM is a documented Level 7 extension, not part of the base lab.

**47. Does this project use DNS, and what is DNS in general?**
DNS (Domain Name System) translates human-readable domain names into IP addresses. The base project is
accessed directly by the EC2 instance's public IP, so DNS isn't required; adding a real domain name via
Route 53 (pointing at that IP, or at a load balancer in a multi-instance setup) is listed as an optional
Level 7 extension.

**48. What are HTTP status codes, and which ones matter most for this project?**
HTTP status codes are three-digit numbers returned with every response indicating the outcome: `2xx` for
success (e.g. `200 OK` from `/health`), `4xx` for a client-side problem (e.g. `404 Not Found` for an unknown
path), and `5xx` for a server-side problem (e.g. `502 Bad Gateway` from Nginx when its upstream fails, or
`500 Internal Server Error` from the app itself). Reading the status code is always the first diagnostic
step before digging into logs.

**49. What's the difference between a service binding to `127.0.0.1` vs `0.0.0.0`?**
Binding to `127.0.0.1` (localhost) only accepts connections originating from the same machine - useful for
the `app` container's internal-only access pattern in this project (it's not published to the host at all).
Binding to `0.0.0.0` accepts connections on any network interface, meaning any reachable client can connect
- which is why only Nginx (the intended public entry point) is published on the host's `0.0.0.0:80`.

## systemd, Bash & Deployment

**50. What's the practical difference between a bare Java process, a systemd service, and a Docker
container for this app?**
A bare `java -jar app.jar` process has no automatic restart and dies with its terminal session. A systemd
service (`valorant-app.service`) adds OS-level supervision - auto-restart on failure, start-on-boot, and
journal logging - while still running directly against the host's installed JRE. A Docker container adds
full runtime isolation (bundled JRE, filesystem, network namespace) with Docker/Compose handling restarts
instead of systemd.

**51. Why does the systemd unit set `Restart=on-failure`?**
This tells systemd to automatically restart the service if the process exits with a non-zero (failure) exit
code, without needing a human to notice and intervene. Combined with `RestartSec=5`, it gives the process a
brief pause before retrying, which helps avoid a tight crash-restart loop from a transient issue like a
port conflict during a redeploy.

**52. Why does `scripts/deploy.sh` return different exit codes for different failure types?**
Distinct exit codes (`1` for a deployment failure, `2` for a failed post-deploy health check) let calling
automation - like the Jenkinsfile - distinguish between "the deploy step itself failed" and "the deploy
happened but the result isn't healthy," which could call for different responses (retry vs. immediate
rollback).

**53. Why do all the Bash scripts source a shared `lib.sh` instead of duplicating logging code?**
Centralizing `log_info`/`log_warn`/`log_error`/`die`/`require_cmd` in one file means every script has
consistent, timestamped log output and identical error-handling behavior, and a fix or improvement to
logging only needs to happen once. It also keeps each individual script focused on its own actual logic
rather than boilerplate.

**54. What's the difference between restart, rebuild, redeploy, and rollback in this project?**
A **restart** stops and starts the exact same running artifact (no new code). A **rebuild** produces a
brand-new artifact from the current source (`scripts/build.sh`). A **redeploy** ships that same artifact
again (useful if the running instance got into a bad state but the code itself is fine). A **rollback**
(`scripts/rollback.sh`) switches back to the *previous*, already-validated artifact recorded before the
current one was deployed - the only one of the four that intentionally reverts to older code.

## Environment Variables, Logging & Monitoring

**55. Why does this project use environment variables instead of hardcoding configuration?**
Environment variables let the exact same JAR or Docker image behave correctly in different environments
(local dev, Docker, EC2) without any code changes or rebuilds - only the environment differs. It also keeps
environment-specific values (like `APP_ENV` or `BUILD_NUMBER`) out of source code entirely, which is both
more flexible and safer than hardcoding.

**56. How would you trace a single request from the browser all the way to the application logs?**
Start at the browser's network tab to confirm the request and response status. Then check Nginx's access
log (`/var/log/nginx/access.log` or `docker compose logs nginx`) to confirm Nginx received and forwarded it.
Then check the Spring Boot application's own logs (console/file, or `docker compose logs app`) for the
corresponding request being handled - each layer's logs should show the same request moving through it in
sequence.

**57. What's the difference between application logs and Nginx logs, and why keep them separate?**
Application logs come from the Spring Boot process itself and reflect business logic, startup events, and
errors inside the JVM. Nginx logs (access + error) reflect what happened at the network edge - every
request's method/path/status/timing, regardless of whether the app ever even received it. Keeping them
separate makes it much faster to tell whether a problem is "the request never reached the app" vs. "the app
received it and handled it badly."

**58. How does rollback actually work in this project?**
`scripts/deploy.sh` records whatever was previously running (image tag or systemd release symlink) before
switching to a new one. `scripts/rollback.sh` reads that record and redeploys the previous, already-known
artifact instead of the current one, then runs the same health check used for a normal deployment to confirm
the rollback actually restored a working state.

**59. If health checks start failing in production, what would you check first?**
First confirm the app process/container is even running (`docker compose ps` or `systemctl status
valorant-app`); if it's not, that's the root cause. If it is running, hit its health endpoint directly,
bypassing Nginx, to isolate whether the problem is the app itself or the proxy layer in front of it, then
check the relevant logs for that layer.

**60. Why does `scripts/cleanup.sh` enforce a fixed retention count for old releases and backups?**
Every backup, Docker image, and systemd release directory consumes disk space, and a small EC2 root volume
can fill up if nothing ever gets removed. Keeping only the most recent N (configurable via `--keep-releases`
/ `--keep-backups`) balances having enough history to roll back or investigate against not silently running
out of disk space over time.

**61. What's the practical value of the `/info` endpoint for DevOps practice, beyond showing off on the
About page?**
It gives you a single, scriptable place to confirm exactly what's running - version, build number, git
commit, environment, and container status - without needing to SSH in and inspect files manually. This is
directly useful during deployments and incident response: `curl http://<host>/info` instantly answers "is
this actually the build I just deployed?"
