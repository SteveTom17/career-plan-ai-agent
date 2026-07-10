<div align="center">
  <p>中文 | <a href="./README.en.md">English</a></p>

  <h1>AI-Agent</h1>

  <p>
    基于 <a href="https://github.com/alibaba/spring-ai-alibaba">Spring AI Alibaba</a> 与通义千问的职业规划智能体应用
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
    <a href="#项目简介">项目简介</a> •
    <a href="#核心特性">核心特性</a> •
    <a href="#技术架构">技术架构</a> •
    <a href="#快速开始">快速开始</a> •
    <a href="#接口文档">接口文档</a> •
    <a href="#数据库说明">数据库说明</a>
  </p>
</div>

![AI-Agent 技术架构图](docs/images/architecture.svg)

## 项目简介

**AI-Agent** 是一个面向职业规划场景的智能体项目。项目基于 **Spring Boot**、**Spring AI Alibaba**、**通义千问 DashScope** 和 **Vue 3** 构建，支持职业规划多轮对话、本地知识库检索增强、SSE 流式输出、工具调用、Manus 多步智能体和 MCP 扩展。

做这个项目的原因是：职业规划问题通常不是一个“查答案”的问题，而是一个需要结合用户现状、目标、约束和资料信息逐步分析的问题。项目希望把大模型的自然语言交互、本地职业规划资料和 Agent 工具调用能力结合起来，让用户可以围绕考研、保研、就业、转型等问题获得更有上下文的建议。

项目仓库地址：https://gitee.com/tonysteve/ai-agent

## 核心特性

| 特性 | 说明 |
| :--- | :--- |
| **职业规划 Agent** | 面向职业咨询场景，支持多轮对话、引导式提问和结构化建议。 |
| **RAG 检索增强** | 加载 `src/main/resources/document` 下的 Markdown 文档，基于向量检索增强回答。 |
| **流式响应** | 基于 SSE 输出模型回复，前端可以实时展示生成过程。 |
| **会话记忆** | 使用文件型 ChatMemory 按 `chatId` 保存不同会话上下文。 |
| **工具调用** | 通过 Spring AI `@Tool` 注册网页搜索、网页抓取、文件读写、终端执行、PDF 生成、资源下载等工具。 |
| **Manus Agent** | 基于 BaseAgent、ReActAgent、ToolCallAgent 的多步任务执行链。 |
| **MCP 扩展** | `image-search-mcp` 子模块提供图片搜索 MCP Server 示例。 |
| **前后端分离** | 后端提供 Spring Boot API，前端使用 Vue 3 + Vite 构建。 |
| **接口文档** | 集成 SpringDoc 与 Knife4j，便于查看和调试接口。 |

## 技术架构

项目采用前后端分离架构：

```text
Vue 页面
  -> Axios / EventSource
  -> AiController
  -> CareerPlanApp / YuManus
  -> ChatClient + Advisors + ToolCallbacks
  -> DashScope Chat / Embedding
  -> 本地知识库、工具、MCP 服务
```

核心分层如下：

| 层级 | 组成 | 说明 |
| :--- | :--- | :--- |
| 前端层 | `ai-agent-fronted` | Vue 3 页面、路由、Axios 请求和对话展示。 |
| 接口层 | `AiController`、`HealthController` | 提供 REST、SSE、健康检查和知识库检索接口。 |
| Agent 层 | `CareerPlanApp`、`YuManus` | 封装职业规划对话、RAG Advisor、流式输出和 Manus 多步执行。 |
| AI 能力层 | Spring AI ChatClient、DashScope | 接入通义千问对话模型和 Embedding 模型。 |
| 知识库层 | Markdown 文档、SimpleVectorStore | 启动时加载本地职业规划资料并构建内存向量库。 |
| 工具层 | `tools` 包、MCP Server | 提供搜索、抓取、文件、终端、PDF、资源下载和图片搜索扩展。 |

## 我负责什么

本项目中我主要负责：

