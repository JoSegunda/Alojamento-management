import java.io.*;
import java.net.Socket;
// Esta classe gerencia uma conexão de um único cliente à base de dados do servidor
public class ClientHandler implements Runnable{
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    //Construtor
    public ClientHandler(Socket socket){
        this.clientSocket = socket;
    }
    @Override
    public void run() {

        try {
            // 1. Configurar Streams de Comunicação
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            //2. Loop de processamento (Lê do cliente até que ele feche a conexão ou envie "sair")
            while ((inputLine = in.readLine()) != null){
                System.out.println("Recebido do cliente: "+clientSocket.getPort()+": "+inputLine);

                if ("sair".equalsIgnoreCase(inputLine)){
                    out.println("Cliente Desconectou");
                    break;
                }

                //3. Fornecendo resposta
                String response = processClientInput(inputLine);
                out.println("Servidor Responde: "+ response);
            }
        } catch (Exception e) {
            System.err.println("Erro de E/S ou conexão fechada: " + e.getMessage());
        }finally {
            // Fechar recursos
            closeResources();
        }

    }
    private String processClientInput(String input){
        // Lógica de negócios do servidor aqui

        return input.toUpperCase();

    }

    private void closeResources(){
        try {
            if (out !=null) out.close();
            if (in !=null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()){
                clientSocket.close();
                System.out.println("Conexão com o cliente " + clientSocket.getPort() + " fechada.");
            }

        } catch (IOException e) {
            System.err.println("Erro ao fechar recursos: " + e.getMessage());
        }
    }
}
