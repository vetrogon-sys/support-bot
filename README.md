# 🧠 SupportBot - AI-Powered Site-Specific Question Answering

A lightweight Kotlin-based application that uses **Retrieval-Augmented Generation (RAG)** and **LLM** technology to help support teams answer user questions with high relevance and efficiency.

---

## 🚀 Project Goals

- Enable customer support teams to **ask natural language questions**
- Use **context-aware AI** to retrieve relevant information from internal site-specific data
- Deliver **LLM-generated answers** based on retrieved context
- Run **locally or in a private cloud** for data safety and control

---

## 🧱 Tech Stack

| Layer            | Technology          | Purpose                                 |
|------------------|---------------------|------------------------------------------|
| Language         | Kotlin (JVM)        | Backend development                     |
| Build Tool       | Gradle              | Project and dependency management       |
| Framework        | Spring Boot         | REST API, Dependency Injection          |
| AI Framework     | Spring AI           | LLM and embedding integration           |
| Vector DB        | Qdrant              | Semantic document retrieval             |
| LLM Backend      | Ollama              | Local language model (answer + embedding) |
| Web UI           | Spring MVC or HTML  | Simple frontend for Q&A interaction     |
| Embedding Model  | LLM or Sentence-BERT| Vector representation of documents      |

---

## 🎯 Key Features

- **Custom RAG pipeline** for context retrieval from internal sources
- **LLM integration via Spring AI** to generate natural answers
- **Document ingestion + vector indexing** with Qdrant
- **Web UI** for users to submit questions and view responses
- Designed to run **offline or in secure internal networks**

---

## 🔁 System Overview

### ASCII Dependency Diagram

```
            +---------------------+
            |     Web UI         |
            |  (Ask Question)    |
            +----------+----------+
                       |
                       v
            +----------+----------+
            |   Spring Boot App   |
            | (QuestionController)|
            +----------+----------+
                       |
      +----------------+----------------+
      |                                 |
      v                                 v
+-------------+                +----------------+
|  RAG Module |                |  LLM Generator |
| (Qdrant +   |                |  (via Ollama)  |
| Embeddings) |                +----------------+
+------+------+
       |
       v
+------------------+
| Embedded Context |
|   from Qdrant    |
+------------------+
```

---

## 📂 Folder Structure

```
src/
├── main/
│   ├── kotlin/com/example/supportbot/
│   │   ├── controller/        # Web endpoints
│   │   ├── service/           # Coordination layer
│   │   ├── rag/               # Retrieval logic with Qdrant
│   │   ├── llm/               # Answer generation using Ollama
│   │   ├── qdrant/            # Qdrant client abstraction
│   │   └── model/             # Data models
│   └── resources/
│       └── application.yml    # Config
```

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

## 📄 License

MIT or Private License (to be defined)

---

## 🤝 Contributing

We welcome improvements and ideas! Just open a PR or issue.