package repository;

import model.Alojamento;
import model.Alojamento.EstadoAlojamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlojamentoRepository {

    // M auxiliar para mapear um ResultSet para um objeto Alojamento
    private Alojamento mapResultSetToAlojamento(ResultSet rs) throws SQLException{
        return new Alojamento(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("cidade"),
                rs.getBoolean("active"),
                rs.getInt("Capacidade"),
                EstadoAlojamento.valueOf(rs.getString("estado").toUpperCase())
        );
    }
}
