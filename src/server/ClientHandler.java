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
            // Identificar tipo de cliente
            String clientType = in.readLine();
            if (clientType != null && clientType.trim().equalsIgnoreCase("ADMIN")) {
                isAdmin = true;
                showMenu();
            } else {
                showMenu();
            }

            // Processar comandos
            processCommands();

        } catch (IOException e) {
            System.out.println("[HANDLER] Cliente " + clientPort + " desconectado: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void processCommands() throws IOException {
        String input;

        while (running && (input = in.readLine()) != null) {
            input = input.trim();

            if (input.equalsIgnoreCase("SAIR") || input.equals("4")) {
                sendMessage("Até logo! 👋");
                break;
            }

            String response = isAdmin ?
                    processAdminCommand(input) :
                    processUserCommand(input);

            sendMessage(response);

            // Mostrar menu novamente após cada comando
            showMenu();
        }
    }

    // ========== COMANDOS DE ADMIN ==========
    private String processAdminCommand(String command) {
        System.out.println("[ADMIN " + clientPort + "] Comando: " + command);

        String[] parts = command.split("\\|");
        String cmd = parts[0].trim();

        try {
            switch (cmd) {
                case "1": // Registar alojamento
                    return handleRegistarAlojamento(parts);
                case "2": // Atualizar estado do alojamento
                    return handleAtualizarEstadoAlojamento(parts);
                case "3": // Atualizar estado da candidatura
                    return handleAtualizarEstadoCandidatura(parts);
                case "4": // Listar candidaturas pendentes
                    return handleListarCandidaturasPendentes();
                case "5": // Listar todos alojamentos
                    return handleListarTodosAlojamentos();
                default:
                    return "ERRO|Comando não reconhecido. Use 1-5 ou SAIR.";
            }
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String handleRegistarAlojamento(String[] parts) throws SQLException {
        if (parts.length < 4) {
            return "ERRO|Formato: 1|Nome|Cidade|Capacidade\nExemplo: 1|Residência A|Lisboa|50";
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

        return "SUCESSO|✅ Alojamento criado com sucesso!\n" +
                "ID: " + registado.getId() +
                " | Nome: " + registado.getNome() +
                " | Estado: " + registado.getEstado();
    }

    private String handleAtualizarEstadoAlojamento(String[] parts) throws SQLException {
        if (parts.length < 3) {
            return "ERRO|Formato: 2|ID|ESTADO\nExemplo: 2|1|ATIVO\nEstados: PENDENTE, EM_OBRAS, ATIVO, SUSPENSO";
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
                "SUCESSO|✅ Estado do alojamento " + id + " atualizado para: " + novoEstado :
                "ERRO|❌ Falha na atualização do alojamento.";
    }

    private String handleAtualizarEstadoCandidatura(String[] parts) throws SQLException, IOException {
        if (parts.length < 2) {
            return "ERRO|Formato: 3|ID_CANDIDATURA\nExemplo: 3|5";
        }

        int candidaturaId;
        try {
            candidaturaId = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return "ERRO|ID deve ser um número";
        }

        // Primeiro verificar se a candidatura existe
        Optional<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);
        if (candidaturaOpt.isEmpty()) {
            return "ERRO|❌ Candidatura com ID " + candidaturaId + " não encontrada.";
        }

        Candidatura candidatura = candidaturaOpt.get();

        // Mostrar informações da candidatura
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
        info.append("Data da candidatura: ").append(candidatura.getDataCandidatura()).append("\n");

        // Enviar informações e pedir novo estado
        sendMessage(info.toString());
        sendMessage("Estados disponíveis: SUBMETIDA, EM_ANALISE, ACEITE, RECUSADA");
        sendMessage("Digite o novo estado para esta candidatura:");

        String novoEstadoStr = in.readLine();
        if (novoEstadoStr == null) {
            return "ERRO|Conexão interrompida.";
        }

        try {
            EstadoCandidatura novoEstado = EstadoCandidatura.valueOf(novoEstadoStr.trim().toUpperCase());
            boolean sucesso = candidaturaService.atualizarEstadoCandidatura(candidaturaId, novoEstado);

            return sucesso ?
                    "SUCESSO|✅ Estado da candidatura " + candidaturaId + " atualizado para: " + novoEstado :
                    "ERRO|❌ Não foi possível atualizar o estado da candidatura.";

        } catch (IllegalArgumentException e) {
            return "ERRO|Estado inválido. Use: SUBMETIDA, EM_ANALISE, ACEITE, RECUSADA";
        }
    }

    private String handleListarCandidaturasPendentes() {
        try {
            // Listar todas as candidaturas pendentes (SUBMETIDA)
            List<Candidatura> candidaturas = candidaturaService.listarCandidaturasPendentes();

            if (candidaturas.isEmpty()) {
                return "INFO|📭 Nenhuma candidatura pendente no momento.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== 📋 CANDIDATURAS PENDENTES ===\n\n");

            for (Candidatura c : candidaturas) {
                // Obter informações do candidato
                Optional<Candidato> candidatoOpt = candidatoService.findById(c.getCandidatoId());
                String nomeCandidato = candidatoOpt.map(Candidato::getNome).orElse("N/A");
                String emailCandidato = candidatoOpt.map(Candidato::getEmail).orElse("N/A");

                // Obter informações do alojamento
                Optional<Alojamento> alojamentoOpt = alojamentoService.findById(c.getAlojamentoId());
                String nomeAlojamento = alojamentoOpt.map(Alojamento::getNome).orElse("N/A");
                String cidadeAlojamento = alojamentoOpt.map(Alojamento::getCidade).orElse("N/A");

                sb.append("┌─ CANDIDATURA ID: ").append(c.getId()).append("\n");
                sb.append("│  Candidato: ").append(nomeCandidato).append("\n");
                sb.append("│  Email: ").append(emailCandidato).append("\n");
                sb.append("│  Alojamento: ").append(nomeAlojamento).append(" (").append(cidadeAlojamento).append(")\n");
                sb.append("│  Data: ").append(c.getDataCandidatura()).append("\n");
                sb.append("│  Estado: ").append(c.getEstado()).append("\n");
                sb.append("└─ Use '3|").append(c.getId()).append("' para gerir esta candidatura\n\n");
            }

            sb.append("Total: ").append(candidaturas.size()).append(" candidatura(s) pendente(s)");

            return sb.toString();

        } catch (SQLException e) {
            return "ERRO|❌ Erro ao listar candidaturas: " + e.getMessage();
        }
    }

    private String handleListarTodosAlojamentos() {
        try {
            // Para admin, listar todos os alojamentos (não apenas os ativos)
            List<Alojamento> alojamentos = alojamentoService.listarAlojamentosPorEstado(null);

            if (alojamentos.isEmpty()) {
                return "INFO|🏠 Nenhum alojamento registado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== 🏘️ TODOS OS ALOJAMENTOS ===\n\n");

            for (Alojamento a : alojamentos) {
                String estadoEmoji = switch (a.getEstado()) {
                    case ATIVO -> "✅";
                    case EM_OBRAS -> "🚧";
                    case FECHADO -> "⏸️";
                    default -> "❓";
                };

                sb.append(String.format("%s ID: %2d | %-25s | %-15s | Cap: %3d | Estado: %-10s\n",
                        estadoEmoji, a.getId(), a.getNome(), a.getCidade(), a.getCapacidade(), a.getEstado()));
            }

            sb.append("\nUse '2|ID|ESTADO' para alterar o estado de um alojamento");

            return sb.toString();

        } catch (SQLException e) {
            return "ERRO|❌ Erro ao listar alojamentos: " + e.getMessage();
        }
    }

    // ========== PROCESSAMENTO USUÁRIO ==========
    private String processUserCommand(String command) {
        try {
            int opcao = Integer.parseInt(command);

            switch (opcao) {
                case 1:
                    return iniciarCandidatura();
                case 2:
                    return verificarEstadoCandidatura();
                case 3:
                    return listarAlojamentosDisponiveis();
                default:
                    return "ERRO|❌ Opção inválida. Use 1, 2, 3 ou 4 para sair.";
            }
        } catch (NumberFormatException e) {
            return "ERRO|❌ Digite um número (1-4).";
        } catch (Exception e) {
            return "ERRO|❌ " + e.getMessage();
        }
    }

    private String iniciarCandidatura() {
        try {
            // Coletar dados básicos primeiro
            sendMessage("=== 📄 NOVA CANDIDATURA ===");
            String nome = readInput("Digite seu nome completo: ");
            String email = readInput("Digite seu email: ");
            String telefone = readInput("Digite seu telefone: ");
            String curso = readInput("Digite seu curso: ");

            // Mostrar alojamentos disponíveis
            String alojamentos = listarAlojamentosDisponiveis();
            sendMessage(alojamentos);

            String alojamentoIdStr = readInput("Digite o ID do alojamento desejado: ");
            int alojamentoId = Integer.parseInt(alojamentoIdStr);

            // Criar candidato
            Candidato candidato = new Candidato(nome, email, telefone, Candidato.Sexo.OUTRO, curso);
            Candidato registado = candidatoService.registarCandidato(candidato, alojamentoId);

            // Obter a candidatura criada
            List<Candidatura> candidaturas = candidaturaService.findByCandidatoId(registado.getId());

            String resultado = "✅ CANDIDATURA SUBMETIDA COM SUCESSO!\n\n";
            resultado += "📋 DADOS DO REGISTO:\n";
            resultado += "ID do Candidato: " + registado.getId() + "\n";
            resultado += "Nome: " + registado.getNome() + "\n";
            resultado += "Email: " + registado.getEmail() + "\n";

            if (!candidaturas.isEmpty()) {
                Candidatura ultimaCandidatura = candidaturas.get(0);
                resultado += "ID da Candidatura: " + ultimaCandidatura.getId() + "\n";
                resultado += "Estado: " + ultimaCandidatura.getEstado() + "\n";
                resultado += "Data: " + ultimaCandidatura.getDataCandidatura() + "\n\n";
                resultado += "⚠️ GUARDE O ID DA CANDIDATURA (" + ultimaCandidatura.getId() + ") PARA CONSULTAR O ESTADO FUTURAMENTE!";
            }

            return resultado;

        } catch (NumberFormatException e) {
            return "ERRO|❌ ID do alojamento inválido.";
        } catch (SQLException e) {
            return "ERRO|❌ Erro na base de dados: " + e.getMessage();
        } catch (Exception e) {
            return "ERRO|❌ " + e.getMessage();
        }
    }

    private String verificarEstadoCandidatura() {
        try {
            sendMessage("=== 🔍 VERIFICAR ESTADO DA CANDIDATURA ===");
            String idStr = readInput("Digite o ID da sua candidatura: ");

            if (idStr == null || idStr.trim().isEmpty()) {
                return "ERRO|❌ ID não fornecido.";
            }

            int candidaturaId = Integer.parseInt(idStr.trim());
            Optional<Candidatura> candidaturaOpt = candidaturaService.findById(candidaturaId);

            if (candidaturaOpt.isEmpty()) {
                return "ERRO|❌ Candidatura não encontrada com o ID: " + candidaturaId;
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
            sb.append("=== 📊 ESTADO DA SUA CANDIDATURA ===\n\n");
            sb.append("ID da Candidatura: ").append(candidatura.getId()).append("\n");
            sb.append("Candidato: ").append(nomeCandidato).append("\n");
            sb.append("Alojamento: ").append(nomeAlojamento).append(" - ").append(cidadeAlojamento).append("\n");
            sb.append("Data da Candidatura: ").append(candidatura.getDataCandidatura()).append("\n");

            // Estado com cor simbólica
            String estadoFormatado;
            switch (candidatura.getEstado()) {
                case ACEITE:
                    estadoFormatado = "✅ ACEITE - Parabéns! Sua candidatura foi aceita.";
                    break;
                case REJEITADA:
                    estadoFormatado = "❌ RECUSADA - Infelizmente sua candidatura não foi aceita.";
                    break;
                case SUBMETIDA:
                    estadoFormatado = "⏳ SUBMETIDA - Sua candidatura está em análise.";
                    break;
                case EM_ANALISE:
                    estadoFormatado = "🔍 EM ANÁLISE - Sua candidatura está sendo analisada.";
                    break;
                default:
                    estadoFormatado = candidatura.getEstado().name();
            }

            sb.append("Estado: ").append(estadoFormatado).append("\n");

            return sb.toString();

        } catch (NumberFormatException e) {
            return "ERRO|❌ ID deve ser um número.";
        } catch (SQLException e) {
            return "ERRO|❌ Erro ao consultar candidatura: " + e.getMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String listarAlojamentosDisponiveis() {
        try {
            List<Alojamento> disponiveis = alojamentoService.listarAlojamentosPorEstado(EstadoAlojamento.ATIVO);

            if (disponiveis.isEmpty()) {
                return "ℹ️ Nenhum alojamento disponível no momento.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== 🏠 ALOJAMENTOS DISPONÍVEIS ===\n\n");

            for (Alojamento a : disponiveis) {
                sb.append(String.format("ID: %2d | %-25s | %-15s | Vagas: %d\n",
                        a.getId(), a.getNome(), a.getCidade(), a.getCapacidade()));
            }

            return sb.toString();

        } catch (SQLException e) {
            return "ERRO|❌ Erro ao carregar alojamentos: " + e.getMessage();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========
    private void showMenu() {
        if (isAdmin) {
            sendMessage(getAdminMenu());
        } else {
            sendMessage(getUserMenu());
        }
    }

    private String getAdminMenu() {
        return """
            ╔══════════════════════════════════════════════════╗
            ║             👑 PAINEL ADMINISTRATIVO             ║
            ╚══════════════════════════════════════════════════╝
            
            📋 COMANDOS DISPONÍVEIS:
            
            1|Nome|Cidade|Capacidade  - 🏠 Registar novo alojamento
            2|ID|ESTADO               - 🔄 Atualizar estado do alojamento
            3|ID                      - 📝 Gerir candidatura (alterar estado)
            4                         - 📋 Listar candidaturas pendentes
            5                         - 🏘️ Listar todos alojamentos
            SAIR                      - 🚪 Encerrar sessão
            
            📝 EXEMPLOS:
            1|Residência X|Porto|100
            2|5|ATIVO
            3|12  (irá pedir o novo estado)
            
            ══════════════════════════════════════════════════
            """;
    }

    private String getUserMenu() {
        return """
            ╔══════════════════════════════════════════════════╗
            ║       🎓 SISTEMA DE ALOJAMENTO ESTUDANTIL        ║
            ╚══════════════════════════════════════════════════╝
            
            📋 OPÇÕES DISPONÍVEIS:
            
            1 - 📄 Candidatar-se a alojamento
            2 - 🔍 Verificar estado da candidatura
            3 - 🏠 Listar alojamentos disponíveis
            4 - 🚪 Sair do sistema
            
            ══════════════════════════════════════════════════
            """;
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