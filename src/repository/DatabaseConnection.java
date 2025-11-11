package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/alojamento_db";
    private static final String USER = "alojamento_admin";
    private static final String PASSWORD = "12345";

    private static Connection connection = null;

    private DatabaseConnection(){
        // Construtor privado para impedir instanciação externa
    }
    /**
     * Obtém a instância da conexão. Conecta-se se a conexão não estiver ativa.
     * @return Uma conexão ativa com o banco de dados.
     * @throws SQLException Se ocorrer um erro de conexão.
     */

    public static Connection getConnection() throws SQLException{
        if (connection == null || connection.isClosed()){
            try {
                // 1. Carregar o driver JDBC

                // 2. Estabelecer a conexão
                connection = DriverManager.getConnection(URL,USER,PASSWORD);
                System.out.println("Conexão estabelecida com sucesso.");
            }catch (SQLException e){
                System.err.println("Falha ao conectar ao PostgreSQL: "+ e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    // Fechar a conexão
    public static void closeConnection(){
        if (connection !=null){
            try {
                connection.close();
                connection = null;
                System.out.println("Conexão com postgres encerrada.");
            }catch (SQLException e){
                System.err.println("Erro ao fechar a conexão: "+ e.getMessage());
            }
        }
    }

}
