package com.itheima.aiagent.tools;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNoticeUtil {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 发送登录成功通知邮件
     *
     * @param to 用户邮箱
     * @param username 用户名
     * @param loginIp 登录 IP，可为空
     */
    @Async("mailTaskExecutor")
    public void sendLoginSuccessNotice(String to, String username, String loginIp) {
        if (!isValidEmail(to)) {
            log.warn("邮件发送失败：邮箱格式不合法，email={}", to);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("登录成功通知");

            String safeUsername = HtmlUtils.htmlEscape(username == null ? "用户" : username);
            String loginTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);

            String htmlContent = buildLoginSuccessHtml(safeUsername, loginTime, loginIp);

            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            log.info("登录成功通知邮件发送成功，email={}", to);
        } catch (MessagingException | MailException e) {
            log.error("登录成功通知邮件发送失败，email={}", to, e);
        }
    }

    /**
     * 发送注册欢迎邮件
     *
     * @param to 用户邮箱
     * @param username 用户名
     */
    @Async("mailTaskExecutor")
    public void sendRegisterWelcomeNotice(String to, String username) {
        if (!isValidEmail(to)) {
            log.warn("邮件发送失败：邮箱格式不合法，email={}", to);
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("欢迎注册 AI-Agent 平台");

            String safeUsername = HtmlUtils.htmlEscape(username == null ? "用户" : username);

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.8;">
                        <h2>欢迎你，%s！</h2>
                        <p>你已成功注册 AI-Agent 平台。</p>
                        <p>在这里，你可以使用智能 Agent 辅助完成学习、开发、知识管理与自动化任务。</p>
                        <p style="color: #666;">如果本次注册不是你本人操作，请忽略此邮件或及时修改账户密码。</p>
                    </div>
                    """.formatted(safeUsername);

            helper.setText(htmlContent, true);

            javaMailSender.send(message);

            log.info("注册欢迎邮件发送成功，email={}", to);
        } catch (MessagingException | MailException e) {
            log.error("注册欢迎邮件发送失败，email={}", to, e);
        }
    }

    private String buildLoginSuccessHtml(String username, String loginTime, String loginIp) {
        String ipText = loginIp == null || loginIp.isBlank()
                ? "未知 IP"
                : HtmlUtils.htmlEscape(loginIp);

        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.8;">
                    <h2>欢迎你，%s！</h2>
                    <p>你的 AI-Agent 平台账号已成功登录。</p>
                    <p><strong>登录时间：</strong>%s</p>
                    <p><strong>登录 IP：</strong>%s</p>
                    <p style="color: #666;">
                        如果本次登录不是你本人操作，请立即修改密码或联系管理员。
                    </p>
                </div>
                """.formatted(username, loginTime, ipText);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}