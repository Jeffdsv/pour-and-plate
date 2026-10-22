# stage 1: build the react app into plain html, css and js files
FROM node:24-alpine AS build
WORKDIR /app

# same trick as the backend: dependencies first, so docker can reuse them
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# the google key is passed in when building, vite bakes it into the js files
ARG VITE_GOOGLE_MAPS_KEY
ENV VITE_GOOGLE_MAPS_KEY=$VITE_GOOGLE_MAPS_KEY
RUN npm run build

# stage 2: nginx serves the finished files, no node needed anymore
FROM nginx:stable-alpine
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://backend:8081
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80