# ---- Build Stage ----
FROM node:22-alpine AS build
WORKDIR /usr/src/app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

# ---- Runtime Stage ----
FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build /usr/src/app/dist/ship-frontend/browser /usr/share/nginx/html
