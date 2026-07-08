package com.itheima.aiagent.controller;

import com.itheima.aiagent.YuManus;
import com.itheima.aiagent.app.CareerPlanApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
     * 同步接口
     * @param message
     * @param chatId
     * @return
     */
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