- 设计职业规划 Agent 的系统提示词和咨询式对话规则。
- 实现 `CareerPlanApp`，支持同步对话、流式对话、RAG 检索增强和工具调用。
- 搭建本地职业规划知识库加载流程，将 Markdown 文档转换为向量检索内容。
- 封装统一接口响应、异常处理、健康检查和接口文档能力。
- 实现 Vue 前端页面，并与后端同步接口和 SSE 流式接口联调。
- 扩展 Manus Agent 执行链和 MCP 图片搜索子模块示例。

## 难点与解决方案

最难的问题是让“职业咨询型对话”和“知识库事实问答”共用一个 Agent 时仍然表现稳定。

如果只使用普通系统提示词，模型容易在用户询问资料内容时继续追问用户，导致知识库问答不够直接；如果只强调知识库问答，模型又会失去职业规划咨询需要的引导性。

解决方式：

1. 在系统提示词中明确区分“职业咨询问题”和“知识库事实问题”的回答规则。
2. 使用 `QuestionAnswerAdvisor` 接入向量库，并自定义 RAG PromptTemplate，让检索内容只在合适场景中作为依据。
3. 对外提供单独的 `/rag/search` 接口，便于调试知识库召回结果。

相关核心代码：

- `src/main/java/com/itheima/aiagent/app/CareerPlanApp.java`
- `src/main/java/com/itheima/aiagent/rag/CareerPlanAppDocumentLoader.java`
- `src/main/java/com/itheima/aiagent/rag/CareerPlanAppVectorStoreConfig.java`

## 项目结构

```text
ai-agent/
|-- src/main/java/com/itheima/aiagent/
|   |-- AiAgentApplication.java          # 后端应用入口
|   |-- agent/                           # BaseAgent、ReActAgent、ToolCallAgent、YuManus
|   |-- app/                             # 职业规划业务 Agent
|   |-- chatmemory/                      # 文件会话记忆
|   |-- common/                          # 统一响应对象与错误码
|   |-- config/                          # 异步、RAG Advisor 等配置
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
|-- image-search-mcp/                    # 图片搜索 MCP 子模块
|-- docs/
|   |-- database/init.sql                # 可选 PGVector 初始化脚本
|   `-- images/architecture.svg          # 技术架构图
|-- Dockerfile
`-- pom.xml
```

## 核心模块说明

| 模块 | 文件/目录 | 说明 |
| :--- | :--- | :--- |
| 接口层 | `controller/AiController.java` | 提供职业规划同步对话、SSE 对话、RAG 搜索和 Manus 对话接口。 |
| 职业规划 Agent | `app/CareerPlanApp.java` | 封装 ChatClient、系统提示词、会话记忆、RAG Advisor 和流式调用。 |
| Manus Agent | `agent/` | 定义 Agent 状态、ReAct 执行流程和工具调用执行链。 |
| 知识库加载 | `rag/CareerPlanAppDocumentLoader.java` | 扫描 `document/*.md` 并转换为 Spring AI Document。 |
| 向量库配置 | `rag/CareerPlanAppVectorStoreConfig.java` | 使用 DashScope EmbeddingModel 初始化 SimpleVectorStore。 |
| 工具注册 | `tools/ToolRegistration.java` | 集中注册可被模型调用的工具。 |
| 前端页面 | `ai-agent-fronted/src/views/` | 首页、职业规划聊天页和 Manus 聊天页。 |
| MCP 服务 | `image-search-mcp/` | 独立图片搜索 MCP Server 示例。 |

## 快速开始

### 1. 准备环境

- JDK 21+
- Maven 3.9+，也可以使用项目内置 `mvnw`
- Node.js 18+ 和 npm
- 通义千问 DashScope API Key
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

如需邮件通知，继续配置：

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

Windows：

```bash
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
./mvnw spring-boot:run
```

后端默认地址：

```text
http://localhost:8123/api
```

### 5. 启动前端

```bash
cd ai-agent-fronted
npm install
npm run dev
```

前端默认地址通常为：

```text
http://localhost:5173
```

### 6. 启动图片搜索 MCP 子模块

