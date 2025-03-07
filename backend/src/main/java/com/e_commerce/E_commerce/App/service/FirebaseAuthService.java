package com.e_commerce.E_commerce.App.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class FirebaseAuthService {

    @Value("${CLIENT_EMAIL}")
    private String clientEmail;

    @Value("${PRIVATE_KEY}")
    private String privateKey;

    @Value("${PROJECT_ID}")
    private String projectId;

    @PostConstruct
    private void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                String formattedPrivateKey = privateKey.replace("\\n", "\n");

                String json = String.format("{\"type\": \"service_account\", \"project_id\": \"%s\", \"private_key_id\": \"\", \"private_key\": \"%s\", \"client_email\": \"%s\", \"client_id\": \"\", \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\", \"token_uri\": \"https://oauth2.googleapis.com/token\", \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\", \"client_x509_cert_url\": \"\"}", projectId, formattedPrivateKey, clientEmail);

                GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initializing firebase: " + e.getMessage(), e);
        }
    }

    public String verifyIdToken(String idToken) throws Exception {
        //System.out.println("Received token: " + idToken);
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        //System.out.println("Decoded user ID: " + decodedToken.getUid());
        return decodedToken.getUid();
    }

}
