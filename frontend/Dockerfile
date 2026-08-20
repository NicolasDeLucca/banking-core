# ---- Build stage ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Vite bakes VITE_* vars into the built JS at build time, not at container
# start - override with --build-arg if the API isn't at the default host:port
# the browser would otherwise reach it at (see docker-compose.yml, which
# leaves this at its default so it lines up with the app service's
# host-published port).
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ---- Runtime stage ----
# nginx:alpine runs its master process as root by default (needed to bind
# port 80, a privileged port). nginxinc/nginx-unprivileged is the same
# official image reconfigured to listen on 8080 and run as a non-root user
# out of the box (correct ownership on its cache/pid dirs already handled) -
# simpler and better-tested than patching directory permissions by hand.
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
# 127.0.0.1, not localhost: this base image's nginx.conf listens on IPv4
# only, but the container resolves "localhost" to ::1 first, so wget would
# get "Connection refused" against a healthy, correctly-listening nginx.
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=5 \
    CMD wget -q -O- http://127.0.0.1:8080/ >/dev/null || exit 1
