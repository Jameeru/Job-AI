package com.jobai.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK initialization.
 *
 * <p>The service account JSON is loaded from the path configured at
 * {@code jobai.firebase.service-account-path}. In development, place
 * {@code firebase-service-account.json} in {@code src/main/resources/}.
 * In production (Render), mount it as an environment secret file.
 *
 * <p>The Bean is idempotent — if a default FirebaseApp already exists
 * (e.g., in tests), initialization is skipped.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${jobai.firebase.project-id}")
    private String firebaseProjectId;

    @Value("${jobai.firebase.service-account-path}")
    private String serviceAccountPath;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Guard: skip if already initialized (e.g., in integration tests)
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase already initialized — reusing existing app.");
            return FirebaseApp.getInstance();
        }

        Resource resource = resourceLoader.getResource(serviceAccountPath);

        if (!resource.exists()) {
            throw new IllegalStateException(
                "Firebase service account file not found at: " + serviceAccountPath +
                ". Download it from Firebase Console → Project Settings → Service Accounts."
            );
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(firebaseProjectId)
                .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized for project: {}", firebaseProjectId);
            return app;
        }
    }
}
