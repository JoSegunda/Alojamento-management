import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import model.Candidato;
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
            out.print(MENU);
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
            // 1. Tentar comandos numéricos (Utilizador Final)
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
                // 2. Se a primeira parte não for um número, tenta comandos administrativos
                switch (cmd) {
                    case "REGISTAR_ALOJAMENTO":
                        // Ex: REGISTAR_ALOJAMENTO|Nome X|Lisboa|10
                        return handleRegistarAlojamento(args);

                    case "ATUALIZAR_ESTADO_ALOJAMENTO":
                        // NOVO COMANDO ADMINISTRATIVO
                        // Ex: ATUALIZAR_ESTADO_ALOJAMENTO|5|APROVADO
                        return handleAtualizarEstadoAlojamento(args);

                    case "ACEITAR_CANDIDATURA":
                        // NOVO COMANDO ADMINISTRATIVO (Ação principal do sistema)
                        // Ex: ACEITAR_CANDIDATURA|12
                        return handleAceitarCandidatura(Integer.parseInt(args[0]));

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
        // Assume que existe um méthod findById ou verificarEstado no serviço
        List<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);

        if (candidaturaOpt.isEmpty()) {
            return "ERRO|Candidatura ID " + candidaturaId + " não encontrada.";
        }

        Candidatura candidatura = candidaturaOpt.getFirst();
        return "SUCESSO|ESTADO_CANDIDATURA|" + candidatura.getId() + "|" + candidatura.getEstado().name();
    }
    // COMANDO 3: Verificar alojamento disponível + capacidade
    private String handleListarAlojamentosDisponiveis() throws IllegalArgumentException, SQLException {
        // Assume que este  busca alojamentos e ATIVOS.
        List<Alojamento> disponiveis = alojamentoService.listarAlojamentosPorEstado(EstadoAlojamento.ATIVO);

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

    private String handleRegistarAlojamento(String[] args) throws IllegalArgumentException, SQLException {
        // Assume a ordem: Nome, Cidade, Capacidade
        Alojamento novoAlojamento = new Alojamento(
                args[0],
                args[1],
                Integer.parseInt(args[2])
        );

        Alojamento registado = alojamentoService.registarAlojamento(novoAlojamento);
        return "SUCESSO|Alojamento ID " + registado.getId() + " registado como " + registado.getEstado();
    }

    private String handleAtualizarEstadoAlojamento(String[] args) throws IllegalArgumentException, SQLException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Argumentos insuficientes. Use: ID|ESTADO_NOVO.");
        }

        int alojamentoId = Integer.parseInt(args[0]);
        EstadoAlojamento novoEstado;

        // 1. Converte a string do estado para o ENUM
        try {
            novoEstado = EstadoAlojamento.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de Alojamento inválido: " + args[1] + ". Use um dos seguintes: " + Arrays.toString(EstadoAlojamento.values()));
        }

        // 2. Chama o serviço
        boolean sucesso = alojamentoService.atualizarEstado(alojamentoId, novoEstado);

        if (sucesso) {
            return "SUCESSO|Alojamento " + alojamentoId + " atualizado para " + novoEstado.name() + ".";
        }
        return "ERRO|Falha na atualização do estado do alojamento.";
    }

    private String handleAceitarCandidatura(int candidaturaId) throws IllegalArgumentException, SQLException {
        // Chama o méthod no serviço que contém a lógica de aceitação
        boolean sucesso = candidaturaService.aceitarCandidatura(candidaturaId);

        if (sucesso) {
            // Se aceita, o serviço deve garantir que outras candidaturas no mesmo alojamento sejam recusadas/finalizadas.
            return "SUCESSO|Candidatura ID " + candidaturaId + " aceite com sucesso. Outras candidaturas podem ter sido finalizadas.";
        }
        return "ERRO|Falha ao aceitar a candidatura. Verifique o estado atual e se o alojamento está ativo.";
    }

    private String handleRegistarCandidato(String[] args) throws IllegalArgumentException, SQLException {
        // Assume a ordem: Nome, Email, Telefone, Sexo, Curso
        // Converte a string de Sexo para o ENUM
        Sexo sexo = Sexo.valueOf(args[3].toUpperCase());

        Candidato novoCandidato = new model.Candidato(
                args[0],
                args[1],
                args[2],
                sexo,
                args[4]
        );
        model.Candidato registado = candidatoService.registarCandidato(novoCandidato);
        return "SUCESSO|Candidato ID " + registado.getId() + " registado.";
    }

    private void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("[HANDLER " + clientPort + "] Conexão com o cliente encerrada.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar recursos: " + e.getMessage());
        }
    }

    // Define o menu como uma constante para clareza
    private static final String MENU =
            "--------------------------------------------------\n" +
                    "| Bem-vindo ao Sistema de Gestão de Alojamentos! |\n" +
                    "--------------------------------------------------\n" +
                    "Comandos disponíveis (Use: OPÇÃO|ARG1|ARG2...):\n" +
                    "--------------------------------------------------\n" +
                    "USUÁRIO:\n" +
                    "1|ID_ALOJAMENTO|ID_CANDIDATO -> Candidatar a alojamento\n" +
                    "2|ID_CANDIDATURA             -> Verificar estado da candidatura\n" +
                    "3                            -> Verificar alojamentos disponíveis + capacidade\n" +
                    "SAIR                         -> Encerrar a conexão\n" +
                    "--------------------------------------------------\n" +
                    "ADMINISTRATIVO (String|ARG1...):\n" +
                    "REGISTAR_ALOJAMENTO|Nome|Cidade|Capacidade\n" +
                    "ATUALIZAR_ESTADO_ALOJAMENTO|ID|ESTADO_NOVO (Ex: APROVADO)\n" +
                    "ACEITAR_CANDIDATURA|ID_CANDIDATURA\n" +
                    "REGISTAR_CANDIDATO|Nome|Email|Telefone|SEXO|Curso\n" +
                    "--------------------------------------------------\n";
}