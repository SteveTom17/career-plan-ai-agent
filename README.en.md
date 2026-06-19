# AI-Agent

An intelligent Agent application framework based on Spring AI + Qwen API

## Project Overview

AI-Agent is an intelligent Agent application built on the Spring AI framework and the Qwen (DashScope) large language model. It provides a rich set of tools, RAG (Retrieval-Augmented Generation) capabilities, and conversation memory management features, suitable for building AI-driven applications.

## Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring AI** - Spring ecosystem framework for AI applications
- **Qwen API** - Alibaba Cloud's large language model service
- **Kryo** - High-performance serialization library (used for chat history storage)
- **Maven** - Project build tool

## Core Features

### 🤖 AI Conversation Capabilities
- Intelligent conversations powered by Qwen
- Support for streaming output
- Conversation history memory management

### 🛠️ Toolset
- **WebSearchTool** - Baidu search engine querying
- **WebScrapingTool** - Web page content scraping
- **FileOperationTool** - File read/write operations
- **TerminalOperationTool** - Terminal command execution
- **PDFGenerationTool** - PDF file generation
- **ResourceDownloadTool** - Resource downloading
- **EmailNoticeUtil** - Email notifications (login alerts, registration welcome)

### 📚 RAG (Retrieval-Augmented Generation)
- Supports multiple vector stores:
  - In-memory vector store
  - PGVector vector store
- Document loaders support Markdown and HTML formats

### 💾 Conversation Memory
- File-based storage of conversation history
- Support for multi-session management

### 🔌 MCP Service
- Provides support for the Model Context Protocol (MCP)
- Includes an image search MCP service (calls the Pexels API)

## Module Structure

```
ai-agent/
├── src/main/java/com/itheima/aiagent/
│   ├── AiAgentApplication.java          # Main application entry
│   ├── advisor/                        # Interceptors/Advisors
│   │   └── MyLoggerAdvisor.java         # Logging interceptor
│   ├── app/                           # Business applications
│   │   └── CareerPlanApp.java          # Career planning application
│   ├── chatmemory/                    # Conversation memory
│   │   └── FileBasedChatMemory.java    # File-based storage
│   ├── config/                        # Configuration classes
│   ├── controller/                    # Controllers
│   ├── demo/invoke/                   # Example code
│   ├── rag/                          # RAG-related components
│   ├── tools/                        # Tool classes
│   └── utils/                        # Utility methods
├── image-search-mcp/                 # Image search MCP module
└── pom.xml                          # Maven configuration
```

## Quick Start

### 1. Clone the Project

```bash
git clone https://gitee.com/tonysteve/ai-agent.git
cd ai-agent
```

### 2. Configure API Key

Set the Qwen API key in `application.yml`:

```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key
```

### 3. Build and Run

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

## Configuration Details

### Core Configuration Options

| Configuration Key | Description |
|-------------------|-------------|
| `spring.ai.dashscope.api-key` | Qwen API Key |
| `search-api.api-key` | Search API Key |
| `spring.mail.username` | Sender email address |
| `pexels.api-key` | Pexels API Key (for image search) |

### Tool Configuration

Tool classes are automatically registered into the Agent system via the `@Tool` annotation. Each tool includes descriptive documentation.

## Usage Examples

### Basic Conversation

```java
@Autowired
private ChatClient chatClient;

public String chat(String message) {
    return chatClient.prompt()
        .user(message)
        .call()
        .content();
}
```

### Using Tools

```java
@Autowired
private ToolCallback[] allTools;

public String chatWithTools(String message) {
    return chatClient.prompt()
        .user(message)
        .tools(allTools)
        .call()
        .content();
}
```

### RAG Query

```java
@Resource
private VectorStore vectorStore;

public String ragQuery(String question) {
    return chatClient.prompt()
        .user(question)
        .vectorStore(vectorStore)
        .call()
        .content();
}
```

## Extending Development

### Adding a New Tool

1. Create a tool class and annotate it with `@Component`
2. Mark public methods with the `@Tool` annotation
3. Annotate parameters with `@ToolParam`

```java
@Component
public class MyCustomTool {
    @Tool(description = "Tool description")
    public String myMethod(@ToolParam(description = "Parameter description") String param) {
        // Business logic
        return result;
    }
}
```

### Adding a New RAG Data Source

Create a `DocumentLoader` implementation:

```java
@Component
public class MyDocumentLoader {
    public List<Document> load() {
        // Document loading logic
        return documents;
    }
}
```

## Testing

The project includes comprehensive unit and integration tests:

```bash
./mvnw test
```

## License

This project is intended solely for learning and communication purposes.

## Contributions

Issues and Pull Requests are welcome!

## Acknowledgments

- [Spring AI](https://spring.io/projects/spring-ai)
- [Qwen](https://dashscope.aliyuncs.com/)