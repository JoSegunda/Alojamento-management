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
        // Verificar se candidato já está na base de dados
        if (findByEmail(candidato.getEmail()).isPresent()) {
            throw new SQLException("Já existe um candidato com este email.");
        }

        String SQL = "INSERT INTO candidato (nome, email, telefone, sexo, curso, data_registo, estado) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(SQL)) {

            // Mapeamento dos campos do candidato para pStatement
            pStatement.setString(1, candidato.getNome());
            pStatement.setString(2, candidato.getEmail());
            pStatement.setString(3, candidato.getTelefone());
            pStatement.setString(4, candidato.getSexo().name());
            pStatement.setString(5, candidato.getCurso());
            pStatement.setDate(6, Date.valueOf(candidato.getDataRegisto())); // Converte LocalDate para SQL Date
            pStatement.setString(7, candidato.getEstado().name());

            ResultSet rs = pStatement.executeQuery();
            if (rs.next()) {
                candidato.setId(rs.getInt("id"));
                System.out.printf("Candidato salvo. ID: %d%n", candidato.getId());
                return candidato;
            }
            throw new SQLException("Falha ao obter o ID do candidato.");
        }
    }

    //Busca um candidato pelo seu id
    public Optional<Candidato> findById(int id) throws SQLException {
        String SQL = "SELECT * FROM candidato WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(SQL)) {

            pStatement.setInt(1, id);
            ResultSet rs = pStatement.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCandidato(rs));
            }
            return Optional.empty(); // Retorna vazio se não for encontrado
        }
    }

    // M para encontrar candidatos através do email
    public Optional<Candidato> findByEmail(String email) throws SQLException {
        String SQL = "SELECT * FROM candidato WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCandidato(rs));
            }
            return Optional.empty();
        }
    }

    // Atualizar o estado de um candidato
    public boolean updateEstado(int id, EstadoCandidato novoEstado) throws SQLException {
        String SQL = "UPDATE candidato SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(SQL)) {

            pStatement.setString(1, novoEstado.name());
            pStatement.setInt(2, id);

            int affectedRows = pStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.printf("Estado do candidato ID %d atualizado para: %s%n", id, novoEstado);
                return true;
            } else {
                System.out.printf("Candidato com ID %d não encontrado.%n", id);
                return false;
            }
        }
    }

}
