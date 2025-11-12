package repository;

import model.Candidatura;
import model.Candidatura.EstadoCandidatura; // Importar o ENUM
import java.sql.*;
import java.util.Optional;

public class CandidaturaRepository {
    // M auxiliar para mapear um ResultSet para um objeto Candidatura
    private Candidatura mapResultSetToCandidatura(ResultSet rs) throws SQLException {
        return new Candidatura(
                rs.getInt("id"),
                rs.getInt("alojamento_id"),
                rs.getInt("candidato_id"),
                rs.getDate("data_candidatura").toLocalDate(),
                EstadoCandidatura.valueOf(rs.getString("estado").toUpperCase())
        );
    }
}
