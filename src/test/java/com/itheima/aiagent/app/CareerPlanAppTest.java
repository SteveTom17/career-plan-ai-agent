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
        String message = "你好，我是大三学生";
        String answer = careerPlanApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想毕业后直接找工作";
        answer = careerPlanApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我是什么职业来着";
        answer = careerPlanApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }
}