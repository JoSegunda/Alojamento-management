package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AdminClient {
    private static final String ServerIP = "127.0.0.1";
    private static final int ServerPort = 12345;

    public static void main(String[] args) {
        System.out.println("Cliente Admin conectando...");

        try (
                Socket socket = new Socket(ServerIP, ServerPort);
                Scanner scanner = new Scanner(System.in);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            System.out.println("Conectado ao servidor em " + ServerIP + ":" + ServerPort);

            // Autenticação admin
            out.println("ADMIN");
            out.flush();

            String userInput;
            String line;

            // Lê o menu inicial do admin
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            while (true) {
                System.out.print("\nAdmin: ");
                userInput = scanner.nextLine();

                out.println(userInput);

                if ("SAIR".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("A sair...");
                    break;
                }

                // 🔹 Processar resposta do servidor
                while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                    System.out.println("Servidor: " + line);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        } finally {
            System.out.println("Conexão admin encerrada.");
        }
    }
}