```bash
cd image-search-mcp
../mvnw spring-boot:run
```

需要先在 `image-search-mcp/src/main/resources/application.yml` 中配置 Pexels API Key。

## 数据库说明

当前项目默认 **不依赖业务数据库**。

现在实际存储方式如下：

| 数据 | 当前存储位置 | 说明 |
| :--- | :--- | :--- |
| 职业规划知识库文档 | `src/main/resources/document/*.md` | 作为 RAG 原始资料随项目提交。 |
| 文档向量 | JVM 内存中的 `SimpleVectorStore` | 应用启动时由 Markdown 文档生成，服务重启后重新加载。 |
| 会话记忆 | `tmp/chat-memory` | `FileBasedChatMemory` 按会话保存上下文，本地运行产生，不建议提交仓库。 |
| API Key 和服务配置 | `application.yml` / `application-local.yml` | 示例配置可提交，真实密钥不应提交。 |

也就是说，当前版本没有用户表、订单表、业务表，也没有把聊天记录正式落到数据库。`docs/database/init.sql` 只是给后续切换到 PostgreSQL + PGVector 时使用的可选初始化脚本，主要用于存储 RAG 文档片段和 Embedding 向量。

## 接口文档

启动后可访问 Knife4j / Swagger 页面查看在线接口文档：

```text
http://localhost:8123/api/doc.html
http://localhost:8123/api/swagger-ui.html
```

常用接口如下：

| 接口 | 方法 | 参数 | 说明 |
| :--- | :--- | :--- | :--- |
| `/api/health` | GET | 无 | 健康检查。 |
| `/api/ai/career_plan_app/chat/sync` | GET | `message`、`chatId` | 职业规划 Agent 同步对话。 |
| `/api/ai/career_plan_app/chat/sse` | GET | `message`、`chatId` | 职业规划 Agent 流式对话。 |
| `/api/ai/career_plan_app/rag/search` | GET | `query` | 查询本地职业规划知识库召回内容。 |
| `/api/ai/manus/chat` | GET | `message` | 调用 Manus Agent 进行多步任务处理，返回 SSE。 |

示例：

```bash
curl "http://localhost:8123/api/health"
curl "http://localhost:8123/api/ai/career_plan_app/chat/sync?message=我适合考研还是就业&chatId=demo"
curl "http://localhost:8123/api/ai/career_plan_app/rag/search?query=考研规划"
```

## 项目效果

项目启动后，用户可以在前端选择职业规划对话或 Manus Agent 对话。职业规划对话会结合本地知识库和多轮上下文，围绕用户的个人状态、目标和困惑给出建议；RAG 搜索接口可以直接验证资料召回内容；Manus Agent 可以调用工具完成多步骤任务。

典型效果：

- 对“我适合考研还是就业”这类开放问题，Agent 会先引导用户补充现状、目标和约束，再给出短期行动建议。
- 对“知识库里关于保研有什么方法”这类事实问题，Agent 会优先依据本地 Markdown 文档分点回答。
- 对复杂任务，Manus Agent 会进入思考、行动、观察的多步执行流程，并在需要时调用工具。

## 测试

```bash
.\mvnw.cmd test
```

或：

```bash
./mvnw test
```

## 文档导航

| 文档 | 此文档包含的内容 |
| :--- | :--- |
| [数据库脚本](docs/database/init.sql) | PostgreSQL + PGVector 可选初始化脚本。 |
| [技术架构图](docs/images/architecture.svg) | 项目整体技术架构。 |
| [图片搜索 MCP 说明](image-search-mcp/HELP.md) | MCP 图片搜索子模块说明。 |

## 参考

- [Spring AI](https://spring.io/projects/spring-ai)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [通义千问 DashScope](https://dashscope.aliyuncs.com/)
- [spring-ai-alibaba/DataAgent](https://github.com/spring-ai-alibaba/DataAgent)

## 许可证

本项目仅用于学习、交流和示例演示，请根据实际依赖与业务场景自行确认生产使用许可。
