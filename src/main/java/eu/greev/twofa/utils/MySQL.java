package eu.greev.twofa.utils;

import java.sql.*;
import java.util.List;

public class MySQL {
    // Proporties
    private String hostname;
    private String port;
    private String username;
    private String password;
    private String database;

    private java.sql.Connection connection;

    // Constructors
    public MySQL() {
        this.hostname = "localhost";
        this.port = "3306";
        this.username = "";
        this.password = "";
    }

    public MySQL(String username, String password, String database) {
        this.hostname = "localhost";
        this.port = "3306";
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public MySQL(String hostname, String username, String password, String database) {
        this.hostname = hostname;
        this.port = "3306";
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public MySQL(String hostname, String port, String username, String password, String database) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    // Functions
    public void connect() {
        String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database;
        try {
            this.connection = DriverManager.getConnection(url, this.username, this.password);
            System.out.println("Connected to Database");
        } catch (SQLException ex) {
            ex.printStackTrace();
            this.connection = null;
        }
    }

    public void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            this.connection = null;
        }
        this.connection = null;
    }

    public ResultSet preparedStatement(String query, List<Object> values) throws SQLException {
        if (!this.isConnected())
            this.connect();

        PreparedStatement ps = this.connection.prepareStatement(query);
        for (int i = 1; i <= values.size(); i++) {
            ps.setObject(i, values.get(i - 1));
        }
        ps.execute();

        return ps.getResultSet();
    }

    public void preparedStatementUpdate(String query, List<Object> values) throws SQLException {
        if (!this.isConnected())
            this.connect();

        PreparedStatement ps = this.connection.prepareStatement(query);
        for (int i = 1; i <= values.size(); i++) {
            ps.setObject(i, values.get(i - 1));
        }
        ps.executeUpdate();
    }

    public ResultSet query(String query) throws SQLException {
        if (!this.isConnected())
            this.connect();

        Statement sm = this.connection.createStatement();

        return sm.executeQuery(query);
    }

    public int updateQuery(String query) throws SQLException {
        if (this.isConnected())
            this.connect();

        Statement sm = this.connection.createStatement();

        return sm.executeUpdate(query);
    }

    // Getters and setters
    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public java.sql.Connection getConnection() {
        return this.connection;
    }

    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

}
