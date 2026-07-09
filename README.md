<div align="center">
  <p>中文 | <a href="./README.en.md">English</a></p>
  <h1>AI-Agent</h1>
  <p>
    <strong>基于 Spring AI Alibaba 与通义千问的职业规划智能体应用</strong>
  </p>
  <p>
    AI 对话 | RAG 检索增强 | 工具调用 | Manus Agent | MCP 图片搜索 | Vue 前端
  </p>

  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.5.13-green" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Spring%20AI%20Alibaba-1.1.2.0-blue" alt="Spring AI Alibaba">
    <img src="https://img.shields.io/badge/Java-21+-orange" alt="Java">
    <img src="https://img.shields.io/badge/Vue-3.x-42b883" alt="Vue">
  </p>

  <p>
    <a href="#项目简介">项目简介</a> |
    <a href="#核心特性">核心特性</a> |
    <a href="#项目结构">项目结构</a> |
    <a href="#快速开始">快速开始</a> |
    <a href="#接口说明">接口说明</a> |
    <a href="#扩展开发">扩展开发</a>
  </p>
</div>

## 项目简介

**AI-Agent** 是一个基于 **Spring AI Alibaba**、**Spring Boot** 和 **通义千问 DashScope** 构建的智能体应用示例。项目围绕职业生涯规划场景，提供同步对话、流式对话、本地知识库检索、工具调用和 ReAct 风格的 Manus Agent 能力。

项目内置职业规划知识文档，可通过 RAG 对“考研、保研、就业”等问题进行检索增强回答；同时提供网页搜索、网页抓取、文件读写、终端执行、PDF 生成、资源下载、邮件通知等工具，便于继续扩展为更完整的业务 Agent。

## 核心特性

| 特性 | 说明 |
| :--- | :--- |
| 职业规划 Agent | 面向职业规划咨询场景，支持多轮对话、结构化建议与会话记忆。 |
| 流式响应 | 基于 SSE 输出模型回复，适合前端实时展示。 |
| RAG 检索增强 | 加载 `src/main/resources/document` 下的 Markdown 知识文档，并构建本地向量知识库。 |
| 工具调用 | 通过 Spring AI `@Tool` 注册搜索、抓取、文件、终端、PDF、下载等工具。 |
| Manus Agent | 基于 BaseAgent、ReActAgent、ToolCallAgent 的智能体执行链，支持多步任务处理。 |
| MCP 扩展 | 包含 `image-search-mcp` 模块，可通过 Pexels API 提供图片搜索能力。 |
| 前后端分离 | 后端提供 Spring Boot API，前端使用 Vue 3 + Vite 构建。 |
| API 文档 | 集成 SpringDoc 与 Knife4j，便于查看和调试接口。 |

## 技术栈

| 类型 | 技术 |
| :--- | :--- |
| 后端框架 | Spring Boot 3.5.13 |
| AI 框架 | Spring AI、Spring AI Alibaba 1.1.2.0 |
| 大模型 | 通义千问 DashScope，默认模型 `qwen-plus` |
| 向量检索 | SimpleVectorStore、PGVector 相关依赖 |
| 文档读取 | Spring AI Markdown Document Reader、Jsoup Document Reader |
| 工具生态 | Spring AI Tool Calling、MCP |
| 前端 | Vue 3、Vue Router、Axios、Vite |
| 构建工具 | Maven、npm |

## 项目结构

