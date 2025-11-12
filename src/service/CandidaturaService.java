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

    // Depende de três repositórios
    public CandidaturaService(CandidaturaRepository candidaturaRepository,
                              AlojamentoRepository alojamentoRepository,
                              CandidatoRepository candidatoRepository) {
        this.candidaturaRepository = candidaturaRepository;
        this.alojamentoRepository = alojamentoRepository;
        this.candidatoRepository = candidatoRepository;
    }
    // Method para submeter candidatura
    public Candidatura submeterCandidatura(int alojamentoId, int candidatoId) throws IllegalArgumentException, SQLException {

        // Chaves Estrangeiras)
        Alojamento alojamento = alojamentoRepository.findById(alojamentoId);
        Optional<Candidato> candidatoOpt = candidatoRepository.findById(candidatoId);

        if (alojamento == null) {
            throw new IllegalArgumentException("Alojamento não existe.");
        }
        if (candidatoOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidato não existe.");
        }
        // O alojamento deve estar aprovado para receber candidaturas
        if (alojamento.getEstado() != Alojamento.EstadoAlojamento.APROVADO) {
            throw new IllegalArgumentException("O alojamento não está disponível para candidaturas.");
        }

        // Verifica se o candidato já tem uma candidatura ativa para este alojamento
        if (candidaturaRepository.findByAlojamentoAndCandidato(alojamentoId, candidatoId).isPresent()) {
            throw new IllegalArgumentException("O candidato já submeteu uma candidatura para este alojamento.");
        }
        Candidatura novaCandidatura = new Candidatura(alojamentoId, candidatoId);
        return candidaturaRepository.save(novaCandidatura);
    }
    // M para Alterar o estado da candidatura
    public boolean aceitarCandidatura(int candidaturaId) throws IllegalArgumentException, SQLException {

        List<Candidatura> candidaturaOpt = candidaturaRepository.listarPorCandidato(candidaturaId);

        if (candidaturaOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidatura não encontrada.");
        }
        Candidatura candidatura = candidaturaOpt.getFirst();

        // Só se pode aceitar se o estado atual for SUBMETIDA ou EM_ANALISE
        if (!candidatura.isAtiva()) {
            throw new IllegalArgumentException("A candidatura não está em um estado que permita aceitação.");
        }
        candidatura.aceitar();

        // 2. TODO: recusar candidaturas.

        return candidaturaRepository.updateEstado(candidaturaId, EstadoCandidatura.ACEITE);
    }
}
