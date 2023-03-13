package com.kexon5.ddbot.conf;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleConfiguration {

    @Value("${spring.application.name}")
    private static String APPLICATION_NAME;
    @Bean
    public GoogleCredential credential(@Value("${google.service.account.key-path}") String keyFilePath,
                                       @Value("${google.service.account.user}") String internalUser) throws GeneralSecurityException, IOException {
        GoogleCredential credential =
                GoogleCredential.fromStream(
                        new FileInputStream(keyFilePath),
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance()
                );

        return new GoogleCredential.Builder()
                .setTransport(credential.getTransport())
                .setJsonFactory(credential.getJsonFactory())
                .setServiceAccountId(credential.getServiceAccountId())
                .setServiceAccountPrivateKey(credential.getServiceAccountPrivateKey())
                .setServiceAccountScopes(DriveScopes.all())
                .setServiceAccountUser(internalUser)
                .build();
    }

    @Bean
    public Drive drive(GoogleCredential credential) {
        return new Drive.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public Sheets sheets(GoogleCredential credential) {
        return new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
