package server;

import repository.*;
import service.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private static final int THREAD_POOL_SIZE = 10;
    private static boolean running = true;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   SERVIDOR DE ALOJAMENTO ESTUDANTIL - ONLINE    ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        System.out.println("🔧 Inicializando serviços...");

        try {
            // Inicializar repositórios
            AlojamentoRepository alojamentoRepo = new AlojamentoRepository();
            CandidatoRepository candidatoRepo = new CandidatoRepository();
            CandidaturaRepository candidaturaRepo = new CandidaturaRepository();

            // Inicializar serviços
            AlojamentoService alojamentoService = new AlojamentoService(alojamentoRepo);
            CandidaturaService candidaturaService = new CandidaturaService(
                    candidaturaRepo, alojamentoRepo, candidatoRepo
            );
            CandidatoService candidatoService = new CandidatoService(candidatoRepo, candidaturaService);

            System.out.println("✅ Serviços inicializados com sucesso!");
            System.out.println("🌐 Aguardando conexões na porta " + PORT + "...");

            ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("\n🔗 Nova conexão: " +
                            clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                    ClientHandler handler = new ClientHandler(
                            clientSocket, alojamentoService, candidatoService, candidaturaService
                    );
                    threadPool.execute(handler);
                }
            }

            threadPool.shutdown();
        } catch (Exception e) {
            System.err.println("❌ Erro fatal no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("🛑 Servidor encerrado.");
        }
    }

    public static void stopServer() {
        running = false;
    }
}