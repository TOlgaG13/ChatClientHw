package homework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter your login: ");
            String login = scanner.nextLine();
            System.out.println("Enter your password: ");
            String password = scanner.nextLine();

            // Отправка данных для авторизации на сервер
            int authResponse = authenticate(login, password);
            if (authResponse != 200) {
                System.out.println("Authentication failed: " + authResponse);
                return;
            }

            Thread th = new Thread(new GetThread());
            th.setDaemon(true);
            th.start();

            System.out.println("Enter your message: ");
            while (true) {
                String text = scanner.nextLine();
                if (text.isEmpty()) break;

                System.out.println("Enter recipient (or leave empty for public message): ");
                String recipient = scanner.nextLine();
                Message m = new Message(login, recipient, text);

                int res = m.send(Utils.getURL() + "/add");

                if (res != 200) { // 200 OK
                    System.out.println("HTTP error occurred: " + res);
                    return;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static int authenticate(String login, String password) throws IOException {
        URL url = new URL("http://localhost:8088/ChatServerHw_war_exploded/auth");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String authData = "login=" + login + "&password=" + password;
        try (OutputStream os = conn.getOutputStream()) {
            os.write(authData.getBytes(StandardCharsets.UTF_8));
            return conn.getResponseCode();
        }
    }

    private static void getOnlineUsers() throws IOException {
        URL url = new URL(Utils.getURL() + "/online");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStream is = conn.getInputStream()) {
            byte[] buf = responseBodyToArray(is);
            String strBuf = new String(buf, StandardCharsets.UTF_8);
            System.out.println("Online users: " + strBuf);
        }
    }

    private static void checkUserStatus(String user) throws IOException {
        URL url = new URL(Utils.getURL() + "/status?user=" + user);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStream is = conn.getInputStream()) {
            byte[] buf = responseBodyToArray(is);
            String strBuf = new String(buf, StandardCharsets.UTF_8);
            System.out.println("User status: " + strBuf);
        }
    }

    private static byte[] responseBodyToArray(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[10240];
        int r;

        do {
            r = is.read(buf);
            if (r > 0) bos.write(buf, 0, r);
        } while (r != -1);

        return bos.toByteArray();
    }
}