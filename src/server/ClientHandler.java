package server;

import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import model.Candidato;
import model.Candidatura;
import model.Candidatura.EstadoCandidatura;
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
            // Identificar tipo de cliente
            String firstLine = in.readLine();
            if (firstLine != null && firstLine.trim().equalsIgnoreCase("ADMIN")) {
                isAdmin = true;
                sendMenu();
            } else {
                sendMenu();
            }

            // Processar comandos
            String input;
            while (running && (input = in.readLine()) != null) {
                input = input.trim();

                System.out.println("[CLIENTE " + clientPort + "] Comando: " + input);

                if (input.equalsIgnoreCase("SAIR")) {
                    sendMessage("Até logo!");
                    break;
                }

                String response;
                if (isAdmin) {
                    response = processAdminCommand(input);
                } else {
                    try {
                        int option = Integer.parseInt(input);
                        response = processUserCommand(option);
                    } catch (NumberFormatException e) {
                        response = "ERRO|Comando inválido. Use números de 1 a 4.";
                    }
                }

                sendMessage(response);

                // Após processar o comando, enviar o menu novamente
                if (!input.equalsIgnoreCase("SAIR")) {
                    sendMenu();
                }
            }
        } catch (IOException e) {
            System.out.println("[HANDLER] Cliente " + clientPort + " desconectado: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    // ========== COMANDOS ADMIN ==========
    private String processAdminCommand(String command) {
        command = command.trim();
        System.out.println("[ADMIN " + clientPort + "] Comando: " + command);

        // Verificar se é um número simples (para comando 4 ou 5)
        if (command.equals("4")) {
            return handleListarCandidaturasPendentes();
        } else if (command.equals("5")) {
            return handleListarTodosAlojamentos();
        }

        // Caso contrário, processar comandos com parâmetros
        String[] parts = command.split("\\|");
        String cmd = parts[0].trim();

        try {
            switch (cmd) {
                case "1":
                    if (parts.length < 4) {
                        return "ERRO|Uso: 1|Nome|Cidade|Capacidade\nExemplo: 1|Residência A|Lisboa|50";
                    }
                    return handleRegistarAlojamento(parts[1], parts[2], parts[3]);

                case "2":
                    if (parts.length < 3) {
                        return "ERRO|Uso: 2|ID|ESTADO\nExemplo: 2|1|ATIVO\nEstados: PENDENTE, EM_OBRAS, ATIVO, SUSPENSO";
                    }
                    return handleAtualizarEstadoAlojamento(parts[1], parts[2]);

                case "3":
                    if (parts.length < 2) {
                        return "ERRO|Uso: 3|ID_CANDIDATURA\nExemplo: 3|5";
                    }
                    return handleAtualizarEstadoCandidatura(parts[1]);

                default:
                    return "ERRO|Comando não reconhecido. Use 1-5 ou SAIR.";
            }
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleRegistarAlojamento(String nome, String cidade, String capacidadeStr) {
        try {
            int capacidade = Integer.parseInt(capacidadeStr);
            Alojamento alojamento = new Alojamento(nome, cidade, capacidade);
            Alojamento registado = alojamentoService.registarAlojamento(alojamento);
            return "SUCESSO|Alojamento criado com sucesso!\nID: " + registado.getId() +
                    " | Nome: " + registado.getNome() +
                    " | Estado: " + registado.getEstado();
        } catch (NumberFormatException e) {
            return "ERRO|Capacidade deve ser um número inteiro.";
        } catch (SQLException e) {
            return "ERRO|Erro na base de dados: " + e.getMessage();
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleAtualizarEstadoAlojamento(String idStr, String estadoStr) {
        try {
            int id = Integer.parseInt(idStr);
            EstadoAlojamento estado = EstadoAlojamento.valueOf(estadoStr.toUpperCase());

            boolean sucesso = alojamentoService.atualizarEstado(id, estado);
            if (sucesso) {
                return "SUCESSO|Estado do alojamento " + id + " atualizado para: " + estado;
            } else {
                return "ERRO|Não foi possível atualizar o alojamento.";
            }
        } catch (NumberFormatException e) {
            return "ERRO|ID deve ser um número.";
        } catch (IllegalArgumentException e) {
            return "ERRO|Estado inválido. Use: PENDENTE, EM_OBRAS, ATIVO, SUSPENSO";
        } catch (SQLException e) {
            return "ERRO|Erro na base de dados: " + e.getMessage();
        }
    }

    private String handleAtualizarEstadoCandidatura(String idStr) {
        try {
            int candidaturaId = Integer.parseInt(idStr);

            // Primeiro verificar se a candidatura existe
            Optional<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);
            if (candidaturaOpt.isEmpty()) {
                return "ERRO|Candidatura com ID " + candidaturaId + " não encontrada.";
            }

            Candidatura candidatura = candidaturaOpt.get();

            // Enviar informações da candidatura
            StringBuilder info = new StringBuilder();
            info.append("=== CANDIDATURA #").append(candidaturaId).append(" ===\n");

            // Informações do candidato
            Optional<Candidato> candidatoOpt = candidatoService.findById(candidatura.getCandidatoId());
            if (candidatoOpt.isPresent()) {
                Candidato candidato = candidatoOpt.get();
                info.append("Candidato: ").append(candidato.getNome()).append("\n");
                info.append("Email: ").append(candidato.getEmail()).append("\n");
                info.append("Curso: ").append(candidato.getCurso()).append("\n");
            }

            // Informações do alojamento
            Optional<Alojamento> alojamentoOpt = alojamentoService.findById(candidatura.getAlojamentoId());
            if (alojamentoOpt.isPresent()) {
                Alojamento alojamento = alojamentoOpt.get();
                info.append("Alojamento: ").append(alojamento.getNome()).append(" - ").append(alojamento.getCidade()).append("\n");
            }

            info.append("Estado atual: ").append(candidatura.getEstado()).append("\n");
            info.append("Data da candidatura: ").append(candidatura.getDataCandidatura()).append("\n\n");

            info.append("Estados disponíveis: SUBMETIDA, EM_ANALISE, ACEITE, RECUSADA\n");
            info.append("Digite o novo estado para esta candidatura:");

            // Enviar informações primeiro
            sendMessage(info.toString());

            // Ler o novo estado do administrador
            String novoEstadoStr = in.readLine();
            if (novoEstadoStr == null || novoEstadoStr.trim().isEmpty()) {
                return "ERRO|Nenhum estado fornecido.";
            }

            try {
                EstadoCandidatura novoEstado = EstadoCandidatura.valueOf(novoEstadoStr.trim().toUpperCase());
                boolean sucesso = candidaturaService.atualizarEstadoCandidatura(candidaturaId, novoEstado);

                return sucesso ?
                        "SUCESSO|Estado da candidatura " + candidaturaId + " atualizado para: " + novoEstado :
                        "ERRO|Não foi possível atualizar o estado da candidatura.";

            } catch (IllegalArgumentException e) {
                return "ERRO|Estado inválido. Use: SUBMETIDA, EM_ANALISE, ACEITE, RECUSADA";
            }

        } catch (NumberFormatException e) {
            return "ERRO|ID deve ser um número.";
        } catch (SQLException e) {
            return "ERRO|Erro na base de dados: " + e.getMessage();
        } catch (IOException e) {
            return "ERRO|Erro de comunicação: " + e.getMessage();
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleListarCandidaturasPendentes() {
        try {
            List<Candidatura> candidaturas = candidaturaService.listarCandidaturasPendentes();

            if (candidaturas.isEmpty()) {
                return "INFO|Nenhuma candidatura pendente no momento.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== CANDIDATURAS PENDENTES ===\n\n");

            for (Candidatura c : candidaturas) {
                // Obter informações do candidato
                Optional<Candidato> candidatoOpt = candidatoService.findById(c.getCandidatoId());
                String nomeCandidato = candidatoOpt.map(Candidato::getNome).orElse("N/A");
                String emailCandidato = candidatoOpt.map(Candidato::getEmail).orElse("N/A");

                // Obter informações do alojamento
                Optional<Alojamento> alojamentoOpt = alojamentoService.findById(c.getAlojamentoId());
                String nomeAlojamento = alojamentoOpt.map(Alojamento::getNome).orElse("N/A");
                String cidadeAlojamento = alojamentoOpt.map(Alojamento::getCidade).orElse("N/A");

                sb.append("CANDIDATURA ID: ").append(c.getId()).append("\n");
                sb.append("  Candidato: ").append(nomeCandidato).append("\n");
                sb.append("  Email: ").append(emailCandidato).append("\n");
                sb.append("  Alojamento: ").append(nomeAlojamento).append(" (").append(cidadeAlojamento).append(")\n");
                sb.append("  Data: ").append(c.getDataCandidatura()).append("\n");
                sb.append("  Estado: ").append(c.getEstado()).append("\n");
                sb.append("  Use '3|").append(c.getId()).append("' para gerir esta candidatura\n\n");
            }

            sb.append("Total: ").append(candidaturas.size()).append(" candidatura(s) pendente(s)");

            return sb.toString();

        } catch (SQLException e) {
            return "ERRO|Erro ao listar candidaturas: " + e.getMessage();
        }
    }

    private String handleListarTodosAlojamentos() {
        try {
            List<Alojamento> alojamentos = alojamentoService.listarTodosAlojamentos();

            if (alojamentos.isEmpty()) {
                return "INFO|Nenhum alojamento registado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== TODOS OS ALOJAMENTOS ===\n\n");

            for (Alojamento a : alojamentos) {
                sb.append(String.format("ID: %2d | %-25s | %-15s | Capacidade: %3d | Estado: %-10s\n",
                        a.getId(), a.getNome(), a.getCidade(), a.getCapacidade(), a.getEstado()));
            }

            sb.append("\nUse '2|ID|ESTADO' para alterar o estado de um alojamento");

            return sb.toString();

        } catch (SQLException e) {
            return "ERRO|Erro ao listar alojamentos: " + e.getMessage();
        }
    }

    // ========== COMANDOS USUÁRIO ==========
    private String processUserCommand(int command) {
        try {
            switch (command) {
                case 1:
                    return handleNovaCandidatura();
                case 2:
                    return handleVerificarEstadoCandidatura();
                case 3:
                    return handleListarAlojamentosDisponiveis();
                case 4:
                    return "SAIR|A sair do sistema...";
                default:
                    return "ERRO|Opção inválida. Use 1, 2, 3 ou 4 para sair.";
            }
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleNovaCandidatura() {
        try {
            // Coletar dados do candidato
            sendMessage("=== NOVA CANDIDATURA ===");
            String nome = readInput("Digite seu nome completo: ");
            String email = readInput("Digite seu email: ");
            String telefone = readInput("Digite seu telefone: ");
            String curso = readInput("Digite seu curso: ");

            // Listar alojamentos disponíveis
            String alojamentos = handleListarAlojamentosDisponiveis();
            sendMessage(alojamentos);

            // Selecionar alojamento
            sendMessage("Digite o ID do alojamento desejado:");
            String resposta = in.readLine();

            if (resposta == null || resposta.trim().isEmpty()) {
                return "ERRO|Nenhum ID fornecido.";
            }

            int alojamentoId;
            try {
                alojamentoId = Integer.parseInt(resposta.trim());
            } catch (NumberFormatException e) {
                return "ERRO|ID inválido. Deve ser um número.";
            }

            // Registrar candidato e candidatura
            Candidato candidato = new Candidato(nome, email, telefone, Candidato.Sexo.OUTRO, curso);
            Candidato registado = candidatoService.registarCandidato(candidato, alojamentoId);

            // Obter o ID da candidatura criada
            List<Candidatura> candidaturas = candidaturaService.findByCandidatoId(registado.getId());
            String candidaturaInfo = "";
            if (!candidaturas.isEmpty()) {
                Candidatura ultimaCandidatura = candidaturas.get(0);
                candidaturaInfo = "\nID da Candidatura: " + ultimaCandidatura.getId() +
                        "\nEstado da Candidatura: " + ultimaCandidatura.getEstado();
            }

            return "SUCESSO|Candidatura submetida com sucesso!\n" +
                    "Seu ID de candidato: " + registado.getId() + candidaturaInfo +
                    "\nGuarde estes números para consultar o estado da sua candidatura!";

        } catch (IOException e) {
            return "ERRO|Erro de comunicação: " + e.getMessage();
        } catch (NumberFormatException e) {
            return "ERRO|ID do alojamento inválido.";
        } catch (SQLException e) {
            return "ERRO|Erro na base de dados: " + e.getMessage();
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleVerificarEstadoCandidatura() {
        try {
            sendMessage("=== VERIFICAR ESTADO DA CANDIDATURA ===");
            sendMessage("Digite o ID da sua candidatura:");

            String idStr = in.readLine();
            if (idStr == null || idStr.trim().isEmpty()) {
                return "ERRO|ID não fornecido.";
            }

            int candidaturaId;
            try {
                candidaturaId = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException e) {
                return "ERRO|ID deve ser um número.";
            }

            Optional<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);

            if (candidaturaOpt.isEmpty()) {
                return "ERRO|Candidatura não encontrada com o ID: " + candidaturaId;
            }

            Candidatura candidatura = candidaturaOpt.get();

            // Obter informações do candidato
            Optional<Candidato> candidatoOpt = candidatoService.findById(candidatura.getCandidatoId());
            String nomeCandidato = candidatoOpt.map(Candidato::getNome).orElse("N/A");

            // Obter informações do alojamento
            Optional<Alojamento> alojamentoOpt = alojamentoService.findById(candidatura.getAlojamentoId());
            String nomeAlojamento = alojamentoOpt.map(Alojamento::getNome).orElse("N/A");
            String cidadeAlojamento = alojamentoOpt.map(Alojamento::getCidade).orElse("N/A");

            StringBuilder sb = new StringBuilder();
            sb.append("=== ESTADO DA SUA CANDIDATURA ===\n\n");
            sb.append("ID da Candidatura: ").append(candidatura.getId()).append("\n");
            sb.append("Candidato: ").append(nomeCandidato).append("\n");
            sb.append("Alojamento: ").append(nomeAlojamento).append(" - ").append(cidadeAlojamento).append("\n");
            sb.append("Data da Candidatura: ").append(candidatura.getDataCandidatura()).append("\n");

            // Estado com mensagem descritiva
            String estadoFormatado;
            switch (candidatura.getEstado()) {
                case ACEITE:
                    estadoFormatado = "ACEITE - Parabéns! Sua candidatura foi aceita.";
                    break;
                case REJEITADA:
                    estadoFormatado = "RECUSADA - Infelizmente sua candidatura não foi aceita.";
                    break;
                case SUBMETIDA:
                    estadoFormatado = "SUBMETIDA - Sua candidatura está em análise.";
                    break;
                case EM_ANALISE:
                    estadoFormatado = "EM ANÁLISE - Sua candidatura está sendo analisada.";
                    break;
                default:
                    estadoFormatado = candidatura.getEstado().name();
            }

            sb.append("Estado: ").append(estadoFormatado).append("\n");

            return sb.toString();

        } catch (IOException e) {
            return "ERRO|Erro de comunicação: " + e.getMessage();
        } catch (SQLException e) {
            return "ERRO|Erro ao consultar candidatura: " + e.getMessage();
        }
    }

    private String handleListarAlojamentosDisponiveis() {
        try {
            List<Alojamento> disponiveis = alojamentoService.listarAlojamentosPorEstado(EstadoAlojamento.ATIVO);

            if (disponiveis.isEmpty()) {
                return "INFO|Nenhum alojamento disponível no momento.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== ALOJAMENTOS DISPONÍVEIS ===\n\n");
            for (Alojamento a : disponiveis) {
                sb.append(String.format("ID: %2d | %-20s | %-15s | Capacidade: %d\n",
                        a.getId(), a.getNome(), a.getCidade(), a.getCapacidade()));
            }
            return sb.toString();
        } catch (SQLException e) {
            return "ERRO|Erro ao carregar alojamentos: " + e.getMessage();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========
    private void sendMenu() {
        if (isAdmin) {
            sendMessage(getAdminMenu());
        } else {
            sendMessage(getUserMenu());
        }
    }

    private String getAdminMenu() {
        return "--------------------------------------------------\n" +
                "PAINEL ADMINISTRATIVO\n" +
                "--------------------------------------------------\n" +
                "COMANDOS DISPONÍVEIS:\n" +
                "1|Nome|Cidade|Capacidade  - Registar novo alojamento\n" +
                "2|ID|ESTADO               - Atualizar estado do alojamento\n" +
                "3|ID                      - Gerir candidatura (alterar estado)\n" +
                "4                         - Listar candidaturas pendentes\n" +
                "5                         - Listar todos alojamentos\n" +
                "SAIR                      - Encerrar sessão\n" +
                "--------------------------------------------------\n" +
                "EXEMPLOS:\n" +
                "1|Residência X|Porto|100\n" +
                "2|5|ATIVO\n" +
                "3|12 (irá pedir o novo estado)\n" +
                "--------------------------------------------------\n";
    }

    private String getUserMenu() {
        return "--------------------------------------------------\n" +
                "SISTEMA DE ALOJAMENTO ESTUDANTIL\n" +
                "--------------------------------------------------\n" +
                "OPÇÕES DISPONÍVEIS:\n" +
                "1 - Candidatar-se a alojamento\n" +
                "2 - Verificar estado da candidatura\n" +
                "3 - Listar alojamentos disponíveis\n" +
                "4 - Sair do sistema\n" +
                "--------------------------------------------------\n";
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
            }
            System.out.println("[HANDLER " + clientPort + "] Conexão encerrada.");
        } catch (IOException e) {
            System.err.println("Erro ao fechar recursos: " + e.getMessage());
        }
    }
}