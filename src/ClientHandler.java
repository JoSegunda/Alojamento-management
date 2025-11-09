import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// Esta classe gerencia uma conexão de um único cliente à base de dados do servidor
public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final int clientPort; // Para identificar o cliente nos logs

    //Construtor
    public ClientHandler(Socket socket){
        this.clientSocket = socket;
        this.clientPort = clientSocket.getPort();
    }
    @Override
    public void run() {
        System.out.println("✅ [HANDLER " + clientPort + "] Thread de processamento iniciada.");
        try {
            // 1. Configurar Streams de Comunicação
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8),true);


            String inputLine;
            //2. Loop de processamento (Lê do cliente até que ele feche a conexão ou envie "sair")
            while ((inputLine = in.readLine()) != null){
                // Print de mensagem recebida
                System.out.println("[CLIENTE " + clientPort + "] Mensagem Recebida: " + inputLine);


                if ("sair".equalsIgnoreCase(inputLine)){
                    out.println("Cliente Desconectou");
                    break;
                }

                //3. Fornecendo resposta
                String response = processClientInput(inputLine);
                out.println("Servidor Responde: "+ response);

                System.out.println("[CLIENTE " + clientPort + "] Mensagem Enviada: Servidor Ecoa: " + response);
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
