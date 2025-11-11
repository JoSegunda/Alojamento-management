package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/alojamento_db";
    private static final String USER = "alojamento_admin"
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


}
