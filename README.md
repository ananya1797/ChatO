# ChatO  - A java Chat Application.

ChatO is an advanced real-time chat application built with Firebase for authentication and database management. The project is structured with Maven for dependency handling and is designed to support scalable integrations. While OpenAI API functionality has not been implemented yet, the core system for authentication and messaging is functional.  

## Features  
- **Firebase Authentication:** Secure login and user management.  
- **Firestore Database:** Real-time storage and retrieval of messages.  
- **Maven Integration:** Efficient dependency management and project structuring.  


## Tech Stack  
- **Firebase:** Authentication and Firestore for real-time chat functionality.  
- **Maven:** Dependency management for structured backend development.  

## Setup Instructions  
1. Clone the repository.  
2. Configure Firebase for authentication and Firestore.  
3. Run the project using Maven.  

running instructions:
server: java -cp "target/ChatO-1.0-SNAPSHOT.jar;target/dependency/*" com.chatapp.Server
client: java -cp "target/ChatO-1.0-SNAPSHOT.jar;target/dependency/*" com.chatapp.Client
