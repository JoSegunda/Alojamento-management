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

            // 🔹 Lê o menu inicial
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
            }

            while (true) {
                System.out.print("Você: ");
                userInput = scanner.nextLine();

                out.println(userInput);

                // 🔹 Se for o comando 1 (registo), modo interativo especial
                if ("1".equals(userInput.trim())) {
                    System.out.println("🌀 Modo de registo ativado...");

                    // 🔥 LIMPAR O BUFFER antes de começar o modo interativo
                    clearInputStream(in);

                    // Processar fluxo interativo
                    while ((line = in.readLine()) != null) {
                        // Se for linha vazia, termina o fluxo
                        if (line.trim().isEmpty()) {
                            break;
                        }
                        // Se for um ERRO, apenas mostrar
                        if (line.startsWith("ERRO|")) {
                            System.out.println("❌ " + line.substring(5));
                            continue;
                        }

                        // Se for SUCESSO, mostrar e terminar
                        if (line.startsWith("SUCESSO|")) {
                            System.out.println("✅ " + line.substring(8));
                            break;
                        }

                        // Se for um prompt (termina com ":"), responder
                        if (line.endsWith(":") || line.contains(":")) {
                            System.out.println("Servidor: " + line);
                            System.out.print("Sua resposta: ");
                            String resposta = scanner.nextLine();
                            out.println(resposta);
                        } else {
                            // Se não for prompt, apenas mostrar a mensagem
                            System.out.println("Servidor: " + line);
                        }
                    }
                } else {
                    //Comportamento normal para outros comandos
                    while ((line = in.readLine()) != null && !line.trim().isEmpty()) {
                        System.out.println("Servidor: " + line);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        } finally {
            System.out.println("Conexão encerrada.");
        }
    }

    //  LIMPAR O BUFFER DE ENTRADA
    private static void clearInputStream(BufferedReader in) {
        try {
            // Lê todos os dados disponíveis sem bloquear
            while (in.ready()) {
                in.readLine();
            }
        } catch (IOException e) {
            // Ignora erros de limpeza
        }
    }
}