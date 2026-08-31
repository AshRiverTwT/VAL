# Troubleshooting Lab

15+ realistic failure scenarios for this project. For each: reproduce the symptom on purpose, use the
investigation commands to figure out *why* before reading the solution, then fix it and confirm.

Guidance format for each scenario: **Symptoms** -> **Investigate** -> **Hints** -> **Expected reasoning**
-> **Solution**.

---

## 1. Maven compilation failure

**Symptoms:** `mvn clean package` (or the Jenkins "Build" stage) fails with `COMPILATION ERROR` and stops
before reaching the test phase.

**Investigate:**
```bash
mvn clean compile
```
Read the first error in the output - Maven reports every downstream error too, but the *first* one is
usually the real cause.

**Hints:** Did you rename a method/field somewhere without updating callers? Missing import? Mismatched
braces? Check the exact file:line Maven reports.

**Expected reasoning:** Compilation happens before tests and packaging - a broken build never even gets to
prove whether the logic works, so always fix compilation errors first, from the top of the output down.

**Solution:** Open the reported file/line, fix the syntax/reference error, re-run `mvn clean compile` until
it succeeds, then proceed to `mvn test`.

---

## 2. Unit test failure

**Symptoms:** `mvn test` reports `Tests run: X, Failures: 1` and the build ends with `BUILD FAILURE`.

**Investigate:**
```bash
mvn test
cat target/surefire-reports/*.txt
```

**Hints:** Surefire reports show the exact assertion that failed and the expected vs. actual values.

**Expected reasoning:** A test failure means either the code's behavior changed (bug) or the test's
expectation is now wrong (intentional change, test needs updating) - decide which one is true before
"fixing" anything.

