package com.dangdangsalon.domain.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(String userEmail, String subject, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            javaMailSender.send(message);
            log.info("이메일 발송 성공 {}", userEmail);

        } catch (Exception e) {
            log.error("이메일 발송 실패 {}: ", userEmail, e);
        }
    }

    public void sendEmailWithTemplate(String userEmail, String subject, String templatePath, Map<String, String> placeholders) {
        try {
            String template = loadTemplate(templatePath);

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                template = template.replace("{" + entry.getKey() + "}",
                        entry.getValue() != null ? entry.getValue() : "");
            }

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setSubject(subject);
            helper.setText(template, true);

            javaMailSender.send(message);
            log.info("이메일 발송 성공 {}", userEmail);

        } catch (Exception e) {
            log.error("이메일 템플릿 발송 실패 {}: ", userEmail, e);
        }
    }

    private String loadTemplate(String templatePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(templatePath), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
