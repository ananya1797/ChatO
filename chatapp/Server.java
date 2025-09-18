package com.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.api.core.ApiFuture;

public class Server {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<Socket, String> clientUsernames = new HashMap<>();
    private static Map<String, Long> lastTypingTime = new HashMap<>();

    public static void main(String[] args) {
        // Initialize Firebase
        FirebaseConfig.initializeFirebase();
        System.out.println("Firebase Initialized");

        System.out.println("Chat Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(5050)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // Request username from client
                out.println("Enter your username: ");
                username = in.readLine();
                System.out.println(username + " has joined the chat.");

                synchronized (clientWriters) {
                    clientWriters.add(out);
                    clientUsernames.put(socket, username);
                }

                // Broadcast join message
                sendToAll(username + " has joined the chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/file")) {
                        receiveFile();
                    } else if (message.equals("/typing")) {
                        sendToAll(username + " is typing...");
                    } else if (message.startsWith("/")) {
                        handleBotCommands(message);
                    } else {
                        System.out.println(username + ": " + message);
                        sendToAll(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                    clientUsernames.remove(socket);
                }
                sendToAll(username + " has left the chat.");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void saveMessageToFirestore(String sender, String message) {
            Firestore db = FirebaseConfig.getFirestore();
            if (db == null) {
                System.out.println("Firestore is not initialized");
                return;
            }

            CollectionReference messagesRef = db.collection("messages");
            Map<String, Object> msgData = new HashMap<>();
            msgData.put("sender", sender);
            msgData.put("message", message);
            msgData.put("timestamp", System.currentTimeMillis());

            // Save message synchronously
            ApiFuture<DocumentReference> future = messagesRef.add(msgData);
            try {
                DocumentReference docRef = future.get();
                System.out.println("Message saved in Firestore with ID: " + docRef.getId());
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Failed to save message: " + e.getMessage());
            }
        }

        private void sendToAll(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }

            // Save message to Firestore
            String[] parts = message.split(": ", 2);
            if (parts.length == 2) {
                saveMessageToFirestore(parts[0], parts[1]); // sender, message
            }
        }

        private void handleBotCommands(String command) {
            switch (command) {
                case "/help":
                    out.println("Bot: Available commands - /help, /joke, /time");
                    break;
                case "/joke":
                    out.println("Bot: " + fetchJoke());
                    break;
                case "/time":
                    out.println("Bot: Current time is " + new Date());
                    break;
                default:
                    out.println("Bot: Unknown command. Type /help for options.");
                    break;
            }
        }

        private String fetchJoke() {
            try {
                URL url = new URL("https://v2.jokeapi.dev/joke/Any?type=single");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONObject json = new JSONObject(response);
                return json.getString("joke");
            } catch (Exception e) {
                return "Couldn't fetch a joke at the moment";
            }
        }

        private void receiveFile() {
            try {
                String fileName = in.readLine();
                long fileSize = Long.parseLong(in.readLine());

                File file = new File("received_" + fileName);
                FileOutputStream fileOut = new FileOutputStream(file);
                byte[] buffer = new byte[4096];

                int bytesRead;
                long totalBytesRead = 0;
                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                fileOut.close();
                System.out.println("File received: " + file.getName());
                sendToAll(username + " has sent a file: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