**Solution:** If it's a real bug, fix the source code. If the expected behavior legitimately changed,
update the assertion and explain why in the commit message. See [section 7 of the README](../README.md#7-testing)
for the built-in intentional-failure exercise.

---

## 3. Application fails to start

**Symptoms:** `java -jar target/valorant-devops-app.jar` exits immediately (or `docker run` container
exits) with a stack trace instead of "Started DevopsHubApplication".

**Investigate:**
```bash
java -jar target/valorant-devops-app.jar
# read the full stack trace, especially the "Caused by:" lines
```

**Hints:** Common causes: a bad value in `application.yml`, a port already bound (see #4), or a missing
environment variable a bean depends on.

**Expected reasoning:** Spring Boot prints a fairly descriptive startup failure report - read the
`APPLICATION FAILED TO START` section, it usually names the exact bean/property at fault.

**Solution:** Fix the offending configuration/property and retry. If unsure, run with
`--debug` (`java -jar target/valorant-devops-app.jar --debug`) for verbose autoconfiguration logs.

---

## 4. Port 8080 already in use

**Symptoms:** Startup fails with `Web server failed to start. Port 8080 was already in use.`

**Investigate:**
```bash
ss -tulpn | grep 8080
# or
lsof -i :8080
```

**Hints:** Is a previous run of the app (or another Java process) still alive in the background?

**Expected reasoning:** Two processes can't bind the same TCP port on the same host - one of them has to
stop, or the new one needs a different port.

**Solution:** Either stop the process holding the port (`kill <pid>`), or run this instance on a different
port: `SERVER_PORT=8081 java -jar target/valorant-devops-app.jar`.

---

## 5. Docker build failure

**Symptoms:** `docker build -t valorant-devops-app:local .` fails during the `mvn` step inside the build
stage.

**Investigate:**
```bash
docker build -t valorant-devops-app:local . --progress=plain
```
Read which `RUN` step failed and its output.

**Hints:** Does `mvn clean package` succeed *outside* Docker first? If not, fix that first (see #1/#2).
If it succeeds locally but fails in Docker, check for anything relying on local machine state (an IDE
setting, a file outside `src/`, network access blocked in the build environment).

**Expected reasoning:** Docker builds run in an isolated context with only what `COPY` brought in - if it
works locally but not in the image, something outside the copied files was assumed to exist.

**Solution:** Ensure `pom.xml` and `src/` are enough to build standalone; fix `.dockerignore` if a needed
file is being excluded.

---

## 6. Container immediately exits

**Symptoms:** `docker ps` doesn't show the container; `docker ps -a` shows it with `Exited (1)`.

**Investigate:**
```bash
docker ps -a
docker logs <container-id-or-name>
```

**Hints:** The exit code and last log lines almost always show a startup exception (same class of issue as
#3, but now inside a container with a different environment).

**Expected reasoning:** A container's lifecycle is tied to its main process (PID 1 - the JVM here). If the
JVM exits (crash or clean exit), the container stops - this is by design, not a Docker bug.

**Solution:** Fix whatever `docker logs` shows (often a missing/incorrect environment variable expected by
`application.yml`), rebuild, and re-run.

---

## 7. Nginx 502 Bad Gateway

**Symptoms:** `curl http://localhost/` (or the public IP) returns `502 Bad Gateway` from Nginx.

**Investigate:**
```bash
docker compose ps
docker compose logs app
curl http://localhost:8080/health   # hit the app directly, bypassing Nginx
```

**Hints:** 502 means Nginx is running and received the request, but couldn't get a valid response from its
upstream (`app:8080`). Is the app container actually up and healthy?

**Expected reasoning:** Isolate which side is broken - Nginx itself, or the thing Nginx is proxying to.
Hitting the app directly (bypassing Nginx) tells you immediately which one to fix.

**Solution:** Usually the app container crashed or hasn't finished starting yet - fix the app (see #3/#6),
or wait for its healthcheck to pass; `depends_on: condition: service_healthy` should already prevent Nginx
from being routed to before that, but manual `docker run` setups won't have that protection.

---

## 8. Nginx configuration error

**Symptoms:** `docker compose up` for the `nginx` service fails to start, or `sudo nginx -t` (on a bare
Ubuntu install) reports a syntax error.

**Investigate:**
```bash
sudo nginx -t
docker compose logs nginx
```

**Hints:** A missing semicolon, an unmatched brace, or a stray character in `nginx/nginx.conf` is enough to
fail the whole config test.

**Expected reasoning:** Nginx validates its *entire* configuration before starting - one bad directive
blocks the whole server, so always test config changes with `nginx -t` before reloading/restarting.

**Solution:** Fix the reported line number in `nginx/nginx.conf`, re-run `nginx -t` until it reports
`syntax is ok` / `test is successful`, then restart Nginx.

---

## 9. Security group blocks HTTP

**Symptoms:** `curl http://<EC2_PUBLIC_IP>/health` hangs and eventually times out from your local machine,
but `curl http://localhost/health` works fine when SSH'd into the instance.

**Investigate:**
```bash
# On your local machine:
curl -v http://<EC2_PUBLIC_IP>/health
# On the EC2 instance:
curl -v http://localhost/health
sudo ss -tulpn | grep :80
```

**Hints:** If it works locally on the instance but not from outside, the problem is almost always network
path, not the application - check the EC2 Security Group's inbound rules for port 80.

**Expected reasoning:** A working `localhost` response proves the app + Nginx are fine; a hang (not even a
"connection refused") from outside strongly suggests a firewall silently dropping the packets before they
ever reach Nginx.

**Solution:** In the AWS Console (or CLI), add an inbound rule for `80/tcp` from `0.0.0.0/0` (or your
specific source) to the instance's security group.

---

## 10. SSH connection failure

**Symptoms:** `ssh -i your-key.pem ubuntu@<EC2_PUBLIC_IP>` hangs, times out, or reports
`Permission denied (publickey)`.

**Investigate:**
```bash
ssh -v -i your-key.pem ubuntu@<EC2_PUBLIC_IP>   # verbose mode shows exactly where it fails
chmod 400 your-key.pem                            # SSH refuses overly-open key file permissions
```

**Hints:** Hang/timeout usually means a Security Group or network path problem (port 22 not open, or wrong
public IP - EC2 public IPs can change after a stop/start unless you use an Elastic IP). `Permission denied
(publickey)` usually means the wrong `.pem` file or wrong username (`ubuntu` for Ubuntu AMIs, not `ec2-user`
which is for Amazon Linux).

**Expected reasoning:** Separate "can't reach the host at all" (network/Security Group) from "reached the
host but authentication failed" (wrong key/username) - the symptom (hang vs. explicit rejection) tells you
which category you're in.

**Solution:** Confirm the Security Group allows `22/tcp` from your IP, confirm you're using the current
public IP, confirm the correct `.pem` file and username, and confirm the key file has `chmod 400`
permissions.

---

## 11. Jenkins build failure

**Symptoms:** A Jenkins pipeline run shows a red ✗ on one of the stages.

**Investigate:** Open the failed build in Jenkins -> "Console Output" and scroll to the first error, not
just the last few lines (Jenkins often prints a generic "script returned exit code 1" summary at the very
end).

**Hints:** Match the failing stage name to the underlying command in `Jenkinsfile` (`mvn test` failing =
same as scenario #2; `docker build` failing = same as scenario #5, etc.) - the Jenkins stage is just running
the same commands you'd run manually.

**Expected reasoning:** Jenkins failures are almost never "Jenkins-specific" - they're the same command
failing in a slightly different environment (different working directory, different user, no interactive
terminal). Reproduce the failing command manually first.

**Solution:** Fix the underlying issue (code, config, or environment), commit, push, and re-run the
pipeline.

---

## 12. Jenkins cannot execute Docker

**Symptoms:** The "Docker Build" stage fails with `permission denied while trying to connect to the Docker
daemon socket` or `docker: command not found`.

**Investigate:**
```bash
sudo systemctl status jenkins
groups jenkins
docker --version
```

**Hints:** The `jenkins` system user needs Docker installed AND needs to be a member of the `docker` group
(or the Jenkins service needs to be restarted after that group was added, since group membership is
resolved at login/service-start time).

**Expected reasoning:** Jenkins jobs run as the `jenkins` OS user, not as whatever user you used over SSH -
permissions that work for you interactively don't automatically apply to Jenkins's own process.

**Solution:**
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```
Then re-run the pipeline.

---

## 13. Health check fails

**Symptoms:** `scripts/health-check.sh` (or the Jenkins "Health Check" stage) reports failure after all
retries, even though `docker compose ps` shows the containers running.

**Investigate:**
```bash
docker compose ps
curl -v http://localhost/health
docker compose logs app
docker compose logs nginx
```

**Hints:** "Running" is not the same as "healthy" - check Docker's own healthcheck status
(`docker inspect --format='{{json .State.Health}}' valorant-app`), and check whether Nginx can actually
reach the app on the internal network.

**Expected reasoning:** A health check exercises the full request path (network + proxy + app), so a
failure could be at any layer - narrow it down by testing `app:8080` directly, then through `nginx:80`.

**Solution:** Depends on the layer at fault - restart the unhealthy container, fix a Nginx upstream
misconfiguration (#7), or fix an application startup problem (#3).

---

## 14. Disk space exhausted

**Symptoms:** Deployments start failing with `no space left on device`, or the app can't write logs.

**Investigate:**
```bash
df -h
du -sh /var/lib/docker/* 2>/dev/null | sort -rh | head
du -sh ~/valorant-devops-app/scripts/backups
docker system df
```

**Hints:** Old Docker images/build cache and accumulated backup archives are the usual culprits on a small
EC2 root volume.

**Expected reasoning:** `df -h` tells you *that* you're out of space; `du` (scoped to likely directories)
tells you *where* it went - always narrow from filesystem-level to directory-level before deleting
anything.

**Solution:**
```bash
./scripts/cleanup.sh
docker system prune -af   # more aggressive - removes ALL unused images, use with care
```

---

## 15. Application works locally but not on EC2

**Symptoms:** Everything works on your laptop; on EC2, requests to `http://<EC2_PUBLIC_IP>/` fail (502,
timeout, or connection refused), even though you deployed the exact same commit.

**Investigate (work through in order, don't skip steps):**
```bash
# 1) Is the app even running on the instance?
docker compose ps

# 2) Does the app answer locally on the instance itself?
curl http://localhost:8080/health

# 3) Does Nginx answer locally on the instance itself?
curl http://localhost/health

# 4) Does anything answer from OUTSIDE the instance?
curl -v http://<EC2_PUBLIC_IP>/health
```

**Hints:** Each step isolates one more layer: app process -> Nginx -> the network path from the internet.
Stop at the first step that fails - that's your layer.

**Expected reasoning:** "Works on my machine" almost always means a difference in *environment*, not code:
missing environment variables in production config, a Security Group blocking the port (#9), Nginx
pointing at the wrong upstream (#7), or the EC2 public IP having changed since your last deploy.

**Solution:** Depends entirely on which step above first failed - that's the point of this exercise: the
diagnostic process matters more than any single fix.
