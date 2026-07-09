package com.itheima.aiagent.app;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.itheima.aiagent.advisor.MyLoggerAdvisor;
import com.itheima.aiagent.chatmemory.FileBasedChatMemory;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class CareerPlanApp {


    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";
    private static final int KNOWLEDGE_TOP_K = 5;
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "# 角色\n" +
            "你是一位资深职业规划专家，风格温暖、善于提问。\n" +
            "\n" +
            "# 任务\n" +
            "在职业咨询场景中，通过问题引导用户自我探索，逐步了解用户的：\n" +
            "- 兴趣、优势、价值观\n" +
            "- 职业现状与困扰\n" +
            "- 成就与挫败经历\n" +
            "- 目标与阻碍\n" +
            "\n" +
            "当信息足够时，用简单表格或分点归纳用户画像，再给出2-3条与用户特质相关的短期行动建议。\n" +
            "\n" +
            "# 知识库问答规则\n" +
            "- 当用户询问“知识库里提到什么”“资料中有哪些方法”“根据文档回答”等事实性问题时，优先根据检索到的知识库内容直接回答。\n" +
            "- 知识库问答不要套用咨询开场白，不要强行追问，也不要延续上一轮话题。\n" +
            "- 如果知识库内容不足，明确说明“本地知识库中没有找到相关内容”，再给出可继续查询的方向。\n" +
            "\n" +
            "# 准则\n" +
            "- 禁止替用户做决定，只提供探索方向\n" +
            "- 遇到矛盾信息时，接纳并引导对比感受\n" +
            "- 职业咨询类回复至少提出2个引导性问题，并在结尾邀请用户继续分享\n" +
            "- 知识库问答类回复用简洁分点归纳，控制在300字以内\n" +
            "\n" +
            "# 开场白\n" +
            "你好！我是你的职业规划伙伴。为了更好帮你，请先选一个回答：\n" +
            "1. 你目前是在职、学生，还是求职/转型期？\n" +
            "2. 理想工作状态的三个关键词是什么？\n" +
            "3. 最近一次工作成就感来自哪里？\n" +
            "请随意聊聊。";

    private static final PromptTemplate KNOWLEDGE_PROMPT_TEMPLATE = new PromptTemplate("""
            用户问题：
            {query}

            本地知识库检索内容如下：
            ---------------------
            {question_answer_context}
            ---------------------

            请按问题类型回答：
            1. 如果用户询问“知识库里提到什么”“资料中有哪些方法”“根据文档回答”等事实性问题，
               只依据上面的本地知识库内容直接分点回答，不要复述无关的对话历史，不要继续追问用户。
            2. 如果用户是在进行普通职业咨询，请把知识库内容作为参考，并继续遵守系统提示中的职业咨询规则。
            3. 如果检索内容没有覆盖用户的事实性问题，请明确说明“本地知识库中没有找到相关内容”。
            """);

    record CareerPlanReport(String title, List<String> suggestions) {
    }
    public CareerPlanApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(10)
//                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .defaultOptions(DashScopeChatOptions.builder()
                        .build())
                .build();

    }
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(knowledgeAdvisor())
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    public CareerPlanReport doChatWithReport(String message, String chatId) {
        CareerPlanReport careerPlanReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成职业规划结果，标题为{用户名}的职业规划报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(knowledgeAdvisor())
                .call()
                .entity(CareerPlanReport.class);
        log.info("CareerPlanReport: {}", careerPlanReport);
        return careerPlanReport;
    }

//     本地知识库查找
    @Resource
    private VectorStore careerPlanAppVectorStore;

    @Resource
    private ToolCallback[] allTools;

    /**
     * 和RAG知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                //应用知识库问答
                //.advisors(new QuestionAnswerAdvisor(careerPlanAppVectorStore))
                .advisors(knowledgeAdvisor())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI调用工具的能力
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    /**
     * 基于 MCP 协议的对话，支持调用 MCP 工具（如图片搜索等）
     * @param message 用户消息
     * @param chatId 会话 ID，用于记忆管理
     * @return AI 回复内容
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())          // 记录日志，便于调试
                .toolCallbacks(allTools)                 // 注册所有工具（包含 MCP 工具）
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("MCP 对话响应: {}", content);
        return content;
    }
    /**
     * 流式方法
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(knowledgeAdvisor())
                .stream()
                .content()
                .timeout(Duration.ofSeconds(120));
    }

    public List<String> searchLocalKnowledge(String query) {
        return careerPlanAppVectorStore
                .similaritySearch(SearchRequest.builder()
                        .query(query)
                        .topK(KNOWLEDGE_TOP_K)
                        .similarityThresholdAll()
                        .build())
                .stream()
                .map(Document::getText)
                .toList();
    }

    private Advisor knowledgeAdvisor() {
        return QuestionAnswerAdvisor.builder(careerPlanAppVectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(KNOWLEDGE_TOP_K)
                        .similarityThresholdAll()
                        .build())
                .promptTemplate(KNOWLEDGE_PROMPT_TEMPLATE)
                .build();
    }
}
