# =========================================
# Stage 1: Build the Angular Application
# =========================================
ARG NODE_VERSION=24.7.0-alpine
ARG NGINX_VERSION=alpine3.22

FROM node:${NODE_VERSION} AS builder

WORKDIR /app

COPY package.json package-lock.json ./

RUN --mount=type=cache,target=/root/.npm npm ci

COPY . .

RUN npm run build

# =========================================
# Stage 2: Prepare Nginx to Serve Static Files
# =========================================

FROM nginx:${NGINX_VERSION} AS runner

ENV BACKEND_URL=http://hexagonship-backend:8080

COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

COPY --from=builder /app/dist/*/browser /usr/share/nginx/html
