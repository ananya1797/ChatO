package com.chatapp;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private BufferedReader in;
    private PrintWriter out;
    private DataOutputStream dataOutputStream;
    private String username;
    private Timer typingTimer;

    public Client() {
        username = JOptionPane.showInputDialog("Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty!");
            return;
        }

        JFrame frame = new JFrame("Chat - " + username);
        JTextArea messageArea = new JTextArea(20, 40);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JTextField textField = new JTextField(30);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel typingLabel = new JLabel(" ");
        typingLabel.setForeground(Color.GRAY);
        JButton sendFileButton = new JButton("Send File");
        sendFileButton.setBackground(new Color(50, 150, 250));
        sendFileButton.setForeground(Color.WHITE);
        sendFileButton.setBorderPainted(false);
        sendFileButton.setFocusPainted(false);

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(textField);
        panel.add(sendFileButton);

        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(typingLabel, BorderLayout.NORTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket socket = new Socket("localhost", 5050);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            out.println(username);

            // Handle message sending
            textField.addActionListener(e -> {
                String text = textField.getText();
                if (!text.isEmpty()) {
                    out.println(text);
                    textField.setText("");
                }
            });

            // Handle "Typing..." indicator
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    out.println("/typing");
                    if (typingTimer != null) {
                        typingTimer.cancel();
                    }
                    typingTimer = new Timer();
                    typingTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            typingLabel.setText(" ");
                        }
                    }, 2000);
                }
            });

            // Handle file sending
            sendFileButton.addActionListener(e -> sendFile());

            // Handle incoming messages
            String message;
            while ((message = in.readLine()) != null) {
                if (message.endsWith("is typing...")) {
                    typingLabel.setText(message);
                } else if (message.startsWith(" ")) {
                    handleFileNotification(message, messageArea);
                } else {
                    typingLabel.setText(" ");
                    messageArea.append(message + "\n");
                    showNotification("New Message", message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                out.println("/file");
                out.println(file.getName());
                out.println(file.length());

                FileInputStream fileIn = new FileInputStream(file);
                byte[] buffer = new byte[4096];

                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }

                fileIn.close();
                dataOutputStream.flush();
                JOptionPane.showMessageDialog(null, "File sent: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileNotification(String message, JTextArea messageArea) {
        messageArea.append(message + "\n");
        String fileName = message.split(": ")[1];

        int option = JOptionPane.showConfirmDialog(null,
                "A file has been received: " + fileName + "\nDo you want to open it?",
                "File Received",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(new File(fileName));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to open file.");
            }
        }
    }

    private void showNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");

            TrayIcon trayIcon = new TrayIcon(image, "Chat App");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Chat App Notification");
            try {
                tray.add(trayIcon);
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
