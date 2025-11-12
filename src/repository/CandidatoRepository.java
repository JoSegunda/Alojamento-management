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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            // ... Mapeamento dos campos do candidato para pstmt ...
            pstmt.setString(1, candidato.getNome());
            pstmt.setString(2, candidato.getEmail());
            pstmt.setString(3, candidato.getTelefone());
            pstmt.setString(4, candidato.getSexo().name());
            pstmt.setString(5, candidato.getCurso());
            pstmt.setDate(6, Date.valueOf(candidato.getDataRegisto())); // Converte LocalDate para SQL Date
            pstmt.setString(7, candidato.getEstado().name());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                candidato.setId(rs.getInt("id"));
                System.out.printf("Candidato salvo. ID: %d%n", candidato.getId());
                return candidato;
            }
            throw new SQLException("Falha ao obter o ID do candidato após a inserção.");
        }
    }

    //Busca um candidato pelo seu ID
    public Optional<Candidato> findById(int id) throws SQLException {
        String SQL = "SELECT * FROM candidatos WHERE id = ?";
    }
}
