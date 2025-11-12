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

            while (true) {
                System.out.println("\n--- MENU DO CANDIDATO ---");
                System.out.println("1 - Registar novo candidato");
                System.out.println("2 - Listar alojamentos disponíveis");
                System.out.println("3 - Verificar estado da candidatura");
                System.out.println("4 - Sair");
                System.out.print("Escolha: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("4")) {
                    out.println("SAIR");
                    break;
                }

                switch (opcao) {
                    case "1":
                        System.out.print("Nome: ");
                        String nome = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Telefone: ");
                        String telefone = scanner.nextLine();
                        System.out.print("Sexo (MASCULINO/FEMININO/OUTRO): ");
                        String sexo = scanner.nextLine();
                        System.out.print("Curso: ");
                        String curso = scanner.nextLine();

                        out.println("REGISTAR_CANDIDATO|" + nome + "|" + email + "|" + telefone + "|" + sexo + "|" + curso);
                        break;

                    case "2":
                        out.println("3"); // comando do servidor para listar alojamentos
                        break;

                    case "3":
                        System.out.print("ID da candidatura: ");
                        String idCand = scanner.nextLine();
                        out.println("2|" + idCand);
                        break;

                    default:
                        System.out.println("Opção inválida!");
                        continue;
                }

                // Lê resposta completa
                System.out.println("\n--- RESPOSTA DO SERVIDOR ---");
                while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                    System.out.println(line);
                    if (line.contains("SAIR")) break;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        } finally {
            System.out.println("Conexão encerrada.");
        }
    }
}
