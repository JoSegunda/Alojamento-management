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
}
