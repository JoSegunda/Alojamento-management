package service;

import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import repository.AlojamentoRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    // M para atualizar o estado do alojamento
    public boolean atualizarEstado(int alojamentoId, EstadoAlojamento novoEstado) throws IllegalArgumentException, SQLException {

        Alojamento alojamento = alojamentoRepository.findById(alojamentoId);

        if (alojamento == null) {
            throw new IllegalArgumentException("Alojamento com ID " + alojamentoId + " não encontrado.");
        }
        // Transição de estado
        // Não se pode aprovar um alojamento que está em obras sem correções
        if (alojamento.getEstado() == EstadoAlojamento.EM_OBRAS && novoEstado == EstadoAlojamento.ATIVO) {
            throw new IllegalArgumentException("Não é possível aprovar um alojamento INCOMPLETO diretamente.");
        }
        // Se o estado for o mesmo, não faz nada
        if (alojamento.getEstado() == novoEstado) {
            return true;
        }

        return alojamentoRepository.updateEstado(alojamentoId, novoEstado);
    }

    // Listar alojamentos por estado
    public List<Alojamento> listarAlojamentosPorEstado(EstadoAlojamento estado) throws SQLException {
        if (estado == null) {
            return alojamentoRepository.findAll();
        }
        return alojamentoRepository.findByEstado(estado);
    }

    public Optional<Alojamento> findById(int id) throws SQLException {
        Alojamento alojamento = alojamentoRepository.findById(id);
        return Optional.ofNullable(alojamento);
    }
}
