# docker/ folder

The main `Dockerfile` lives at the repository root (Docker convention - it
needs to be next to the build context). This folder is reserved for
Docker-related runtime data used by `docker-compose.yml`:

- `docker/data/` - created automatically as a bind-mounted volume target for
  application logs (see the `app` service's `volumes:` section). It is
  git-ignored on purpose since it holds runtime output, not source.

Nothing needs to be manually created here before running
`docker compose up` - Docker creates the bind-mount directory for you.
