package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/alojamento_db";
    private static final String USER = "alojamento_admin";
    private static final String PASSWORD = "12345";

    private DatabaseConnection() { }

    /**
     * Cria e devolve uma nova conexão ao PostgreSQL.
     * @return uma conexão JDBC ativa.
     * @throws SQLException se ocorrer erro de ligação.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Carrega explicitamente o driver (opcional, mas seguro)
            Class.forName("org.postgresql.Driver");

            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão estabelecida com sucesso ao PostgreSQL.");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL não encontrado: " + e.getMessage());
            throw new SQLException("Driver não encontrado", e);
        } catch (SQLException e) {
            System.err.println("Falha ao conectar ao PostgreSQL: " + e.getMessage());
            throw e;
        }
    }

    /** Fecha uma conexão específica (em vez de uma única estática global). */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexão encerrada com sucesso.");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
