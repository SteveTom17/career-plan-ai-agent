package com.itheima.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class CareerPlanAppTest {

    @Resource
    private CareerPlanApp careerPlanApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我叫小明，是一名大三学生";
        String answer = careerPlanApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想毕业后直接找工作";
        answer = careerPlanApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我叫什么来着";
        answer = careerPlanApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }
    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是一名大三软件工程的学生，我想毕业后直接找工作，但我不知道该怎么做";
        CareerPlanApp.CareerPlanReport loveReport = careerPlanApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是一名大三软工学生，保研失败后，有哪些备选方案可以选择";
        CareerPlanApp.CareerPlanReport loveReport = careerPlanApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }
}