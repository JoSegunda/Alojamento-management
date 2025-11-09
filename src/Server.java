import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
public class Server {
    public static void main(String[] args) {
        // Instancia o server socket na porta 12345

        try(ServerSocket server = new ServerSocket(12345)) {
            System.out.println("Servidor ouvindo na porta 12345");
            while(true){
                // Accept() bloqueia a execução até que o servidor receba um pedido de conexão

                Socket client = server.accept();
                new Thread(new ClientHandler(client)).start();
                System.out.println("Cliente conectado: "+client.getInetAddress().getHostAddress());

                ObjectOutputStream saida = new ObjectOutputStream(client.getOutputStream());
                saida.flush();
                LocalDate myObj = LocalDate.now();
                saida.writeObject("hello");

                saida.close();
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
