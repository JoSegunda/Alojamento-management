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
                rs.getInt("capacidade"),
                EstadoAlojamento.valueOf(rs.getString("estado").toUpperCase())
        );
    }
    // Inserir um novo alojamento no PostgreSQL.
    public Alojamento save(Alojamento alojamento) throws SQLException{
        String SQL = "INSERT INTO alojamento (nome, cidade, capacidade, active, estado) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pStatement = conn.prepareStatement(SQL)){

            pStatement.setString(1, alojamento.getNome());
            pStatement.setString(2, alojamento.getCidade());
            pStatement.setInt(3, alojamento.getCapacidade());
            pStatement.setBoolean(4, alojamento.isActive());
            // Converter o enum para String
            pStatement.setString(5, alojamento.getEstado().name());

            ResultSet rs = pStatement.executeQuery();

            if (rs.next()){
                // Define o ID gerado pela BD no objeto java
                alojamento.setId(rs.getInt("id"));
                System.out.printf("Alojamento salvo, ID: %d%n", alojamento.getId());
                return alojamento;
            }
            throw new SQLException("Falha ao obter o ID Depois da inserção");
        }// O try-with-resources garante que conn e pStatement são fechados
    }

    // Atualizar o estado de um alojamento
    public boolean updateEstado(int id, EstadoAlojamento novoEstado) throws SQLException{
        String SQL = "UPDATE alojamento SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pStatement = conn.prepareStatement(SQL)) {

            pStatement.setString(1, novoEstado.name());
            pStatement.setInt(2, id);

            int affectedRows = pStatement.executeUpdate();
            return affectedRows > 0;
        }
    }
    // M que nos permite encontrar candidato pelo id
    public Alojamento findById(int id) throws SQLException {
        String SQL = "SELECT * FROM alojamento WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAlojamento(rs);
            }
            return null;
        }
    }
    // M que nos permite encontrar todos candidatos
    public List<Alojamento> findAll() throws SQLException {
        List<Alojamento> lista = new ArrayList<>();
        String SQL = "SELECT * FROM alojamento ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToAlojamento(rs));
            }
        }
        return lista;
    }

}
