package com.chatapp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {
    private static Firestore firestore;

    public static void initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/google-services.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://chato-d3f7b.firebaseio.com")// Replace with your Firebase URL
                    .build();

            FirebaseApp.initializeApp(options);
            firestore = FirestoreClient.getFirestore();
            System.out.println("🔥 Firebase Initialized Successfully!");

        } catch (IOException e) {
            System.err.println("Firebase Initialization Failed: " + e.getMessage());
        }
    }

    public static Firestore getFirestore() {
        return firestore;
    }
}


