# syntax=docker/dockerfile:1

# ---- Stage 1: build the Angular application ----
FROM node:24-alpine AS build
WORKDIR /app

# Dependencies first, cached until package-lock.json changes.
# "npm ci" installs exactly the locked versions.
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build

# ---- Stage 2: static files served by nginx (no Node.js at runtime) ----
# The "unprivileged" image runs nginx as a non-root user, listening on 8080.
FROM nginxinc/nginx-unprivileged:1.31-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html/
EXPOSE 8080
