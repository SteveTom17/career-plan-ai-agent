package com.itheima.aiagent.utils;

import com.itheima.aiagent.tools.EmailNoticeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailNoticeUtilTest {

    @Autowired
    private EmailNoticeUtil emailNoticeUtil;

    /**
     * 读取 application.yml 中配置的真实邮箱
     */
    @Value("${spring.mail.username}")
    private String realEmail;

    /**
     * 测试登录成功通知邮件
     */
    @Test
    void testSendLoginSuccessNotice() throws InterruptedException {
        emailNoticeUtil.sendLoginSuccessNotice(
                realEmail,
                "Steve",
                "127.0.0.1"
        );

        /*
         * 因为 EmailNoticeUtil 中使用了 @Async 异步发送邮件，
         * 所以这里等待几秒，避免测试方法结束太快。
         */
        Thread.sleep(5000);
    }

    /**
     * 测试注册欢迎邮件
     */
    @Test
    void testSendRegisterWelcomeNotice() throws InterruptedException {
        emailNoticeUtil.sendRegisterWelcomeNotice(
                realEmail,
                "Steve"
        );

        Thread.sleep(5000);
    }
}