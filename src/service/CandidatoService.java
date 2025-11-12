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
}
