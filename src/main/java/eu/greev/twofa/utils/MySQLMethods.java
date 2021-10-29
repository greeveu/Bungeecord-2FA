package eu.greev.twofa.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MySQLMethods {
    private final MySQL mySQL;

    public MySQLMethods(MySQL mySQL) {
        this.mySQL = mySQL;
    }

    public void createTable() {
        try {
            this.mySQL.updateQuery("CREATE TABLE IF NOT EXISTS `mc_proxy`.`2fa_players` ( `uuid` VARCHAR(64) NOT NULL , `secret` VARCHAR(16) NOT NULL , `last_ip` VARCHAR(50) NOT NULL , PRIMARY KEY (`uuid`)) ENGINE = InnoDB;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasRecord(String uuid) {
        try {
            String sql = "SELECT 1 FROM 2fa_players WHERE uuid = ?";
            ArrayList<Object> list = new ArrayList<>();
            list.add(uuid);
            ResultSet rs = this.mySQL.preparedStatement(sql, list);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getSecret(String uuid) {
        try {
            String sql = "SELECT `secret` FROM 2fa_players WHERE uuid = ?";
            ArrayList<Object> list = new ArrayList<>();
            list.add(uuid);
            ResultSet rs = this.mySQL.preparedStatement(sql, list);
            if (rs.next()) {
                return rs.getString("secret");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "none";
    }

    public String getLastIP(String uuid) {
        try {
            String sql = "SELECT `last_ip` FROM 2fa_players WHERE uuid = ?";
            ArrayList<Object> list = new ArrayList<>();
            list.add(uuid);
            ResultSet rs = this.mySQL.preparedStatement(sql, list);
            if (rs.next()) {
                return rs.getString("last_ip");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "none";
    }

    public void addNewPlayer(String uuid, String secret, String ip) {
        try {
            String sql = "INSERT INTO `2fa_players`(`uuid`, `secret`, `last_ip`) VALUES (?,?,?)";
            ArrayList<Object> list = new ArrayList<>();
            list.add(uuid);
            list.add(secret);
            list.add(ip);
            this.mySQL.preparedStatementUpdate(sql, list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePlayer(String uuid) {
        try {
            this.mySQL.updateQuery("DELETE FROM `2fa_players` WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setIP(String uuid, String ip) {
        try {
            this.mySQL.updateQuery("UPDATE `2fa_players` SET `last_ip` = '" + ip + "' WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSecret(String uuid, String secret) {
        try {
            this.mySQL.updateQuery("UPDATE `2fa_players` SET `secret` = '" + secret + "' WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
