package eu.greev.twofa.utils;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MySQL {
    // Proporties
    private final String hostname;
    private final String port;
    private final String username;
    private final String password;
    private final String database;

    private Connection connection;

    // Functions
    public void connect() {
        String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;
        System.out.println(url);
        try {
            this.connection = DriverManager.getConnection(url, this.username, this.password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.connection = null;
        }
    }

    public void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.connection = null;
    }

    public Connection getConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                connect();
                return connection;
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
