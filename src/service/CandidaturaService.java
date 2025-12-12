package service;

import model.Candidatura;
import model.Candidatura.EstadoCandidatura;
import model.Alojamento;
import model.Candidato;
import repository.CandidaturaRepository;
import repository.AlojamentoRepository;
import repository.CandidatoRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final AlojamentoRepository alojamentoRepository;
    private final CandidatoRepository candidatoRepository;

    public CandidaturaService(CandidaturaRepository candidaturaRepository,
                              AlojamentoRepository alojamentoRepository,
                              CandidatoRepository candidatoRepository) {
        this.candidaturaRepository = candidaturaRepository;
        this.alojamentoRepository = alojamentoRepository;
        this.candidatoRepository = candidatoRepository;
    }

    // Método para submeter candidatura
    public Candidatura submeterCandidatura(int alojamentoId, int candidatoId) throws IllegalArgumentException, SQLException {
        Alojamento alojamento = alojamentoRepository.findById(alojamentoId);
        Optional<Candidato> candidatoOpt = candidatoRepository.findById(candidatoId);

        if (alojamento == null) {
            throw new IllegalArgumentException("Alojamento não existe.");
        }
        if (candidatoOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidato não existe.");
        }
        if (alojamento.getEstado() != Alojamento.EstadoAlojamento.ATIVO) {
            throw new IllegalArgumentException("O alojamento não está disponível para candidaturas.");
        }

        if (candidaturaRepository.findByAlojamentoAndCandidato(alojamentoId, candidatoId).isPresent()) {
            throw new IllegalArgumentException("O candidato já submeteu uma candidatura para este alojamento.");
        }

        Candidatura novaCandidatura = new Candidatura(alojamentoId, candidatoId);
        return candidaturaRepository.save(novaCandidatura);
    }

    // Méto do para aceitar candidatura
    public boolean aceitarCandidatura(int candidaturaId) throws IllegalArgumentException, SQLException {
        Optional<Candidatura> candidaturaOpt = candidaturaRepository.findById(candidaturaId);

        if (candidaturaOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidatura não encontrada.");
        }

        Candidatura candidatura = candidaturaOpt.get();

        if (!candidatura.isAtiva()) {
            throw new IllegalArgumentException("A candidatura não está em um estado que permita aceitação.");
        }

        candidatura.aceitar();
        return candidaturaRepository.updateEstado(candidaturaId, EstadoCandidatura.ACEITE);
    }

    // Método para recusar candidatura
    public boolean recusarCandidatura(int candidaturaId) throws SQLException {
        Optional<Candidatura> candidaturaOpt = candidaturaRepository.findById(candidaturaId);

        if (candidaturaOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidatura não encontrada.");
        }

        return candidaturaRepository.updateEstado(candidaturaId, EstadoCandidatura.REJEITADA);
    }

    // Adicionar este método à classe CandidaturaService:
    public boolean atualizarEstadoCandidatura(int candidaturaId, EstadoCandidatura novoEstado) throws SQLException {
        Optional<Candidatura> candidaturaOpt = candidaturaRepository.findById(candidaturaId);

        if (candidaturaOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidatura não encontrada.");
        }

        Candidatura candidatura = candidaturaOpt.get();

        // Validações específicas
        if (novoEstado == EstadoCandidatura.ACEITE) {
            if (!candidatura.isAtiva()) {
                throw new IllegalArgumentException("A candidatura não está em um estado que permita aceitação.");
            }
        }

        // Atualiza o estado
        return candidaturaRepository.updateEstado(candidaturaId, novoEstado);
    }

    // Método para listar candidaturas pendentes
    public List<Candidatura> listarCandidaturasPendentes() throws SQLException {
        return candidaturaRepository.findByEstado(EstadoCandidatura.SUBMETIDA);
    }

    // Método para buscar candidatura por ID
    public Optional<Candidatura> findById(int id) throws SQLException {
        return candidaturaRepository.findById(id);
    }

    // Método para buscar candidaturas por candidato
    public List<Candidatura> findByCandidatoId(int candidatoId) throws SQLException {
        return candidaturaRepository.listarPorCandidato(candidatoId);
    }
}