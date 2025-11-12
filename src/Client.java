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
            System.out.println("Digite 'SAIR' para desconectar.\n");
            System.out.println("Pressione qualquer tecla para continuar");

            String userInput;
            String line;

            // 🔹 Lê o menu inicial (antes de o utilizador digitar qualquer coisa)
            in.readLine();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            while (true) {
                System.out.print("Você: ");
                userInput = scanner.nextLine();

                out.println(userInput);

                if (userInput.equalsIgnoreCase("SAIR")) {
                    System.out.println("Encerrando sessão...");
                    break;
                }
                // 🔹 Ler múltiplas linhas até o servidor mandar uma linha vazia
                while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                    System.out.println("\nRESPOSTA DO SERVIDOR: "+line);

                }
            }

        } catch (Exception e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        } finally {
            System.out.println("Conexão encerrada.");
        }
    }
}
