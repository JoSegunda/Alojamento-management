package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AdminClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("ADMINISTRADOR - SISTEMA DE ALOJAMENTO");
        System.out.println("=========================================");

        try {
            connectToServer();

            // Identificar como admin
            out.println("ADMIN");
            out.flush();

            System.out.println("Conectado como administrador!");

            boolean running = true;
            while (running) {
                try {
                    // Ler resposta do servidor
                    String response = readServerResponse();

                    if (response == null) {
                        System.out.println("Conexão com o servidor perdida.");
                        break;
                    }

                    // Mostrar resposta
                    if (!response.isEmpty()) {
                        System.out.println(response);
                    }

                    // Se a resposta contiver "SAIR|", terminar
                    if (response.startsWith("SAIR|")) {
                        running = false;
                        continue;
                    }

                    // Pedir comando do admin
                    System.out.print("\nAdmin> ");
                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("SAIR")) {
                        out.println("SAIR");
                        out.flush();
                        running = false;
                    } else if (!input.isEmpty()) {
                        out.println(input);
                        out.flush();
                    }

                } catch (IOException e) {
                    System.out.println("Erro de comunicação: " + e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Erro de conexão: " + e.getMessage());
        } finally {
            disconnect();
            scanner.close();
            System.out.println("Sessão admin encerrada.");
        }
    }

    private static String readServerResponse() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;

        // Ler até encontrar "END" ou null
        while ((line = in.readLine()) != null) {
            if (line.equals("END")) {
                break;
            }
            response.append(line).append("\n");
        }

        // Se a conexão foi fechada
        if (line == null && response.length() == 0) {
            return null;
        }

        return response.toString().trim();
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}