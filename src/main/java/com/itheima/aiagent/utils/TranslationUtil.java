package com.itheima.aiagent.utils;

import cn.hutool.core.lang.Assert;
import org.springframework.ai.chat.client.ChatClient;


public class TranslationUtil {

    public static String translateToEnglish(ChatClient.Builder chatClientBuilder, String text) {
        Assert.notNull(chatClientBuilder, "chatClientBuilder cannot be null");

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .system("""
                        You are a professional translation engine.
                        Translate the user's text into Chinese.
                        Only output the translated result.
                        Do not explain.
                        """)
                .user(text)
                .call()
                .content();
    }
}