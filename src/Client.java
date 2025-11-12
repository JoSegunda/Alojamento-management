import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String ServerIP = "127.0.0.1";
    private static final int ServerPort = 12345;

    public static void main(String[] args) {
        System.out.println("Cliente conectando...");

        try (
                Socket socket = new Socket(ServerIP, ServerPort);
                Scanner scanner = new Scanner(System.in);
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            System.out.println("Conectado ao servidor em " + ServerIP + ":" + ServerPort);

            String userInput;
            String line;

            // 🔹 Lê o menu inicial (antes de o utilizador digitar qualquer coisa)
            in.readLine();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }
            // NO CLIENTE - adicione esta lógica
            while (true) {
                System.out.print("Você: ");
                userInput = scanner.nextLine();
                out.println(userInput);

                // Se o usuário digitou "1", trata-se de um fluxo interativo
                if ("1".equals(userInput.trim())) {
                    System.out.println("🌀 Modo de registo ativado...");

                    // Ler e responder a cada prompt individualmente
                    String serverLine;
                    while ((serverLine = in.readLine()) != null) {
                        System.out.println("Servidor: " + serverLine);

                        // Se for uma linha vazia, sai do loop
                        if (serverLine.trim().isEmpty()) {
                            break;
                        }

                        // Se for um prompt (termina com ": "), pedir input ao usuário
                        if (serverLine.endsWith(": ") || serverLine.contains(":")) {
                            System.out.print("Sua resposta: ");
                            String resposta = scanner.nextLine();
                            out.println(resposta);
                        }
                    }
                } else {
                    // Comportamento normal para outros comandos
                    while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                        System.out.println("\nRESPOSTA DO SERVIDOR: " + line);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        } finally {
            System.out.println("Conexão encerrada.");
        }
    }
}
