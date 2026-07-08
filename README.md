# AI-Agent

基于 Spring AI + 通义千问 API 的智能 Agent 应用框架

## 项目简介

AI-Agent 是一个基于 Spring AI 框架和通义千问（DashScope）大语言模型构建的智能 Agent 应用。提供了丰富的工具集、RAG（检索增强生成）能力、对话记忆管理等功能，可用于构建 AI 驱动的应用场景。

## 技术栈

- **Java 17+**
- **Spring Boot 3.x**
- **Spring AI** - AI 应用的 Spring 生态框架
- **通义千问 API** - 阿里云大语言模型服务
- **Kryo** - 高效的序列化库（用于聊天记录存储）
- **Maven** - 项目构建工具

## 核心功能

### 🤖 AI 对话能力
- 基于通义千问的智能对话
- 支持流式输出
- 对话历史记忆管理

### 🛠️ 工具集
- **WebSearchTool** - 百度搜索引擎搜索
- **WebScrapingTool** - 网页内容抓取
- **FileOperationTool** - 文件读写操作
- **TerminalOperationTool** - 终端命令执行
- **PDFGenerationTool** - PDF 文件生成
- **ResourceDownloadTool** - 资源下载
- **EmailNoticeUtil** - 邮件通知（登录提醒、注册欢迎）

### 📚 RAG（检索增强生成）
- 支持多种向量存储：
  - 内存向量存储
  - PGVector 向量存储
- 文档加载器支持 Markdown 和 HTML 格式

### 💾 对话记忆
- 基于文件的对话历史存储
- 支持多会话管理

### 🔌 MCP 服务
- 提供 Model Context Protocol (MCP) 协议支持
- 包含图片搜索 MCP 服务（调用 Pexels API）

## 模块结构

```
ai-agent/
├── src/main/java/com/itheima/aiagent/
│   ├── AiAgentApplication.java          # 主应用入口
│   ├── advisor/                        # 拦截器/顾问
│   │   └── MyLoggerAdvisor.java         # 日志拦截器
│   ├── app/                           # 业务应用
│   │   └── CareerPlanApp.java          # 职业规划应用
│   ├── chatmemory/                    # 对话记忆
│   │   └── FileBasedChatMemory.java    # 文件存储
│   ├── config/                        # 配置类
│   ├── controller/                    # 控制器
│   ├── demo/invoke/                   # 示例代码
│   ├── rag/                          # RAG 相关
│   ├── tools/                        # 工具类
│   └── utils/                        # 工具方法
├── image-search-mcp/                 # 图片搜索 MCP 模块
└── pom.xml                          # Maven 配置
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://gitee.com/tonysteve/ai-agent.git
cd ai-agent
```

### 2. 配置 API Key

在 `application.yml` 中配置通义千问 API Key：

```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key
```

### 3. 构建运行

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

## 配置说明

### 核心配置项

| 配置项 | 说明 |
|--------|------|
| `spring.ai.dashscope.api-key` | 通义千问 API Key |
| `search-api.api-key` | 搜索 API Key |
| `spring.mail.username` | 邮件发送者邮箱 |
| `pexels.api-key` | Pexels API Key（图片搜索用） |

### 工具配置

工具类通过 `@Tool` 注解自动注册到 Agent 系统中，每个工具都有描述性文档。

## 使用示例

### 基本对话

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

### 使用工具

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

### RAG 问答

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

## 扩展开发

### 添加新工具

1. 创建工具类并添加 `@Component` 注解
2. 使用 `@Tool` 注解标记公共方法
3. 使用 `@ToolParam` 注解标记参数

```java
@Component
public class MyCustomTool {
    @Tool(description = "工具描述")
    public String myMethod(@ToolParam(description = "参数描述") String param) {
        // 业务逻辑
        return result;
    }
}
```

### 添加新的 RAG 数据源

创建 `DocumentLoader` 实现类：

```java
@Component
public class MyDocumentLoader {
    public List<Document> load() {
        // 加载文档逻辑
        return documents;
    }
}
```

## 测试

项目包含完整的单元测试和集成测试：

```bash
./mvnw test
```

## 许可证

本项目仅供学习交流使用。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 致谢

- [Spring AI](https://spring.io/projects/spring-ai)
- [通义千问](https://dashscope.aliyuncs.com/)