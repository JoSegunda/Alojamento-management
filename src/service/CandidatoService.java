package service;

import model.Candidato;
import model.Candidato.EstadoCandidato;
import repository.CandidatoRepository;
import java.sql.SQLException;
import java.util.Optional;

public class CandidatoService {

    private final CandidatoRepository candidatoRepository;

    public CandidatoService(CandidatoRepository candidatoRepository) {
        this.candidatoRepository = candidatoRepository;
    }

    public Candidato registarCandidato(Candidato candidato) throws IllegalArgumentException, SQLException {
        // Verificar
        if (candidato == null) {
            throw new IllegalArgumentException("O objeto Candidato não pode ser nulo.");
        }
        if (candidato.getNome() == null || candidato.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do candidato é obrigatório.");
        }
        if (!candidato.getEmail().contains("@")) { // Validação de email
            throw new IllegalArgumentException("Formato de email inválido.");
        }
        // verificar se já existe candidato
        if (candidatoRepository.findByEmail(candidato.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um candidato registado com este email.");
        }
        return candidatoRepository.save(candidato);
    }
    // M para suspender candidato
    public boolean suspenderCandidato(int candidatoId) throws SQLException {
        Optional<Candidato> candidatoOpt = candidatoRepository.findById(candidatoId);

        if (candidatoOpt.isEmpty()) {
            throw new IllegalArgumentException("Candidato com ID " + candidatoId + " não encontrado.");
        }

        Candidato candidato = candidatoOpt.get();

        // usar o method do modelo e atualizar na BD
        if (candidato.getEstado() != EstadoCandidato.SUSPENSO) {
            candidato.suspender();
            return candidatoRepository.updateEstado(candidatoId, EstadoCandidato.SUSPENSO);
        }
        return true; // Já estava suspenso
    }
}

