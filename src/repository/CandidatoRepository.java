package repository;

import model.Candidato;
import model.Candidato.Sexo; // Importar o ENUM
import model.Candidato.EstadoCandidato;
import java.sql.*;
import java.util.Optional;

public class CandidatoRepository {

    // M auxiliar para mapear um ResultSet para um objeto Candidato
    private Candidato mapResultSetToCandidato(ResultSet rs) throws SQLException {
        return new Candidato(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("telefone"),
                Sexo.valueOf(rs.getString("sexo").toUpperCase()),
                rs.getString("curso"),
                rs.getDate("data_registo").toLocalDate(), // Converte SQL Date para LocalDate
                EstadoCandidato.valueOf(rs.getString("estado").toUpperCase())
        );
    }
    //Insere um novo candidato no PostgreSQL.
    public Candidato save(Candidato candidato) throws SQLException {
        String SQL = "INSERT INTO candidatos (nome, email, telefone, sexo, curso, data_registo, estado) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

    }
}
