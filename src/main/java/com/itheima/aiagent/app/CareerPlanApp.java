package com.itheima.aiagent.app;

import com.itheima.aiagent.advisor.MyLoggerAdvisor;
import com.itheima.aiagent.chatmemory.FileBasedChatMemory;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import java.util.List;

@Component
@Slf4j
public class CareerPlanApp {


    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "# 角色\n" +
            "你是一位资深职业规划专家，风格温暖、善于提问。不直接给答案，而是通过问题引导用户自我探索。\n" +
            "\n" +
            "# 任务\n" +
            "模拟真实咨询场景，每次回复至少提出2个引导性问题，逐步了解用户的：\n" +
            "- 兴趣、优势、价值观\n" +
            "- 职业现状与困扰\n" +
            "- 成就与挫败经历\n" +
            "- 目标与阻碍\n" +
            "\n" +
            "当信息足够时，用简单表格或分点归纳用户画像，再给出2-3条与用户特质相关的短期行动建议。\n" +
            "\n" +
            "# 准则\n" +
            "- 禁止替用户做决定，只提供探索方向\n" +
            "- 遇到矛盾信息时，接纳并引导对比感受\n" +
            "- 语言平实，回复控制在200字以内\n" +
            "- 结尾邀请用户继续分享\n" +
            "\n" +
            "# 开场白\n" +
            "你好！我是你的职业规划伙伴。为了更好帮你，请先选一个回答：\n" +
            "1. 你目前是在职、学生，还是求职/转型期？\n" +
            "2. 理想工作状态的三个关键词是什么？\n" +
            "3. 最近一次工作成就感来自哪里？\n" +
            "请随意聊聊。";

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
                .build();

    }
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
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
                .advisors(
                        QuestionAnswerAdvisor.builder(careerPlanAppVectorStore).build()
                )
                .call()
                .entity(CareerPlanReport.class);
        log.info("CareerPlanReport: {}", careerPlanReport);
        return careerPlanReport;
    }

//     本地知识库查找
    @Resource
    private VectorStore careerPlanAppVectorStore;

    @Resource
    private Advisor careerPlanAppRagCloudAdvisor;

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
                // 应用知识库问答
//                .advisors(new QuestionAnswerAdvisor(careerPlanAppVectorStore))
//                .advisors(
//                        QuestionAnswerAdvisor.builder(careerPlanAppVectorStore).build()
//                )
//                 应用增强检索服务
//                .advisors(CareerPlanAppCloudAdvisor)
                .advisors(careerPlanAppRagCloudAdvisor)
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

}
