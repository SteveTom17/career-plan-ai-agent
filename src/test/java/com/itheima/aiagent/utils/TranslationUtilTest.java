package com.itheima.aiagent.utils;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class TranslationUtilTest {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApi;

    @Resource
    private ChatClient.Builder chatClientBuilder;

    @Test
    void translationToEnglish() {
        assertNotNull(dashScopeApi, "DashScope API Key 没有读取到");
        assertFalse(dashScopeApi.isBlank(), "DashScope API Key 不能为空");

        assertNotNull(chatClientBuilder, "ChatClient.Builder 注入失败");

        String sourceText = "Hvad er Danmarks hovedstad?";

        String result = TranslationUtil.translateToEnglish(chatClientBuilder, sourceText);

        assertNotNull(result);
        assertFalse(result.isBlank());

        System.out.println("========== 翻译测试结果 ==========");
        System.out.println("原文：" + sourceText);
        System.out.println("译文：" + result);
    }
}