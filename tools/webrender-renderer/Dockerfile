FROM node:18-slim

# Install deps required by Chromium
RUN apt-get update && apt-get install -y --no-install-recommends \
    gconf-service libasound2 libatk1.0-0 libc6 libcairo2 libgcc1 libgdk-pixbuf2.0-0 libglib2.0-0 libgtk-3-0 libnspr4 libnss3 libpango-1.0-0 libx11-6 libx11-xcb1 libxcomposite1 libxdamage1 libxext6 libxrandr2 libgbm1 libatk-bridge2.0-0 libxss1 fonts-liberation ca-certificates wget && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY package.json package.json
RUN npm install --no-audit --no-fund
COPY index.js index.js

EXPOSE 3000
CMD ["node", "index.js"]
