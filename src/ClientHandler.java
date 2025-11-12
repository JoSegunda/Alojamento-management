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

    public ClientHandler(Socket socket,
                         AlojamentoService alojamentoService,
                         CandidatoService candidatoService,
                         CandidaturaService candidaturaService) throws IOException {

        this.clientSocket = socket;
        this.clientPort = socket.getPort();
        this.alojamentoService = alojamentoService;
        this.candidatoService = candidatoService;
        this.candidaturaService = candidaturaService;

        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        System.out.println("[HANDLER] Conexão iniciada com cliente na porta " + clientPort);

        try {
            // Mostra o menu imediatamente
            out.println(MENU);
            out.flush();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                inputLine = inputLine.trim();
                if (inputLine.isEmpty()) continue;

                System.out.println("[CLIENTE " + clientPort + "] Comando recebido: " + inputLine);

                String response = processCommand(inputLine);

                // Envia a resposta e o menu novamente
                out.println(response);
                out.println(); // linha em branco
                out.println(MENU);

                System.out.println("[CLIENTE " + clientPort + "] Resposta enviada.");
                if (inputLine.equalsIgnoreCase("SAIR")) break;
            }

        } catch (IOException e) {
            System.err.println("Conexão encerrada com o cliente " + clientPort);
        } finally {
            closeResources();
        }
    }

    // -------------------------------------------------------
    // PROCESSAMENTO DE COMANDOS
    // -------------------------------------------------------
    private String processCommand(String command) {
        command = command.trim();
        if (command.isEmpty()) return "ERRO|Comando vazio";

        String[] parts = command.split("\\|", -1);
        String cmd = parts[0].trim().toUpperCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        try {
            // --- COMANDOS NUMÉRICOS (usuário) ---
            try {
                int cmdNum = Integer.parseInt(cmd);
                switch (cmdNum) {
                    case 1: // Submeter candidatura
                        if (args.length < 2)
                            return "ERRO|Uso: 1|<alojamentoId>|<candidatoId>";
                        return handleSubmeterCandidatura(
                                Integer.parseInt(args[0].trim()),
                                Integer.parseInt(args[1].trim())
                        );
                    case 2: // Verificar estado candidatura
                        if (args.length < 1)
                            return "ERRO|Uso: 2|<candidaturaId>";
                        return handleVerificarEstadoCandidatura(Integer.parseInt(args[0].trim()));
                    case 3: // Listar alojamentos disponíveis
                        return handleListarAlojamentosDisponiveis();
                    default:
                        return "ERRO|Opção não reconhecida: " + cmdNum;
                }
            } catch (NumberFormatException e) {
                // --- COMANDOS ADMINISTRATIVOS (texto) ---
                switch (cmd) {
                    case "REGISTAR_ALOJAMENTO":
                        return handleRegistarAlojamento(args);
                    case "ATUALIZAR_ESTADO_ALOJAMENTO":
                        return handleAtualizarEstadoAlojamento(args);
                    case "ACEITAR_CANDIDATURA":
                        if (args.length < 1) return "ERRO|Uso: ACEITAR_CANDIDATURA|<id>";
                        return handleAceitarCandidatura(Integer.parseInt(args[0].trim()));
                    case "REGISTAR_CANDIDATO":
                        return handleRegistarCandidato(args);
                    case "SAIR":
                        return "SUCESSO|Sessão encerrada.";
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

    // -------------------------------------------------------
    // HANDLERS DE COMANDOS
    // -------------------------------------------------------

    private String handleSubmeterCandidatura(int alojamentoId, int candidatoId)
            throws SQLException {
        Candidatura candidatura = candidaturaService.submeterCandidatura(alojamentoId, candidatoId);
        return "SUCESSO|Candidatura ID " + candidatura.getId()
                + " submetida. Estado: " + candidatura.getEstado();
    }

    private String handleVerificarEstadoCandidatura(int candidaturaId) throws SQLException {
        Optional<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);
        if (candidaturaOpt.isEmpty())
            return "ERRO|Candidatura ID " + candidaturaId + " não encontrada.";
        Candidatura candidatura = candidaturaOpt.get();
        return "SUCESSO|Candidatura ID " + candidatura.getId()
                + " está com estado: " + candidatura.getEstado();
    }

    private String handleListarAlojamentosDisponiveis() throws SQLException {
        List<Alojamento> disponiveis = alojamentoService.listarAlojamentosDisponiveis();

        if (disponiveis.isEmpty())
            return "SUCESSO|Nenhum alojamento disponível no momento.";

        StringBuilder sb = new StringBuilder("SUCESSO|LISTA_ALOJAMENTOS|\n");
        for (Alojamento a : disponiveis) {
            sb.append("ID: ").append(a.getId())
                    .append(" | ").append(a.getNome())
                    .append(" - ").append(a.getCidade())
                    .append(" | Capacidade: ").append(a.getCapacidade())
                    .append("\n");
        }
        return sb.toString();
    }

    private String handleRegistarAlojamento(String[] args) throws SQLException {
        if (args.length < 3)
            return "ERRO|Uso: REGISTAR_ALOJAMENTO|<Nome>|<Cidade>|<Capacidade>";

        String nome = args[0].trim();
        String cidade = args[1].trim();
        int capacidade;
        try {
            capacidade = Integer.parseInt(args[2].trim());
        } catch (NumberFormatException e) {
            return "ERRO|Capacidade inválida: " + args[2];
        }

        Alojamento novo = new Alojamento(nome, cidade, capacidade);
        Alojamento registado = alojamentoService.registarAlojamento(novo);
        return "SUCESSO|Alojamento ID " + registado.getId()
                + " registado como " + registado.getEstado();
    }

    private String handleAtualizarEstadoAlojamento(String[] args) throws SQLException {
        if (args.length < 2)
            return "ERRO|Uso: ATUALIZAR_ESTADO_ALOJAMENTO|<id>|<estado>";

        int id = Integer.parseInt(args[0].trim());
        EstadoAlojamento novoEstado;
        try {
            novoEstado = EstadoAlojamento.valueOf(args[1].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "ERRO|Estado inválido. Use um de: " + Arrays.toString(EstadoAlojamento.values());
        }

        boolean sucesso = alojamentoService.atualizarEstado(id, novoEstado);
        if (sucesso)
            return "SUCESSO|Alojamento " + id + " atualizado para " + novoEstado.name();
        else
            return "ERRO|Falha na atualização do alojamento.";
    }

    private String handleAceitarCandidatura(int candidaturaId) throws SQLException {
        boolean sucesso = candidaturaService.aceitarCandidatura(candidaturaId);
        if (sucesso)
            return "SUCESSO|Candidatura " + candidaturaId + " aceite com sucesso.";
        else
            return "ERRO|Não foi possível aceitar a candidatura (verifique estado/capacidade).";
    }

    private String handleRegistarCandidato(String[] args) throws SQLException {
        if (args.length < 5)
            return "ERRO|Uso: REGISTAR_CANDIDATO|<Nome>|<Email>|<Telefone>|<Sexo>|<Curso>";

        String nome = args[0].trim();
        String email = args[1].trim();
        String telefone = args[2].trim();
        Sexo sexo;
        try {
            sexo = Sexo.valueOf(args[3].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "ERRO|Sexo inválido. Use: MASCULINO, FEMININO, OUTRO.";
        }
        String curso = args[4].trim();

        Candidato novo = new Candidato(nome, email, telefone, sexo, curso);
        Candidato registado = candidatoService.registarCandidato(novo);
        return "SUCESSO|Candidato ID " + registado.getId() + " registado.";
    }

    // -------------------------------------------------------
    // FECHAR CONEXÃO
    // -------------------------------------------------------
    private void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("[HANDLER " + clientPort + "] Conexão encerrada.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar recursos: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // MENU
    // -------------------------------------------------------
    private static final String MENU =
            "--------------------------------------------------\n" +
                    "| Bem-vindo ao Sistema de Gestão de Alojamentos! |\n" +
                    "--------------------------------------------------\n" +
                    "Comandos disponíveis (Use: OPÇÃO|ARG1|ARG2...):\n" +
                    "--------------------------------------------------\n" +
                    "USUÁRIO:\n" +
                    "1 -> Candidatar a alojamento (1|alojId|candId)\n" +
                    "2 -> Verificar estado da candidatura (2|candId)\n" +
                    "3 -> Listar alojamentos disponíveis\n" +
                    "--------------------------------------------------\n" +
                    "ADMINISTRADOR:\n" +
                    "REGISTAR_ALOJAMENTO|Nome|Cidade|Capacidade\n" +
                    "ATUALIZAR_ESTADO_ALOJAMENTO|id|ESTADO\n" +
                    "ACEITAR_CANDIDATURA|id\n" +
                    "REGISTAR_CANDIDATO|Nome|Email|Telefone|Sexo|Curso\n" +
                    "--------------------------------------------------\n" +
                    "SAIR -> Encerra a sessão\n" +
                    "--------------------------------------------------";
}
