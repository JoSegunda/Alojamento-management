import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
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
            throw new RuntimeException(e);
        }
    }
}
