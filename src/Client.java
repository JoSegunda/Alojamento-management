import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String ServerIP = "127.0.0.1";
    private static  final int ServerPort = 12345;

    public static void main(String[] args){
        System.out.println("Cliente Conectando...");

        try(


            // Conectar ao servidor
            Socket socket = new Socket(ServerIP, ServerPort);

            //Ler do terminal para configurar usuário digitar a mensagem
            Scanner scanner = new Scanner(System.in);

            //Configurar a entrada e saída para enviar e receber mensagens do servidor
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8) , true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        ){
            System.out.println("Conectado ao servidor em " + ServerIP + ":" + ServerPort);
            System.out.println("Digite 'sair' para desconectar.\n");

            String userInput;
            String serverResponse;

            // ‘Loop’ de comunicação
            while (true) {
                //  1. lê a entrada do usuário
                System.out.print("Você: ");
                userInput = scanner.nextLine();

                // Verifica a condição de saída
                if (userInput.equalsIgnoreCase("sair")) {
                    out.println(userInput); // Envia "sair" para o servidor
                    break;
                }

                // Enviar mensagem ao servidor
                out.println(userInput);

                // aguarda e lê a resposta do servidor
                if ((serverResponse = in.readLine()) != null) {
                    System.out.println("Servidor: " + serverResponse);
                }
            }

        }catch (Exception e) {
            System.err.println("Erro de conexão ou comunicação com o servidor: " + e.getMessage());
        } finally {
            System.out.println("\nConexão do cliente encerrada.");
        }
    }
}
