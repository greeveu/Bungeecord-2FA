package eu.greev.twofa.utils;

import eu.greev.twofa.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class MySQLMethodes {

    static MySQL mySQL = Main.getInstance().mySQL;

    public static void createTable() {
        try {
            mySQL.updateQuery("CREATE TABLE IF NOT EXISTS `mc_proxy`.`2fa_players` ( `uuid` VARCHAR(64) NOT NULL , `secret` VARCHAR(16) NOT NULL , `last_ip` VARCHAR(50) NOT NULL , PRIMARY KEY (`uuid`)) ENGINE = InnoDB;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasRecord(String uuid) {
        try {
            String sql = "SELECT 1 FROM 2fa_players WHERE uuid = ?";
            ResultSet rs = mySQL.preparedStatement(sql, Collections.singletonList(uuid));
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Optional<String> getSecret(String uuid) {
        try {
            String sql = "SELECT `secret` FROM 2fa_players WHERE uuid = ?";
            ResultSet rs = mySQL.preparedStatement(sql, Collections.singletonList(uuid));
            if (rs.next()) {
                return Optional.of(rs.getString("secret"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<String> getLastIP(String uuid) {
        try {
            String sql = "SELECT `last_ip` FROM 2fa_players WHERE uuid = ?";
            ResultSet rs = mySQL.preparedStatement(sql, Collections.singletonList(uuid));
            if (rs.next()) {
                return Optional.of(rs.getString("last_ip"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static void addNewPlayer(String uuid, String secret, String ip) {
        try {
            String sql = "INSERT INTO `2fa_players`(`uuid`, `secret`, `last_ip`) VALUES (?,?,?)";
            ArrayList<Object> list = new ArrayList<>();
            list.add(uuid);
            list.add(secret);
            list.add(ip);
            mySQL.preparedStatementUpdate(sql, list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removePlayer(String uuid) {
        try {
            mySQL.updateQuery("DELETE FROM `2fa_players` WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setIP(String uuid, String ip) {
        try {
            mySQL.updateQuery("UPDATE `2fa_players` SET `last_ip` = '" + ip + "' WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setSecret(String uuid, String secret) {
        try {
            mySQL.updateQuery("UPDATE `2fa_players` SET `secret` = '" + secret + "' WHERE `uuid` = '" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
