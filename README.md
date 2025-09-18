ChatO - Real-Time Multi-Server Chat Application
ChatO is a scalable, real-time chat application built in Java. It demonstrates a multi-server architecture where different client instances can connect to separate servers and still communicate seamlessly with each other. This is made possible by using Redis as a high-speed message broker for Publish/Subscribe messaging.

The application also integrates Google Firebase Firestore for persistent message storage and includes modern features like online user tracking and recent chat history caching.

Key Features
Multi-Server Architecture: Run multiple server instances on different ports. Clients connected to different servers can chat with each other in real-time.

Real-Time Messaging: Instantaneous message delivery between all connected clients.

Redis Pub/Sub Integration: Uses Redis as a message broker to connect all server instances, enabling a scalable backend.

Online Presence Tracking: Type /online to see a list of all currently connected users across all servers.

Recent Chat History: New users are shown the last 15 messages upon joining, providing immediate context.

Persistent Storage: Chat messages are saved to Google Firebase Firestore.

File Sharing: Users can send and receive files.

Bot Commands: Interactive bot commands like /joke, /time, and /help.

"Is Typing" Indicator: See when another user is typing a message.

Tech Stack
Backend: Java

UI: Java Swing

Messaging Broker: Redis (Pub/Sub)

Database: Google Firebase Firestore

Build Tool: Apache Maven

Containerization: Docker (for Redis)

Getting Started
Follow these instructions to get the project up and running on your local machine.

Prerequisites
Java Development Kit (JDK) - Version 11 or higher

Apache Maven - For building the project and managing dependencies

Docker Desktop - To run a Redis instance easily

Google Firebase Account - To set up the Firestore database

1. Firebase Setup
Go to the Firebase Console and create a new project.

In your project, go to Project Settings > Service accounts.

Click "Generate new private key" and download the resulting JSON file.

Important: For security, do not place this key inside the project folder. Store it in a safe, permanent location on your computer (e.g., C:\Users\YourUser\Documents\secrets\).

In your project, navigate to the src/main/java/com/chatapp/ directory and open the FirebaseConfig.java file. It is configured to load the key path from an environment variable, which is the secure, recommended approach.

2. Redis Setup with Docker
Redis will act as the message broker. The easiest way to run it is with Docker.

Open a terminal and pull the official Redis image:

docker pull redis

Run the Redis container. This command will start a container named my-redis and map the port 6379.

docker run -d --name my-redis -p 6379:6379 redis

To verify that Redis is running, use the command docker ps. You should see my-redis in the list of running containers.

3. Build the Application
Open your terminal and navigate to the root directory of the ChatO project.

Run the Maven build command:

mvn clean install

This will compile the code and download all the necessary dependencies.

4. Running the Application
You will need to open several separate terminal windows to run the servers and clients.

To Run a Server:
In each terminal you want to run a server, you must first set the environment variable pointing to your Firebase key.

# Set the environment variable (use the correct path for your key)
# For Windows PowerShell:
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\your\firebase-key.json"
# For Git Bash / Linux / macOS:
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/firebase-key.json"

# Run the server on a specific port (e.g., 9000)
mvn exec:java "-Dexec.mainClass=com.chatapp.Server" "-Dexec.args=9000"

To run a second server, open a new terminal and repeat the steps above, but use a different port (e.g., 9001).

To Run a Client:
You do not need to set the environment variable for the client.

# In a new terminal, run the client and point it to a server
# Example: Connecting to the server on port 9000
mvn exec:java "-Dexec.mainClass=com.chatapp.Client" "-Dexec.args=localhost 9000"

A Java Swing GUI will pop up. Enter a username to join the chat.

How to Use
Chat: Type in the text field and press Enter to send a message.

Send a File: Click the "Send File" button.

Commands:

/online: See who is currently in the chat.

/joke: Get a random programming joke.

/time: See the current server time.

/help: See a list of available commands.
