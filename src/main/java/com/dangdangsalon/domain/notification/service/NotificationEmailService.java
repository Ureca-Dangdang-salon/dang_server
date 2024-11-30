package com.dangdangsalon.domain.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
}
