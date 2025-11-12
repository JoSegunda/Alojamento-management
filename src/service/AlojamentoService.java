package service;

import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import repository.AlojamentoRepository;
import java.sql.SQLException;
import java.util.List;

public class AlojamentoService {

    private final AlojamentoRepository alojamentoRepository;

    public AlojamentoService(AlojamentoRepository alojamentoRepository) {
        this.alojamentoRepository = alojamentoRepository;
    }
    // Registar alojamento
    public Alojamento registarAlojamento(Alojamento alojamento) throws IllegalArgumentException, SQLException {

        if (alojamento == null) {
            throw new IllegalArgumentException("O objeto Alojamento não pode ser nulo.");
        }
        if (alojamento.getNome() == null || alojamento.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do alojamento é obrigatório.");
        }
        if (alojamento.getCidade() == null || alojamento.getCidade().trim().isEmpty()) {
            throw new IllegalArgumentException("A cidade é obrigatória.");
        }
        if (alojamento.getCapacidade() <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser um valor positivo.");
        }
        // Assume-se que o estado inicial é PENDENTE (definido no construtor)
        return alojamentoRepository.save(alojamento);
    }
}
