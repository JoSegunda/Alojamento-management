package server;

import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import model.Candidato;
import model.Candidatura;
import repository.AlojamentoRepository;
import repository.CandidatoRepository;
import repository.CandidaturaRepository;
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

    // Serviços e Repositórios
    private final AlojamentoService alojamentoService;
    private final CandidatoService candidatoService;
    private final CandidaturaService candidaturaService;
    private final AlojamentoRepository alojamentoRepository;

    private boolean isAdmin = false;

    public ClientHandler(Socket socket,
                         AlojamentoService alojamentoService,
                         CandidatoService candidatoService,
                         CandidaturaService candidaturaService) throws IOException {
        this.clientSocket = socket;
        this.clientPort = socket.getPort();
        this.alojamentoService = alojamentoService;
        this.candidatoService = candidatoService;
        this.candidaturaService = candidaturaService;

        // Inicializar repositório para métodos adicionais
        this.alojamentoRepository = new AlojamentoRepository();

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
                sendMessage(getAdminMenu());
            } else {
                sendMessage(getUserMenu());
            }

            // Processar comandos
            processCommands();

        } catch (IOException e) {
            System.out.println("[HANDLER] Cliente " + clientPort + " desconectado.");
        } finally {
            closeResources();
        }
    }

    private void processCommands() throws IOException {
        String input;
        while ((input = in.readLine()) != null) {
            input = input.trim();

            if (input.equalsIgnoreCase("SAIR") || input.equals("4")) {
                sendMessage("Até logo! 👋");
                break;
            }

            String response = isAdmin ?
                    processAdminInput(input) :
                    processUserInput(input);

            sendMessage(response);

            // Se não for admin, mostrar menu novamente após cada comando
            if (!isAdmin && !response.contains("ERRO")) {
                sendMessage(getUserMenu());
            } else if (isAdmin) {
                sendMessage(getAdminMenu());
            }
        }
    }

    // ========== PROCESSAMENTO ADMIN ==========
    private String processAdminInput(String input) {
        System.out.println("[ADMIN " + clientPort + "] Comando: " + input);

        String[] parts = input.split("\\|");
        String command = parts[0].trim();

        try {
            switch (command) {
                case "1":
                case "REGISTAR_ALOJAMENTO":
                    if (parts.length < 4) {
                        return "ERRO|Formato: 1|Nome|Cidade|Capacidade\nExemplo: 1|Residência A|Lisboa|50";
                    }
                    return registarAlojamento(parts[1], parts[2], parts[3]);

                case "2":
                case "ATUALIZAR_ESTADO_ALOJAMENTO":
                    if (parts.length < 3) {
                        return "ERRO|Formato: 2|ID|ESTADO\nExemplo: 2|1|ATIVO\nEstados: PENDENTE, EM_OBRAS, ATIVO, SUSPENSO";
                    }
                    return atualizarEstadoAlojamento(parts[1], parts[2]);

                case "3":
                case "ACEITAR_CANDIDATURA":
                    if (parts.length < 2) {
                        return "ERRO|Formato: 3|ID_CANDIDATURA\nExemplo: 3|5";
                    }
                    return aceitarCandidatura(parts[1]);

                case "4":
                case "LISTAR_CANDIDATURAS":
                    return "SUCESSO|Lista de candidaturas:\n(Esta funcionalidade será implementada)";

                case "5":
                case "LISTAR_ALOJAMENTOS":
                    return listarTodosAlojamentos();

                default:
                    return "ERRO|Comando não reconhecido. Use os números 1-5 ou os comandos completos.";
            }
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String registarAlojamento(String nome, String cidade, String capacidadeStr) {
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

    private String atualizarEstadoAlojamento(String idStr, String estadoStr) {
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

    private String aceitarCandidatura(String idStr) {
        try {
            int candidaturaId = Integer.parseInt(idStr);
            boolean sucesso = candidaturaService.aceitarCandidatura(candidaturaId);

            if (sucesso) {
                return "SUCESSO|Candidatura " + candidaturaId + " aceite com sucesso!";
            } else {
                return "ERRO|Não foi possível aceitar a candidatura.\nVerifique se existe e está no estado correto.";
            }
        } catch (NumberFormatException e) {
            return "ERRO|ID deve ser um número.";
        } catch (SQLException e) {
            return "ERRO|Erro na base de dados: " + e.getMessage();
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }

    private String listarTodosAlojamentos() {
        try {
            List<Alojamento> alojamentos = alojamentoRepository.findAll();

            if (alojamentos.isEmpty()) {
                return "INFO|Nenhum alojamento registado.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== TODOS OS ALOJAMENTOS ===\n");
            for (Alojamento a : alojamentos) {
                sb.append(String.format("ID: %2d | %-20s | %-15s | Cap: %3d | Estado: %-10s\n",
                        a.getId(), a.getNome(), a.getCidade(), a.getCapacidade(), a.getEstado()));
            }
            return sb.toString();
        } catch (SQLException e) {
            return "ERRO|Erro ao listar alojamentos: " + e.getMessage();
        }
    }

    // ========== PROCESSAMENTO USUÁRIO ==========
    private String processUserInput(String input) {
        System.out.println("[USER " + clientPort + "] Opção: " + input);

        try {
            int opcao = Integer.parseInt(input);

            switch (opcao) {
                case 1:
                    return iniciarCandidatura();
                case 2:
                    return "SUCESSO|Para verificar o estado da sua candidatura, contacte a administração.";
                case 3:
                    return listarAlojamentosDisponiveis();
                default:
                    return "ERRO|Opção inválida. Use 1, 2, 3 ou 4 para sair.";
            }
        } catch (NumberFormatException e) {
            return "ERRO|Digite um número (1-4).";
        }
    }

    private String iniciarCandidatura() {
        try {
            // Coletar dados básicos primeiro
            sendMessage("=== NOVA CANDIDATURA ===\nDigite seu nome completo:");
            String nome = in.readLine();

            sendMessage("Digite seu email:");
            String email = in.readLine();

            sendMessage("Digite seu telefone:");
            String telefone = in.readLine();

            sendMessage("Digite seu curso:");
            String curso = in.readLine();

            // Mostrar alojamentos disponíveis
            String alojamentos = listarAlojamentosDisponiveis();
            sendMessage(alojamentos + "\nDigite o ID do alojamento desejado:");

            String alojamentoIdStr = in.readLine();
            int alojamentoId = Integer.parseInt(alojamentoIdStr);

            // Criar candidato
            Candidato candidato = new Candidato(nome, email, telefone,
                    Candidato.Sexo.OUTRO, curso); // Sexo padrão

            Candidato registado = candidatoService.registarCandidato(candidato, alojamentoId);

            return "SUCESSO|Candidatura submetida com sucesso!\n" +
                    "Seu ID de candidato: " + registado.getId() + "\n" +
                    "Aguarde contato da administração.";

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

    private String listarAlojamentosDisponiveis() {
        try {
            List<Alojamento> disponiveis = alojamentoService.listarAlojamentosPorEstado(EstadoAlojamento.ATIVO);

            if (disponiveis.isEmpty()) {
                return "INFO|Nenhum alojamento disponível no momento.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== ALOJAMENTOS DISPONÍVEIS ===\n");
            for (Alojamento a : disponiveis) {
                sb.append(String.format("ID: %2d | %-20s | %-15s | Vagas: %d\n",
                        a.getId(), a.getNome(), a.getCidade(), a.getCapacidade()));
            }
            return sb.toString();
        } catch (SQLException e) {
            return "ERRO|Erro ao carregar alojamentos: " + e.getMessage();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========
    private String getAdminMenu() {
        return """
            ╔══════════════════════════════════════════════════╗
            ║             PAINEL ADMINISTRATIVO                ║
            ╚══════════════════════════════════════════════════╝
            
            COMANDOS:
            1|Nome|Cidade|Capacidade  - Registar novo alojamento
            2|ID|ESTADO               - Atualizar estado do alojamento
            3|ID                      - Aceitar candidatura
            4                         - Listar candidaturas pendentes
            5                         - Listar todos alojamentos
            SAIR                      - Encerrar sessão
            
            Exemplos:
            1|Residência X|Porto|100
            2|5|ATIVO
            3|12
            
            ══════════════════════════════════════════════════
            """;
    }

    private String getUserMenu() {
        return """
            ╔══════════════════════════════════════════════════╗
            ║       SISTEMA DE ALOJAMENTO ESTUDANTIL           ║
            ╚══════════════════════════════════════════════════╝
            
            OPÇÕES:
            1 - Candidatar-se a alojamento
            2 - Verificar estado da candidatura
            3 - Listar alojamentos disponíveis
            4 - Sair do sistema
            
            ══════════════════════════════════════════════════
            """;
    }

    private void sendMessage(String message) {
        out.println(message);
        out.println("END");
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