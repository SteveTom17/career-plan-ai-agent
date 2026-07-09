<div align="center">
  <p><a href="./README.md">中文</a> | English</p>
  <h1>AI-Agent</h1>
  <p>
    <strong>A career planning agent application built with Spring AI Alibaba and Qwen</strong>
  </p>
  <p>
    AI Chat | RAG | Tool Calling | Manus Agent | MCP Image Search | Vue Frontend
  </p>

  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5.13-green" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Spring%20AI%20Alibaba-1.1.2.0-blue" alt="Spring AI Alibaba">
    <img src="https://img.shields.io/badge/Java-21+-orange" alt="Java">
    <img src="https://img.shields.io/badge/Vue-3.x-42b883" alt="Vue">
  </p>

  <p>
    <a href="#project-overview">Overview</a> |
    <a href="#features">Features</a> |
    <a href="#project-structure">Structure</a> |
    <a href="#quick-start">Quick Start</a> |
    <a href="#api-endpoints">API</a> |
    <a href="#extension-guide">Extension</a>
  </p>
</div>

## Project Overview

**AI-Agent** is an intelligent agent application built with **Spring AI Alibaba**, **Spring Boot**, and **Qwen DashScope**. It focuses on career planning scenarios and provides synchronous chat, streaming chat, local knowledge retrieval, tool calling, and a ReAct-style Manus Agent.

The project includes career planning knowledge documents for RAG-enhanced answers about postgraduate exams, recommendation-based admission, and employment. It also provides tools for web search, web scraping, file operations, terminal execution, PDF generation, resource downloading, and email notification.

## Features

| Feature | Description |
| :--- | :--- |
| Career Planning Agent | Supports multi-turn consulting, structured suggestions, and session memory. |
| Streaming Response | Uses SSE to stream model responses to the frontend. |
| RAG | Loads Markdown documents from `src/main/resources/document` and builds a local vector knowledge base. |
| Tool Calling | Registers search, scraping, file, terminal, PDF, and download tools through Spring AI `@Tool`. |
| Manus Agent | Provides a multi-step agent flow based on BaseAgent, ReActAgent, and ToolCallAgent. |
| MCP Extension | Includes the `image-search-mcp` module for image search through the Pexels API. |
| Frontend and Backend Separation | Provides Spring Boot APIs and a Vue 3 + Vite frontend. |
| API Documentation | Integrates SpringDoc and Knife4j for API inspection and debugging. |

## Technology Stack

| Type | Technology |
| :--- | :--- |
| Backend | Spring Boot 3.5.13 |
| AI Framework | Spring AI, Spring AI Alibaba 1.1.2.0 |
| LLM | Qwen DashScope, default model `qwen-plus` |
| Vector Retrieval | SimpleVectorStore, PGVector-related dependencies |
| Document Readers | Spring AI Markdown Document Reader, Jsoup Document Reader |
| Tool Ecosystem | Spring AI Tool Calling, MCP |
| Frontend | Vue 3, Vue Router, Axios, Vite |
| Build Tools | Maven, npm |

## Project Structure

```text
ai-agent/
|-- src/main/java/com/itheima/aiagent/
|   |-- AiAgentApplication.java          # Backend entry point
|   |-- advisor/                         # ChatClient advisors and logging
|   |-- agent/                           # BaseAgent, ReActAgent, Manus Agent
|   |-- app/                             # Career planning business agent
|   |-- chatmemory/                      # File-based chat memory
|   |-- common/                          # Common responses and error codes
|   |-- config/                          # Spring and RAG configuration
|   |-- controller/                      # REST and SSE endpoints
|   |-- exception/                       # Global exception handling
|   |-- rag/                             # Document loading and vector store config
|   |-- tools/                           # Agent-callable tools
|   `-- utils/                           # Utility methods
|-- src/main/resources/
|   |-- application.yml                  # Application configuration
|   |-- mcp-servers.json                 # MCP server configuration example
|   `-- document/                        # Career planning RAG documents
|-- ai-agent-fronted/                    # Vue 3 frontend project
|-- image-search-mcp/                    # Image search MCP module
|-- Dockerfile
`-- pom.xml
```

## Quick Start

### 1. Requirements

- JDK 21+
- Maven 3.9+ or the bundled `mvnw`
- Node.js 18+ and npm
- A valid DashScope API key
- Optional: search API key, Pexels API key, SMTP auth code, PostgreSQL/PGVector

### 2. Clone the Project

```bash
git clone https://gitee.com/tonysteve/ai-agent.git
cd ai-agent
```

### 3. Configure Backend

Edit `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    dashscope:
      api-key: your-dashscope-api-key
      chat:
        options:
          model: qwen-plus

search-api:
  api-key: your-search-api-key
```

To enable email notification, configure:

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your-email@qq.com
    password: your-smtp-auth-code
    protocol: smtps
```

### 4. Start Backend

```bash
./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8123/api
```

API documentation:

```text
http://localhost:8123/api/swagger-ui.html
```

### 5. Start Frontend

```bash
cd ai-agent-fronted
npm install
npm run dev
```

## API Endpoints

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/ai/career_plan_app/chat/sync` | GET | Synchronous career planning chat. Parameters: `message`, `chatId`. |
| `/api/ai/career_plan_app/chat/sse` | GET | Streaming career planning chat. Parameters: `message`, `chatId`. |
| `/api/ai/career_plan_app/rag/search` | GET | Search the local career planning knowledge base. Parameter: `query`. |
| `/api/ai/manus/chat` | GET | Run the Manus Agent for multi-step tasks. Parameter: `message`. |

Example:

```bash
curl "http://localhost:8123/api/ai/career_plan_app/chat/sync?message=Should I pursue postgraduate study or employment?&chatId=demo"
```

## Configuration

| Key | Description |
| :--- | :--- |
| `spring.ai.dashscope.api-key` | DashScope API key. |
| `spring.ai.dashscope.chat.options.model` | Default chat model, currently `qwen-plus`. |
| `search-api.api-key` | Search service key used by WebSearchTool. |
| `spring.mail.username` | Email sender account. |
| `spring.mail.password` | SMTP authorization code. |
| `server.port` | Backend port, default `8123`. |
| `server.servlet.context-path` | Backend context path, default `/api`. |

## Extension Guide

### Add a New Tool

1. Create a tool class in the `tools` package.
2. Mark model-callable methods with `@Tool`.
3. Describe parameters with `@ToolParam`.
4. Register the tool instance in `ToolRegistration`.

```java
@Component
public class MyCustomTool {

    @Tool(description = "Tool capability description")
    public String run(@ToolParam(description = "Input text") String input) {
        return "Result: " + input;
    }
}
```

### Add RAG Documents

Place new Markdown documents in:

```text
src/main/resources/document/
```

On startup, `CareerPlanAppDocumentLoader` loads these documents into the career planning vector knowledge base.

### Connect a New MCP Server

Use the `image-search-mcp` module and `src/main/resources/mcp-servers.json` as references for connecting external MCP servers to the agent tool ecosystem.

## Testing

```bash
./mvnw test
```

## Contributing

Issues and pull requests are welcome. Please run tests and update related documentation before submitting changes.

## License

This project is intended for learning, communication, and demonstration. Please verify production licensing based on your actual dependencies and use case.

## Acknowledgments

- [Spring AI](https://spring.io/projects/spring-ai)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [Qwen DashScope](https://dashscope.aliyuncs.com/)
- [spring-ai-alibaba/DataAgent](https://github.com/spring-ai-alibaba/DataAgent)
