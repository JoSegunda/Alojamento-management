import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import model.Candidato.Sexo;
import model.Candidatura;
import service.AlojamentoService;
import service.CandidatoService;
import service.CandidaturaService;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final int clientPort;

    // Serviços
    private final AlojamentoService alojamentoService;
    private final CandidatoService candidatoService;
    private final CandidaturaService candidaturaService;

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
        System.out.println("Handler iniciado...");

        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[CLIENTE " + clientPort + "] Comando Recebido: " + inputLine);

                // Processa o pedido e envia a resposta
                String response = processCommand(inputLine);
                out.println(response);
                System.out.println("[CLIENTE " + clientPort + "] Resposta Enviada: " + response.split("\\|")[0]);

                if ("SAIR".equalsIgnoreCase(inputLine)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Conexão encerrada pelo cliente.");
        } finally {
            closeResources();
        }
    }


    // Lógica de funcionamento
    private String processCommand(String command) {
        String[] parts = command.split("\\|");
        String cmd = parts[0].toUpperCase();

        // Os argumentos (ARG1, ARG2, ...) estão a partir do índice 1
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        try {
            // 1. Tentar comandos numéricos
            try {
                int cmdNum = Integer.parseInt(cmd);
                switch (cmdNum) {
                    case 1:
                        // Ex: 1|10|5 (alojamentoId, candidatoId)
                        return handleSubmeterCandidatura(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    case 2:
                        // Ex: 2|5 (candidaturaId)
                        return handleVerificarEstadoCandidatura(Integer.parseInt(args[0]));
                    case 3:
                        // Ex: 3
                        return handleListarAlojamentosDisponiveis();
                    default:
                        return "ERRO|Opção de menu não reconhecida: " + cmdNum;
                }
            } catch (NumberFormatException e) {
                // Se a primeira parte não for um número, tenta comandos administrativos
                switch (cmd) {
                    case "REGISTAR_ALOJAMENTO":
                        // ... (restante dos comandos administrativos existentes) ...
                    case "APROVAR_ALOJAMENTO":
                        // Ex: APROVAR_ALOJAMENTO|5
                        return handleAtualizarEstadoAlojamento(Integer.parseInt(args[0]), EstadoAlojamento.APROVADO);
                    case "REGISTAR_CANDIDATO":
                        // Ex: REGISTAR_CANDIDATO|Joana|joana@mail.pt|912345678|FEMININO|Eng
                        return handleRegistarCandidato(args);
                    case "SAIR":
                        return "SUCESSO|Desconectando...";
                    default:
                        return "ERRO|Comando não reconhecido: " + cmd;
                }
            }

        } catch (IllegalArgumentException e) {
            return "ERRO_VALIDACAO|" + e.getMessage();
        } catch (SQLException e) {
            System.err.println("Erro de BD: " + e.getMessage());
            return "ERRO_BD|Falha na operação de base de dados.";
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            return "ERRO_SISTEMA|Ocorreu um erro interno.";
        }
    }

    // --- MÉTODOS HANDLERS PARA COMANDOS NUMÉRICOS ---

    // COMANDO 1: Candidatar a alojamento (Submeter Candidatura)
    private String handleSubmeterCandidatura(int alojamentoId, int candidatoId) throws IllegalArgumentException, SQLException {
        model.Candidatura candidatura = candidaturaService.submeterCandidatura(alojamentoId, candidatoId);
        return "SUCESSO|Candidatura ID " + candidatura.getId() + " submetida com sucesso. Estado: " + candidatura.getEstado();
    }

    // COMANDO 2: Verificar estado da candidatura
    private String handleVerificarEstadoCandidatura(int candidaturaId) throws IllegalArgumentException, SQLException {
        // Assume que existe um método findById ou verificarEstado no serviço
        Optional<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);

        if (candidaturaOpt.isEmpty()) {
            return "ERRO|Candidatura ID " + candidaturaId + " não encontrada.";
        }

        Candidatura candidatura = candidaturaOpt.get();
        return "SUCESSO|ESTADO_CANDIDATURA|" + candidatura.getId() + "|" + candidatura.getEstado().name();
    }

    // COMANDO 3: Verificar alojamento disponíveis + capacidade
    private String handleListarAlojamentosDisponiveis() throws IllegalArgumentException, SQLException {
        // Assume que este método busca alojamentos APROVADOS e ATIVOS.
        List<Alojamento> disponiveis = alojamentoService.listarAlojamentosDisponiveis();

        if (disponiveis.isEmpty()) {
            return "SUCESSO|Nenhum alojamento disponível no momento.";
        }

        StringBuilder sb = new StringBuilder("SUCESSO|LISTA_ALOJAMENTOS|");
        sb.append(disponiveis.size()).append(" encontrados.\n");

        for (Alojamento a : disponiveis) {
            sb.append(a.getId())
                    .append(" - ").append(a.getNome())
                    .append(" em ").append(a.getCidade())
                    .append(" | Capacidade: ").append(a.getCapacidade()).append("\n");
        }

        return sb.toString();
    }

    // --- (Restante dos métodos handlers, como handleRegistarAlojamento, ficam inalterados) ---

    private void closeResources() {
        // ... (método closeResources inalterado) ...
    }
}