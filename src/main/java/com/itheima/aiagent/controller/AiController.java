package com.itheima.aiagent.controller;

import com.itheima.aiagent.agent.YuManus;
import com.itheima.aiagent.app.CareerPlanApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private CareerPlanApp careerPlanApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 返回Flux响应对象
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/career_plan_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithCareerPlanAppSSE(String message, String chatId) {
        return careerPlanApp.doChatByStream(message, chatId);
    }

    @GetMapping("/career_plan_app/chat/sync")
    public String doChatWithCareerAppSync(String message, String chatId) {
        return careerPlanApp.doChat(message, chatId);
    }
    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        YuManus yuManus = new YuManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }
}