```text
ai-agent/
|-- src/main/java/com/itheima/aiagent/
|   |-- AiAgentApplication.java          # 后端应用入口
|   |-- advisor/                         # ChatClient 顾问与日志
|   |-- agent/                           # BaseAgent、ReActAgent、Manus Agent
|   |-- app/                             # 职业规划业务 Agent
|   |-- chatmemory/                      # 文件会话记忆
|   |-- common/                          # 通用响应对象与错误码
|   |-- config/                          # Spring 与 RAG 配置
|   |-- controller/                      # REST/SSE 接口
|   |-- exception/                       # 全局异常处理
|   |-- rag/                             # 文档加载与向量库配置
|   |-- tools/                           # Agent 可调用工具
|   `-- utils/                           # 工具方法
|-- src/main/resources/
|   |-- application.yml                  # 应用配置
|   |-- mcp-servers.json                 # MCP 服务配置示例
|   `-- document/                        # 职业规划 RAG 知识文档
|-- ai-agent-fronted/                    # Vue 3 前端项目
|-- image-search-mcp/                    # 图片搜索 MCP 模块
|-- Dockerfile
`-- pom.xml
```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.9+ 或使用项目内置 `mvnw`
- Node.js 18+ 与 npm
- 可用的 DashScope API Key
- 可选：搜索 API Key、Pexels API Key、SMTP 邮箱授权码、PostgreSQL/PGVector

### 2. 克隆项目

```bash
git clone https://gitee.com/tonysteve/ai-agent.git
cd ai-agent
```

### 3. 配置后端

编辑 `src/main/resources/application.yml`：

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

如需使用邮件通知，继续配置：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your-email@qq.com
    password: your-smtp-auth-code
    protocol: smtps
```

### 4. 启动后端

```bash
./mvnw spring-boot:run
```

后端默认运行在：

```text
http://localhost:8123/api
```

接口文档地址：

```text
http://localhost:8123/api/swagger-ui.html
```

### 5. 启动前端

```bash
cd ai-agent-fronted
npm install
npm run dev
```

## 接口说明

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/ai/career_plan_app/chat/sync` | GET | 职业规划 Agent 同步对话，参数：`message`、`chatId`。 |
| `/api/ai/career_plan_app/chat/sse` | GET | 职业规划 Agent 流式对话，参数：`message`、`chatId`。 |
| `/api/ai/career_plan_app/rag/search` | GET | 查询职业规划本地知识库，参数：`query`。 |
| `/api/ai/manus/chat` | GET | 调用 Manus Agent 进行多步任务处理，参数：`message`。 |

示例：

```bash
curl "http://localhost:8123/api/ai/career_plan_app/chat/sync?message=我适合考研还是就业&chatId=demo"
```

## 配置说明

| 配置项 | 说明 |
| :--- | :--- |
| `spring.ai.dashscope.api-key` | DashScope API Key。 |
| `spring.ai.dashscope.chat.options.model` | 默认对话模型，当前为 `qwen-plus`。 |
| `search-api.api-key` | WebSearchTool 使用的搜索服务 Key。 |
| `spring.mail.username` | 邮件通知发送账号。 |
| `spring.mail.password` | 邮箱 SMTP 授权码。 |
| `server.port` | 后端端口，默认 `8123`。 |
| `server.servlet.context-path` | 后端上下文路径，默认 `/api`。 |

## 扩展开发

### 添加新工具

1. 在 `tools` 包下创建工具类。
2. 使用 `@Tool` 标记可被模型调用的方法。
3. 使用 `@ToolParam` 描述参数含义。
4. 在 `ToolRegistration` 中注册工具实例。

```java
@Component
public class MyCustomTool {

    @Tool(description = "工具能力说明")
    public String run(@ToolParam(description = "输入内容") String input) {
        return "处理结果：" + input;
    }
}
```

### 添加 RAG 知识文档

将新的 Markdown 文档放入：

```text
src/main/resources/document/
```

应用启动时会通过 `CareerPlanAppDocumentLoader` 加载文档，并写入职业规划向量知识库。

### 接入新的 MCP 服务

可参考 `image-search-mcp` 模块和 `src/main/resources/mcp-servers.json`，将外部 MCP Server 挂载到 Agent 工具生态中。

## 测试

```bash
./mvnw test
```

## 贡献

欢迎提交 Issue 或 Pull Request。建议在提交前先运行测试，并补充必要的文档说明。

## 许可证

本项目仅用于学习、交流和示例演示，请根据实际依赖与业务场景自行确认生产使用许可。

## 致谢

- [Spring AI](https://spring.io/projects/spring-ai)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [通义千问 DashScope](https://dashscope.aliyuncs.com/)
- [spring-ai-alibaba/DataAgent](https://github.com/spring-ai-alibaba/DataAgent)
