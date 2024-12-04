package com.dangdangsalon.domain.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("NotificationEmailServiceTest")
class NotificationEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationEmailService notificationEmailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("이메일 전송 성공")
    void sendEmail_Success() throws Exception {
        // Given
        String userEmail = "test1234@naver.com";
        String subject = "내일 떙땡시에 미용 예약이 되어있습니다!!";
        String body = "<p>잊지 말아주세여</p>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        notificationEmailService.sendEmail(userEmail, subject, body);

        // Then
        verify(javaMailSender, times(1)).send(mimeMessage);
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertThat(sentMessage).isNotNull();
    }

    @Test
    @DisplayName("이메일 전송 실패")
    void sendEmail_Failure() {
        // Given
        String userEmail = "invalid-email";
        String subject = "내일 떙땡시에 미용 예약이 되어있습니다!!";
        String body = "<p>잊지 말아주세여</p>";

        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("이메일 생성 실패"));

        // When
        notificationEmailService.sendEmail(userEmail, subject, body);

        // Then
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
}
