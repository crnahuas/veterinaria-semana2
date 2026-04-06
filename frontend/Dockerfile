FROM nginx:1.27-alpine

COPY frontend/docker/default.conf /etc/nginx/conf.d/default.conf
COPY apache/server.crt /etc/nginx/certs/server.crt
COPY apache/server.key /etc/nginx/certs/server.key
COPY frontend/index.html /usr/share/nginx/html/index.html
COPY frontend/login.html /usr/share/nginx/html/login.html
COPY frontend/pacientes.html /usr/share/nginx/html/pacientes.html
COPY frontend/citas.html /usr/share/nginx/html/citas.html
COPY frontend/css /usr/share/nginx/html/css
COPY frontend/js /usr/share/nginx/html/js

EXPOSE 80 443
