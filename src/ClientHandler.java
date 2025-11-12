import service.AlojamentoService;
import service.CandidatoService;
import service.CandidaturaService;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

// Esta classe gerencia uma conexão de um único cliente à base de dados do servidor
public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final int clientPort; // Para identificar o cliente nos logs

    // Serviços
    private final AlojamentoService alojamentoService;
    private final CandidatoService candidatoService;
    private final CandidaturaService candidaturaService;

    //Construtor
    public ClientHandler(Socket socket, AlojamentoService as, CandidatoService cs, CandidaturaService cads) throws IOException {
        this.clientSocket = socket;
        this.clientPort = socket.getPort();
        this.alojamentoService = as;
        this.candidatoService = cs;
        this.candidaturaService = cads;

        // Inicialização dos Streams de I/O
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        System.out.println("Gerente a iniciar...");

        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[CLIENTE - " + clientPort + "] Comando Recebido: " + inputLine);

                // Processa a requisição e envia a resposta
                String response = processCommand(inputLine);
                out.println(response);
                System.out.println("[CLIENTE " + clientPort + "] Resposta Enviada: " + response.split("\\|")[0]);

                if ("SAIR".equalsIgnoreCase(inputLine)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[HANDLER " + clientPort + "] Conexão encerrada pelo cliente.");
        } finally {
            closeResources();
        }

    }
    private String processCommand(String command) {
        String[] parts = command.split("\\|");
        String cmd = parts[0].toUpperCase();

        // Os argumentos (ARG1, ARG2, ...) estão a partir do índice 1
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        try {
            switch (cmd) {
                case "REGISTAR_ALOJAMENTO":
                    // Ex: REGISTAR_ALOJAMENTO|Nome X|Lisboa|10
                    return handleRegistarAlojamento(args);
                case "APROVAR_ALOJAMENTO":
                    // Ex: APROVAR_ALOJAMENTO|5
                    return handleAtualizarEstadoAlojamento(Integer.parseInt(args[0]), EstadoAlojamento.APROVADO);
                case "REGISTAR_CANDIDATO":
                    // Ex: REGISTAR_CANDIDATO|Joana|joana@mail.pt|912345678|FEMININO|Eng
                    return handleRegistarCandidato(args);
                case "SUBMETER_CANDIDATURA":
                    // Ex: SUBMETER_CANDIDATURA|10|5
                    return handleSubmeterCandidatura(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                case "SAIR":
                    return "SUCESSO|Desconectando...";
                default:
                    return "ERRO|Comando não reconhecido: " + cmd;
            }
        } catch (IllegalArgumentException e) {
            // Erros de validação (Serviço) ou parsing (números)
            return "ERRO_VALIDACAO|" + e.getMessage();
        } catch (SQLException e) {
            // Erros da base de dados (Repositório)
            System.err.println("Erro de BD: " + e.getMessage());
            return "ERRO_BD|Falha na operação de base de dados.";
        } catch (Exception e) {
            // Outros erros inesperados
            System.err.println("Erro inesperado: " + e.getMessage());
            return "ERRO_SISTEMA|Ocorreu um erro interno.";
        }
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
