import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LUMORACHAT {
    private static final String API_KEY = "ZDwdsoZGEYEPXc1io92V9zX1IYVPKFENp03vgCyr";
    private static final String ENDPOINT = "https://api.cohere.com/v2/chat";
    private static final String HISTORY_FILE = "chat_history.txt";
    private static final String FEEDBACK_FILE = "feedback.txt";
    private static final String[] PROMPT_SUGGESTIONS = {
        "Explain quantum computing in simple terms.",
        "How do I improve my resume?",
        "Give me a 5-day meal plan for vegetarians.",
        "What's the difference between machine learning and deep learning?",
        "Write a professional email for a job application."
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("**************************************************");
        System.out.println("*                                                *");
        System.out.println("*                    LUMORA                      *");
        System.out.println("*         Illuminating the world of books        *");
        System.out.println("*                                                *");
        System.out.println("*     Developed by: Prathamesh, Karan, Adhish    *");
        System.out.println("**************************************************\n");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if ("admin".equals(username) && "1234".equals(password)) {
            adminMode(scanner);
        } else {
            userMode(scanner, username);
        }
        scanner.close();
    }

    private static void adminMode(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Admin Panel ---");
            System.out.println("1. View Chat History");
            System.out.println("2. View Feedback");
            System.out.println("3. Switch to User Mode");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewChatHistory();
                    break;
                case 2:
                    viewFeedback();
                    break;
                case 3:
                    System.out.println("Switching to user mode...");
                    userMode(scanner, "admin");
                    return;
                case 4:
                    System.out.println("Exiting admin mode...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewChatHistory() {
        System.out.println("\n--- Chat History ---");
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("No chat history found.");
        }
    }

    private static void viewFeedback() {
        System.out.println("\n--- User Feedback ---");
        try (BufferedReader br = new BufferedReader(new FileReader(FEEDBACK_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("No feedback available.");
        }
    }

    private static void userMode(Scanner scanner, String username) {
        System.out.println("\nWelcome, " + username + "!");
        System.out.println("Type your message below.");
        System.out.println("Commands: 'exit' to quit | 'menu' for admin panel | 'review' to leave feedback | 'suggest' for prompt ideas");

        while (true) {
            System.out.print("\nYou: ");
            String userMessage = scanner.nextLine().trim();

            if ("menu".equalsIgnoreCase(userMessage)) {
                adminMode(scanner);
                return;
            } else if ("exit".equalsIgnoreCase(userMessage)) {
                System.out.println("Goodbye!");
                break;
            } else if ("review".equalsIgnoreCase(userMessage)) {
                collectFeedback(scanner, username);
                continue;
            } else if ("suggest".equalsIgnoreCase(userMessage)) {
                showRecommendedPrompts();
                continue;
            }

            String response = sendMessage(userMessage);
            System.out.println("Assistant: " + formatText(response));
            saveToHistory(username, userMessage, response);
        }
    }

    private static String sendMessage(String userMessage) {
        try {
            String requestBody = String.format(
                "{\"model\":\"command-a-03-2025\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                userMessage);

            URL url = new URI(ENDPOINT).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
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

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONObject("message")
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .optString("text", "No response text found.");

        } catch (Exception e) {
            return "An error occurred. Please try again.";
        }
    }

    private static String formatText(String text) {
    // ANSI color codes
    final String RESET = "\u001B[0m";
    final String LIGHT_BLUE = "\u001B[94;1m";
    final String YELLOW_BOLD = "\u001B[93;1m";
    final String GREEN_BOLD = "\u001B[92;1m";
    final String CYAN_BG_BLACK_TEXT = "\u001B[30;106m"; // black on cyan background

    text = text.replaceAll(
        "###\\s*(.*?)\\s*",
        "\n=== " + LIGHT_BLUE + "$1" + RESET + " ===\n"
    );

    // **Bold** alternates between yellow and green
    StringBuffer boldBuffer = new StringBuffer();
    java.util.regex.Matcher boldMatcher = java.util.regex.Pattern.compile("\\*\\*(.*?)\\*\\*").matcher(text);
    boolean toggle = true;
    while (boldMatcher.find()) {
        String color = toggle ? YELLOW_BOLD : GREEN_BOLD;
        boldMatcher.appendReplacement(boldBuffer, color + boldMatcher.group(1) + RESET);
        toggle = !toggle;
    }
    boldMatcher.appendTail(boldBuffer);
    text = boldBuffer.toString();

    // ```code``` blocks
    StringBuffer codeBuffer = new StringBuffer();
    java.util.regex.Matcher codeMatcher = java.util.regex.Pattern.compile("```(.*?)```", java.util.regex.Pattern.DOTALL).matcher(text);
    while (codeMatcher.find()) {
        String code = codeMatcher.group(1);
        String formattedCode = CYAN_BG_BLACK_TEXT + code + RESET;
        codeMatcher.appendReplacement(codeBuffer, formattedCode);
    }
    codeMatcher.appendTail(codeBuffer);

    return codeBuffer.toString();
}




    private static void saveToHistory(String username, String userMessage, String response) {
        try (FileWriter writer = new FileWriter(HISTORY_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(String.format("%s | %s -> %s\n", timestamp, username, userMessage));
            writer.write(String.format("%s | Assistant -> %s\n", timestamp, response));
        } catch (Exception e) {
            System.out.println("Error saving chat history.");
        }
    }

    private static void collectFeedback(Scanner scanner, String username) {
        try (FileWriter writer = new FileWriter(FEEDBACK_FILE, true)) {
            System.out.print("Enter star rating (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter your feedback: ");
            String feedback = scanner.nextLine();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(String.format("%s | %s | Rating: %d | Feedback: %s\n", timestamp, username, rating, feedback));
            System.out.println("Thank you for your feedback!");
        } catch (Exception e) {
            System.out.println("Error saving feedback.");
        }
    }

    private static void showRecommendedPrompts() {
        System.out.println("\n--- Recommended Prompts ---");
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            int index = random.nextInt(PROMPT_SUGGESTIONS.length);
            System.out.println("- " + PROMPT_SUGGESTIONS[index]);
        }

        System.out.println("\n--- Previous Prompts ---");
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null && count < 3) {
                if (line.contains("->")) {
                    String[] parts = line.split("->");
                    if (parts.length > 1) {
                        System.out.println("- " + parts[1].trim());
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("No previous prompts available.");
        }
    }
}
