import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class CohereChatGUI extends JFrame {
    private JTextArea inputArea;
    private JTextPane outputArea;
    private JButton sendButton;
    private JButton clearButton;

    public CohereChatGUI() {
        setTitle("Cohere Chat Assistant");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(1, 2)); // Split GUI into two sections

        // Left Panel for User Input
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea();
        inputArea.setFont(new Font("Arial", Font.PLAIN, 16));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("User Input:"));
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // Right Panel for Assistant Response
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputArea = new JTextPane();
        outputArea.setContentType("text/html");
        outputArea.setFont(new Font("Arial", Font.PLAIN, 16));
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Assistant's Response:"));
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);

        mainPanel.add(inputPanel);
        mainPanel.add(outputPanel);
        add(mainPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));
        sendButton.addActionListener(e -> new Thread(this::sendMessage).start());

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 16));
        clearButton.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
        });

        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private void sendMessage() {
        String apiKey = "ZDwdsoZGEYEPXc1io92V9zX1IYVPKFENp03vgCyr";
        String userMessage = inputArea.getText().trim();

        if (userMessage.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String endpoint = "https://api.cohere.com/v2/chat";
        String requestBody = String.format("{\"model\":\"command-a-03-2025\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}", userMessage);

        try {
            URL url = new URI(endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            System.out.println("API Response: " + response.toString()); // Print response in console
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            String assistantMessage = jsonResponse.getJSONObject("message")
                .getJSONArray("content")
                .getJSONObject(0)
                .optString("text", "No response text found.");

            // Format text for display
            String formattedMessage = assistantMessage.replace("###", "<h2>")
                                                      .replace("**", "<b>")
                                                      .replace("```", "<pre>");

            SwingUtilities.invokeLater(() -> outputArea.setText("<html><body>" + formattedMessage + "</body></html>"));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> outputArea.setText("An error occurred. Please try again."));
        }
    }

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog(null, "Enter username:", "Login", JOptionPane.PLAIN_MESSAGE);
        String password = JOptionPane.showInputDialog(null, "Enter password:", "Login", JOptionPane.PLAIN_MESSAGE);

        if ("admin".equals(username) && "1234".equals(password)) {
            SwingUtilities.invokeLater(() -> new CohereChatGUI().setVisible(true));
        } else {
            JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}