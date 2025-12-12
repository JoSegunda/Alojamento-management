package server;

import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import model.Candidato;
import model.Candidatura;
import service.AlojamentoService;
import service.CandidatoService;
import service.CandidaturaService;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
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

    private boolean isAdmin = false;
    private boolean running = true;

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
        System.out.println("[HANDLER] Cliente conectado na porta " + clientPort);

        try {
            // Verificar tipo de cliente
            String firstLine = in.readLine();
            if (firstLine != null && firstLine.trim().equalsIgnoreCase("ADMIN")) {
                isAdmin = true;
                showAdminMenu();
            } else {
                showUserMenu();
            }

            // Processar comandos
            processClientCommands();

        } catch (IOException e) {
            System.err.println("Cliente " + clientPort + " desconectado: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void processClientCommands() throws IOException {
        String inputLine;

        while (running && (inputLine = in.readLine()) != null) {
            inputLine = inputLine.trim();

            if (inputLine.equalsIgnoreCase("SAIR") || inputLine.equalsIgnoreCase("4")) {
                sendMessage("Até logo! 👋");
                break;
            }

            String response = isAdmin ?
                    processAdminCommand(inputLine) :
                    processUserCommand(inputLine);

            sendMessage(response);
        }
    }

    // ========== COMANDOS DE ADMIN ==========
    private String processAdminCommand(String command) {
        String[] parts = command.split("\\|");
        String cmd = parts[0].trim().toUpperCase();

        try {
            return switch (cmd) {
                case "1", "REGISTAR_ALOJAMENTO" -> handleRegistarAlojamento(parts);
                case "2", "ATUALIZAR_ESTADO_ALOJAMENTO" -> handleAtualizarEstadoAlojamento(parts);
                case "3", "ACEITAR_CANDIDATURA" -> handleAceitarCandidatura(parts);
                case "4", "LISTAR_CANDIDATURAS" -> handleListarCandidaturas();
                case "5", "LISTAR_ALOJAMENTOS" -> handleListarTodosAlojamentos();
                default -> "ERRO|Comando não reconhecido";
            };
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleRegistarAlojamento(String[] parts) throws SQLException {
        if (parts.length < 4) {
            return "ERRO|Uso: 1|Nome|Cidade|Capacidade ou REGISTAR_ALOJAMENTO|Nome|Cidade|Capacidade";
        }

        String nome = parts[1].trim();
        String cidade = parts[2].trim();
        int capacidade;

        try {
            capacidade = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) {
            return "ERRO|Capacidade deve ser um número";
        }

        Alojamento novo = new Alojamento(nome, cidade, capacidade);
        Alojamento registado = alojamentoService.registarAlojamento(novo);

        return "SUCESSO|Alojamento ID " + registado.getId() +
                " registado como " + registado.getEstado();
    }

    private String handleAtualizarEstadoAlojamento(String[] parts) throws SQLException {
        if (parts.length < 3) {
            return "ERRO|Uso: 2|ID|ESTADO ou ATUALIZAR_ESTADO_ALOJAMENTO|ID|ESTADO";
        }

        int id;
        EstadoAlojamento novoEstado;

        try {
            id = Integer.parseInt(parts[1].trim());
            novoEstado = EstadoAlojamento.valueOf(parts[2].trim().toUpperCase());
        } catch (NumberFormatException e) {
            return "ERRO|ID deve ser um número";
        } catch (IllegalArgumentException e) {
            return "ERRO|Estado inválido. Use: PENDENTE, EM_OBRAS, ATIVO, SUSPENSO";
        }

        boolean sucesso = alojamentoService.atualizarEstado(id, novoEstado);
        return sucesso ?
                "SUCESSO|Alojamento " + id + " atualizado para " + novoEstado :
                "ERRO|Falha na atualização";
    }

    private String handleAceitarCandidatura(String[] parts) throws SQLException {
        if (parts.length < 2) {
            return "ERRO|Uso: 3|ID ou ACEITAR_CANDIDATURA|ID";
        }

        int candidaturaId;
        try {
            candidaturaId = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return "ERRO|ID deve ser um número";
        }

        boolean sucesso = candidaturaService.aceitarCandidatura(candidaturaId);
        return sucesso ?
                "SUCESSO|Candidatura " + candidaturaId + " aceite" :
                "ERRO|Não foi possível aceitar a candidatura";
    }

    private String handleListarCandidaturas() throws SQLException {
        // Implementação simplificada - listar apenas as pendentes
        return "SUCESSO|Funcionalidade em desenvolvimento. Use o AdminClient para mais opções.";
    }

    private String handleListarTodosAlojamentos() throws SQLException {
        // Este método precisaria ser adicionado ao AlojamentoService
        return "SUCESSO|Funcionalidade em desenvolvimento. Use o AdminClient para mais opções.";
    }

    // ========== COMANDOS DE USUÁRIO ==========
    private String processUserCommand(String command) {
        try {
            int option = Integer.parseInt(command);

            return switch (option) {
                case 1 -> handleNovaCandidatura();
                case 2 -> handleVerificarCandidatura();
                case 3 -> handleListarAlojamentosDisponiveis();
                default -> "ERRO|Opção inválida. Use 1, 2, 3 ou 4 para sair.";
            };
        } catch (NumberFormatException e) {
            return "ERRO|Digite um número (1-4)";
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleNovaCandidatura() {
        try {
            // Coletar dados do candidato
            Candidato candidato = coletarDadosCandidato();

            // Listar alojamentos disponíveis
            String alojamentos = handleListarAlojamentosDisponiveis();
            sendMessage(alojamentos);

            // Selecionar alojamento
            sendMessage("Digite o ID do alojamento desejado:");
            String resposta = in.readLine();

            if (resposta == null) return "ERRO|Conexão perdida";

            int alojamentoId;
            try {
                alojamentoId = Integer.parseInt(resposta.trim());
            } catch (NumberFormatException e) {
                return "ERRO|ID inválido";
            }

            // Registrar candidato e candidatura
            Candidato registado = candidatoService.registarCandidato(candidato, alojamentoId);

            return "SUCESSO|Candidato " + registado.getNome() +
                    " (ID: " + registado.getId() + ") registado com sucesso!";

        } catch (IOException e) {
            return "ERRO|Erro de comunicação: " + e.getMessage();
        } catch (SQLException e) {
            return "ERRO|Erro no banco de dados: " + e.getMessage();
        }
    }

    private Candidato coletarDadosCandidato() throws IOException {
        sendMessage("=== NOVA CANDIDATURA ===");

        String nome = readInput("Nome completo: ");
        String email = readInput("Email: ");
        String telefone = readInput("Telefone: ");

        Candidato.Sexo sexo = null;
        while (sexo == null) {
            String sexoStr = readInput("Sexo (MASCULINO/FEMININO/OUTRO): ").toUpperCase();
            try {
                sexo = Candidato.Sexo.valueOf(sexoStr);
            } catch (IllegalArgumentException e) {
                sendMessage("ERRO|Opção inválida. Tente novamente.");
            }
        }

        String curso = readInput("Curso: ");

        return new Candidato(nome, email, telefone, sexo, curso);
    }

    private String handleVerificarCandidatura() throws IOException {
        sendMessage("Digite o ID da sua candidatura:");
        String resposta = in.readLine();

        if (resposta == null) return "ERRO|Conexão perdida";

        try {
            int candidaturaId = Integer.parseInt(resposta.trim());
            Optional<Candidatura> candidaturas = candidaturaService.findById(candidaturaId);

            if (candidaturas.isEmpty()) {
                return "ERRO|Candidatura não encontrada";
            }

            Candidatura candidatura = candidaturas.get();
            StringBuilder sb = new StringBuilder();
            sb.append("=== ESTADO DA CANDIDATURA ===\n");
            sb.append("ID: ").append(candidatura.getId()).append("\n");
            sb.append("Data: ").append(candidatura.getDataCandidatura()).append("\n");
            sb.append("Estado: ").append(candidatura.getEstado()).append("\n");

            // Informações adicionais
            Optional<Candidato> candidato = candidatoService.findById(candidatura.getCandidatoId());
            candidato.ifPresent(c -> sb.append("Candidato: ").append(c.getNome()).append("\n"));

            Optional<Alojamento> alojamento = alojamentoService.findById(candidatura.getAlojamentoId());
            alojamento.ifPresent(a -> sb.append("Alojamento: ").append(a.getNome()).append("\n"));

            return sb.toString();

        } catch (NumberFormatException e) {
            return "ERRO|ID inválido";
        } catch (SQLException e) {
            return "ERRO|Erro ao consultar candidatura: " + e.getMessage();
        }
    }

    private String handleListarAlojamentosDisponiveis() throws SQLException {
        List<Alojamento> disponiveis = alojamentoService.listarAlojamentosPorEstado(EstadoAlojamento.ATIVO);

        if (disponiveis.isEmpty()) {
            return "Nenhum alojamento disponível no momento.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ALOJAMENTOS DISPONÍVEIS ===\n");
        for (Alojamento a : disponiveis) {
            sb.append(String.format("ID: %d | %s - %s | Capacidade: %d\n",
                    a.getId(), a.getNome(), a.getCidade(), a.getCapacidade()));
        }
        return sb.toString();
    }

    // ========== MÉTODOS AUXILIARES ==========
    private void showAdminMenu() {
        String menu = """
            ╔══════════════════════════════════════════════════╗
            ║             PAINEL ADMINISTRATIVO                ║
            ╚══════════════════════════════════════════════════╝
            COMANDOS:
            1|Nome|Cidade|Capacidade  - Registar alojamento
            2|ID|ESTADO               - Atualizar estado
            3|ID                      - Aceitar candidatura
            4                         - Listar candidaturas
            5                         - Listar alojamentos
            SAIR                      - Encerrar sessão
            ══════════════════════════════════════════════════
            """;
        sendMessage(menu);
    }

    private void showUserMenu() {
        String menu = """
            ╔══════════════════════════════════════════════════╗
            ║       SISTEMA DE ALOJAMENTO ESTUDANTIL           ║
            ╚══════════════════════════════════════════════════╝
            OPÇÕES:
            1 - Candidatar-se a alojamento
            2 - Verificar estado da candidatura
            3 - Listar alojamentos disponíveis
            4 - Sair do sistema
            ══════════════════════════════════════════════════
            Digite o número da opção desejada:
            """;
        sendMessage(menu);
    }

    private String readInput(String prompt) throws IOException {
        sendMessage(prompt);
        String input = in.readLine();
        return input != null ? input.trim() : "";
    }

    private void sendMessage(String message) {
        out.println(message);
        out.println("END"); // Marcador de fim de mensagem
        out.flush();
    }

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
}