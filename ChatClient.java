import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class ChatClient extends JFrame {
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JButton sendButton;
    private final JButton connectButton;
    private final JTextField serverIPField;
    private final JTextField userNameField;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;

    public ChatClient() {
        setTitle("Chat Client");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Create connection panel at the top
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        connectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel serverLabel = new JLabel("Server IP:");
        serverIPField = new JTextField("localhost", 12);
        
        JLabel nameLabel = new JLabel("Username:");
        userNameField = new JTextField(10);
        
        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        
        connectionPanel.add(serverLabel);
        connectionPanel.add(serverIPField);
        connectionPanel.add(nameLabel);
        connectionPanel.add(userNameField);
        connectionPanel.add(connectButton);
        
        add(connectionPanel, BorderLayout.NORTH);
        
        // Create chat area with scroll pane
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create message input panel at the bottom
        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setEnabled(false);
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        
        add(messagePanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void connectToServer() {
        if (connected) {
            return;
        }
        
        String serverIP = serverIPField.getText().trim();
        String userName = userNameField.getText().trim();
        
        if (serverIP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter server IP", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (userName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int port = 5000;
        
        try {
            socket = new Socket(serverIP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            connected = true;
            appendMessage("Connected to chat server\n");
            
            // Disable connection fields
            serverIPField.setEnabled(false);
            userNameField.setEnabled(false);
            connectButton.setEnabled(false);
            
            // Enable message fields
            messageField.setEnabled(true);
            sendButton.setEnabled(true);
            messageField.requestFocus();
            
            // Send username
            out.println(userName);
            
            // Start thread to receive messages
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            final String msg = message;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    appendMessage(msg + "\n");
                                }
                            });
                        }
                    } catch (IOException e) {
                        if (connected) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    appendMessage("Connection lost\n");
                                    disconnect();
                                }
                            });
                        }
                    }
                }
            }).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void sendMessage() {
        if (!connected || out == null) {
            return;
        }
        
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }
    
    private void appendMessage(String message) {
        chatArea.append(message);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    private void disconnect() {
        connected = false;
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClient();
            }
        });
    }
}