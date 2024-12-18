package com.dangdangsalon.domain.notification.initializer;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
@Slf4j
@Component
public class FCMInitializer {

    @Value("${firebase.service-account-key}")
    private String base64EncodedKey;

    @PostConstruct
    public void initialize() {

        try {
            if (base64EncodedKey == null || base64EncodedKey.isEmpty()) {
                throw new IllegalArgumentException("Firebase Service Account Key가 설정되지 않았습니다.");
            }
            // Base64 디코딩
            byte[] decodedKey = Base64.getDecoder().decode(base64EncodedKey);

            // Firebase 초기화
            try (InputStream credentialsStream = new ByteArrayInputStream(decodedKey)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                        .build();
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("FirebaseApp initialization complete");
                }
            }
        } catch (Exception e) {
            log.error("FirebaseApp 초기화 중 오류 발생", e);
        }
    }
}
