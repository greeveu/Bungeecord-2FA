package eu.greev.twofa.utils;

import com.google.common.hash.Hashing;
import eu.greev.twofa.Main;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class MySQLMethodes {

    private MySQLMethodes() {
        throw new IllegalStateException("Utility class");
    }

    private static final MySQL mySQL = Main.getInstance().getMySQL();

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
        String hashedIp = hashIp(ip);
        try {
            String sql = "INSERT INTO `2fa_players`(`uuid`, `secret`, `last_ip`) VALUES (?,?,?)";
            mySQL.preparedStatementUpdate(sql, Arrays.asList(uuid, secret, hashedIp));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removePlayer(String uuid) {
        try {
            String sql = "DELETE FROM `2fa_players` WHERE `uuid` = ?";
            mySQL.preparedStatementUpdate(sql, Collections.singletonList(uuid));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setIP(String uuid, String ip) {
        String hashedIp = hashIp(ip);
        try {
            String sql = "UPDATE `2fa_players` SET `last_ip` = ? WHERE `uuid` = ?";
            mySQL.preparedStatementUpdate(sql, Arrays.asList(hashedIp, uuid));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setSecret(String uuid, String secret) {
        try {
            String sql = "UPDATE `2fa_players` SET `secret` = ? WHERE `uuid` = ?";
            mySQL.preparedStatementUpdate(sql, Arrays.asList(secret, uuid));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String hashIp(String ip) {
        return Hashing.sha256().hashString(ip, StandardCharsets.UTF_8).toString();
    }
}
