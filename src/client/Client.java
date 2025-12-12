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
    private static boolean connected = false;

    public static void main(String[] args) {
        System.out.println("🎓 CLIENTE DE ALOJAMENTO ESTUDANTIL");
        System.out.println("=====================================");

        if (!connectToServer()) {
            return;
        }

        try {
            // Identificar como usuário normal
            out.println("USER");
            out.flush();

            // Receber menu inicial
            System.out.println("\n📋 MENU PRINCIPAL:");
            readServerResponse();

            // Loop principal de interação
            boolean exit = false;
            while (!exit && connected) {
                System.out.print("\n📝 Escolha uma opção (1-4): ");
                String option = scanner.nextLine().trim();

                switch (option) {
                    case "1":
                        handleCandidatura();
                        break;
                    case "2":
                        handleVerificarCandidatura();
                        break;
                    case "3":
                        listarAlojamentos();
                        break;
                    case "4":
                        out.println("SAIR");
                        out.flush();
                        exit = true;
                        System.out.println("👋 A sair do sistema...");
                        break;
                    default:
                        System.out.println("⚠️ Opção inválida! Tente novamente.");
                }

                if (!exit) {
                    readServerResponse();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro durante a sessão: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private static boolean connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            connected = true;
            System.out.println("✅ Conectado ao servidor!");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Não foi possível conectar ao servidor: " + e.getMessage());
            return false;
        }
    }

    private static void readServerResponse() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) break;

            if (line.startsWith("ERRO")) {
                System.err.println("❌ " + line.replaceFirst("ERRO[|_]*", ""));
            } else if (line.startsWith("SUCESSO")) {
                System.out.println("✅ " + line.replaceFirst("SUCESSO[|_]*", ""));
            } else {
                System.out.println(line);
            }
        }
    }

    private static void handleCandidatura() {
        System.out.println("\n📄 NOVA CANDIDATURA");
        System.out.println("-------------------");

        try {
            // O servidor irá guiar passo a passo
            out.println("1");
            out.flush();

            // Aguardar instruções do servidor
            readServerResponse();

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar candidatura: " + e.getMessage());
        }
    }

    private static void handleVerificarCandidatura() {
        System.out.println("\n🔍 VERIFICAR CANDIDATURA");
        System.out.println("-------------------------");

        try {
            System.out.print("Digite o ID da candidatura: ");
            String id = scanner.nextLine().trim();

            out.println("2|" + id);
            out.flush();

        } catch (Exception e) {
            System.err.println("❌ Erro ao verificar candidatura: " + e.getMessage());
        }
    }

    private static void listarAlojamentos() {
        System.out.println("\n🏠 ALOJAMENTOS DISPONÍVEIS");
        System.out.println("-------------------------");

        try {
            out.println("3");
            out.flush();
        } catch (Exception e) {
            System.err.println("❌ Erro ao listar alojamentos: " + e.getMessage());
        }
    }

    private static void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            scanner.close();
            System.out.println("🔒 Conexão encerrada.");
        } catch (IOException e) {
            System.err.println("⚠️ Erro ao fechar conexão: " + e.getMessage());
        }
    }
}