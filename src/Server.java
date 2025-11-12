import repository.AlojamentoRepository;
import repository.CandidatoRepository;
import repository.CandidaturaRepository;
import repository.DatabaseConnection;
import service.AlojamentoService;
import service.CandidatoService;
import service.CandidaturaService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor...");

        // Inicialização de dos repositórios
        AlojamentoRepository alojamentoRepository = new AlojamentoRepository();
        CandidatoRepository candidatoRepository = new CandidatoRepository();
        CandidaturaRepository candidaturaRepository = new CandidaturaRepository();

        AlojamentoService alojamentoService = new AlojamentoService(alojamentoRepository);
        CandidatoService candidatoService = new CandidatoService(candidatoRepository);
        CandidaturaService candidaturaService = new CandidaturaService(
                candidaturaRepository, alojamentoRepository, candidatoRepository
        );

        // Testar conexão a base de dados
        try {
            DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("ERRO FATAL: Falha na conexão com a base de dados. Encerrando o servidor.");
            return; // Encerra o programa
        }

        // Instancia o server socket na porta 12345
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor Iniciado na porta" + PORT + ". Aguardando conexões...");

            while(true){
                // Accept() bloqueia a execução até que o servidor receba um pedido de conexão
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress() + " na porta " + clientSocket.getPort());

                // Criar um clientHandler para processar o cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                //Inicia o handler numa nova Thread
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
