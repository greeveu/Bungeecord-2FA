package eu.greev.twofa.utils;

import eu.greev.twofa.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MySQLMethodes {

    private MySQLMethodes() {
        throw new IllegalStateException("Utility class");
    }

    private static final MySQL mySQL = Main.getInstance().getMySQL();

    public static void createTable() {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `mc_proxy`.`2fa_players`(`uuid` VARCHAR(64) NOT NULL, `secret` VARCHAR(16) NOT NULL, `last_ip` VARCHAR(64) NOT NULL, `status` VARCHAR(16) NOT NULL, PRIMARY KEY (`uuid`)) ENGINE = InnoDB;")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasRecord(String uuid) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("SELECT 1 FROM 2fa_players WHERE uuid = ?")) {
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Optional<String> getSecret(String uuid) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("SELECT `secret` FROM 2fa_players WHERE uuid = ?")) {
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("secret"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static TwoFactorState getState(String uuid) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("SELECT `status` FROM 2fa_players WHERE uuid = ?")) {
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return TwoFactorState.valueOf(rs.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Optional<String> getLastIP(String uuid) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("SELECT `last_ip` FROM 2fa_players WHERE uuid = ?")) {
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("last_ip"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static void addNewPlayer(String uuid, String secret, String ip, TwoFactorState status) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("INSERT INTO `2fa_players`(`uuid`, `secret`, `last_ip`, `status`) VALUES (?,?,?,?)")) {
            ps.setString(1, uuid);
            ps.setString(2, secret);
            ps.setString(3, ip);
            ps.setString(4, String.valueOf(status));

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removePlayer(String uuid) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("DELETE FROM `2fa_players` WHERE `uuid` = ?")) {
            ps.setString(1, uuid);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setIP(String uuid, String ip) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("UPDATE `2fa_players` SET `last_ip` = ? WHERE `uuid` = ?")) {
            ps.setString(1, ip);
            ps.setString(2, uuid);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setState(String uuid, TwoFactorState state) {
        try (PreparedStatement ps = mySQL.getConnection().prepareStatement("UPDATE `2fa_players` SET `status` = ? WHERE `uuid` = ?")) {
            ps.setString(1, String.valueOf(state));
            ps.setString(2, uuid);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
