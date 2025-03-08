import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

import java.io.IOException;
import java.io.InputStream;

public class FirebaseConfig {
    private static Firestore firestore;

    public static void initializeFirebase() throws IOException {
        InputStream serviceAccount = FirebaseConfig.class.getClassLoader().getResourceAsStream("firebase-adminsdk.json");

        if (serviceAccount == null) {
            throw new IOException("Firebase JSON file not found in resources!");
        }

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://your-database-name.firebaseio.com") // Replace with actual Firebase database URL
                .build();

        FirebaseApp.initializeApp(options);
        firestore = FirestoreClient.getFirestore();
        System.out.println("🔥 Firebase Initialized Successfully!");
    }

    public static Firestore getFirestore() {
        return firestore;
    }
}
