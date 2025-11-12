package service;

import model.Candidatura;
import model.Candidatura.EstadoCandidatura;
import model.Alojamento;
import model.Candidato;
import repository.CandidaturaRepository;
import repository.AlojamentoRepository;
import repository.CandidatoRepository;
import java.sql.SQLException;
import java.util.Optional;

public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final AlojamentoRepository alojamentoRepository;
    private final CandidatoRepository candidatoRepository;
}
