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
    // Insere uma nova candidatura no PostgreSQL.
    public Candidatura save(Candidatura candidatura) throws SQLException{

        // Verificar se já existe uma candidatura com este id
        String checkSQL = "SELECT 1 FROM candidatura WHERE alojamento_id=? AND candidato_id=? AND estado IN ('SUBMETIDA','EM_ANALISE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, candidatura.getAlojamentoId());
            checkStmt.setInt(2, candidatura.getCandidatoId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new SQLException("Já existe uma candidatura ativa para este alojamento e candidato.");
            }
        }


        String SQL = "INSERT INTO candidatura (alojamento_id, candidato_id, data_candidatura, estado) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(SQL)) {

            pStatement.setInt(1, candidatura.getAlojamentoId());
            pStatement.setInt(2, candidatura.getCandidatoId());
            pStatement.setDate(3, Date.valueOf(candidatura.getDataCandidatura()));
            pStatement.setString(4, candidatura.getEstado().name());

            ResultSet rs = pStatement.executeQuery();

            if (rs.next()) {
                candidatura.setId(rs.getInt("id"));
                System.out.printf("Candidatura salva. ID: %d%n", candidatura.getId());
                return candidatura;
            }
            throw new SQLException("Falha ao obter o ID da candidatura após a inserção.");
        }
    }
    //Atualiza o estado da candidatura
    public boolean updateEstado(int id, EstadoCandidatura novoEstado) throws SQLException {
        String SQL = "UPDATE candidatura SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(SQL)) {

            pStatement.setString(1, novoEstado.name());
            pStatement.setInt(2, id);

            int affectedRows = pStatement.executeUpdate();
            return affectedRows > 0;
        }
    }
}
