
# Support-Bot Core Module

The **Support-Bot Core Module** is responsible for managing chat sessions and handling messages within the application.

---

## Features

- Create, retrieve, and delete chat sessions.
- Handle user or service messages effectively.

---

## API Endpoints

### Chat Management API

1. **Retrieve all active chat sessions**  
   **Endpoint**: `GET /api/chats`  
   Description: Retrieves a list of all active chat sessions.  
   **Response**: Chat session identifiers.

2. **Create a new chat session**  
   **Endpoint**: `POST /api/chats`  
   Description: Creates a new chat session.  
   **Response**: Details of the created session.

3. **Retrieve specific chat session**  
   **Endpoint**: `GET /api/chats/{chatId}`  
   Description: Retrieves details for a specific chat using `chatId`.  
   **Response**: Chat session details.

4. **Delete a specific chat session**  
   **Endpoint**: `DELETE /api/chats/{chatId}`  
   Description: Deletes an existing chat session.  
   **Response**: Confirmation of session deletion.

### Chat Messages API

1. **Send a message to a specific chat**  
   **Endpoint**: `POST /api/chats/{chatId}/messages`  
   Description: Sends a message to a particular chat.  
   **Body Parameters**:
   ```json
   {
     "message": "The content of the chat message."
   }
   ```
   **Response**: Updated chat session details.

---

## Data Structures

- **ChatMessage**: Represents the message entity sent to a chat.
   ```kotlin
   data class ChatMessage(val message: String)
   ```

- **ChatDto and ChatIdDto**: DTOs for transferring data related to chat details and identifiers.

---

## Setup and Usage

### Integration

The Support-Bot Core Module can be integrated into your Java/Kotlin Spring Boot project. Ensure the module is accessible and configured within your **Gradle dependencies**.

### Testing APIs

Use API tools like **Postman** or **cURL** to test the endpoints. For example:

- `GET` all chats using:
   ```bash
   curl -X GET http://localhost:8080/api/chats
   ```

- `POST` a message to a chat using:
   ```bash
   curl -X POST -H "Content-Type: application/json" \
   -d '{"message":"Hello, Support-Bot!"}' \
   http://localhost:8080/api/chats/{chatId}/messages
   ```

---

## Technologies Used

- Kotlin
- Spring Boot
- Spring Data JPA
- Lombok
- Jakarta

---

## Folder Structure

Refer to the project's main **README.md** file for overall directory structure.
---

## 🛠 Setup Instructions

1. **Start Qdrant**:
   ```bash
   docker run -p 6333:6333 qdrant/qdrant
   ```

2. **Run Ollama**:
   ```bash
   ollama run mistral
   ```

3. **Build and run Spring Boot app**:
   ```bash
   ./gradlew bootRun
   ```

4. **Access Web UI or API** at:
   ```
   http://localhost:8080
   ```

---

## 📌 Roadmap

- [ ] Add document ingestion + chunking
- [ ] Use Ollama for embedding generation
- [ ] Enable document re-indexing
- [ ] Build rich frontend (React or Thymeleaf)
- [ ] Add logging and observability

---
