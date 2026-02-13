FROM node:18-alpine

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
COPY package*.json ./
RUN npm install

# Bundle app source
COPY . .

# Expose port
EXPOSE 3000

# Define environment variables
ENV PORT=3000
ENV SECRET_KEY=change_this_secret_in_production

# Start the app
CMD [ "node", "server.js" ]
