FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
RUN npm install -g @expo/ngrok@^4.1.0
COPY . .
EXPOSE 8081
CMD ["npx", "expo", "start", "--tunnel"]
