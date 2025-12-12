package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🎓 CLIENTE DE ALOJAMENTO ESTUDANTIL");
        System.out.println("=====================================");

        try {
            connectToServer();

            // Enviar identificação como usuário normal
            out.println("USER");
            out.flush();

            System.out.println("✅ Conectado ao servidor!");

            boolean running = true;
            while (running) {
                // Ler e exibir resposta do servidor
                String response = readServerResponse();
                if (response.contains("Até logo") || response.contains("SAIR")) {
                    running = false;
                    continue;
                }

                // Pedir entrada do usuário
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("4") || input.equalsIgnoreCase("SAIR")) {
                    out.println("SAIR");
                    out.flush();
                } else {
                    out.println(input);
                    out.flush();
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erro de conexão: " + e.getMessage());
        } finally {
            disconnect();
            scanner.close();
        }
    }

    private static String readServerResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            if (line.equals("END")) break;
            response.append(line).append("\n");
        }

        System.out.print(response.toString());
        return response.toString();
    }

    private static void connectToServer() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    private static void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("🔒 Conexão encerrada.");
        } catch (IOException e) {
            System.err.println("⚠️ Erro ao fechar conexão: " + e.getMessage());
        }
    }
}