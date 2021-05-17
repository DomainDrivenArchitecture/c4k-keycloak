FROM node:lts-slim

RUN npm install -g keycloak
RUN mkdir /app

ENTRYPOINT ["keycloak"]
CMD [ "/app/config.edn" ]