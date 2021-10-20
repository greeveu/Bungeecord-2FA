package eu.greev.twofa.utils;

import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.List;

@RequiredArgsConstructor
public class MySQL {
    // Proporties
    private final String hostname;
    private final String port;
    private final String username;
    private final String password;
    private final String database;

    private java.sql.Connection connection;

    // Functions
    public void connect() {
        String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;
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

    public ResultSet preparedStatement(String query, List<Object> values) throws SQLException {
        if (!this.isConnected())
            this.connect();

        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            for (int i = 1; i <= values.size(); i++) {
                ps.setObject(i, values.get(i - 1));
            }

            ps.execute();

            return ps.getResultSet();
        }
    }

    public void preparedStatementUpdate(String query, List<Object> values) throws SQLException {
        if (!this.isConnected())
            this.connect();

        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            for (int i = 1; i <= values.size(); i++) {
                ps.setObject(i, values.get(i - 1));
            }
            ps.executeUpdate();
        }
    }

    public ResultSet query(String query) throws SQLException {
        if (!this.isConnected())
            this.connect();

        try (Statement sm = this.connection.createStatement()) {
            return sm.executeQuery(query);
        }
    }

    public int updateQuery(String query) throws SQLException {
        if (this.isConnected())
            this.connect();

        try (Statement sm = this.connection.createStatement()) {
            return sm.executeUpdate(query);
        }
    }

    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

}
