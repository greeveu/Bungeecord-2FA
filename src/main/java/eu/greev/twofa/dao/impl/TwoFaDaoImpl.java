package eu.greev.twofa.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import eu.greev.twofa.dao.TwoFaDao;
import eu.greev.twofa.entities.UserData;
import eu.greev.twofa.utils.TwoFactorState;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TwoFaDaoImpl implements TwoFaDao {

    private HikariDataSource hikariDataSource;

    public TwoFaDaoImpl(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public void createTable() {
        try (PreparedStatement ps = hikariDataSource.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS `2fa_players`\n" +
                "(\n" +
                "    `uuid`    VARCHAR(64) NOT NULL,\n" +
                "    `secret`  VARCHAR(16) NOT NULL,\n" +
                "    `yubiotp` VARCHAR(16) NOT NULL,\n" +
                "    `last_ip` VARCHAR(64) NOT NULL,\n" +
                "    `status`  VARCHAR(16) NOT NULL,\n" +
                "    PRIMARY KEY (`uuid`)\n" +
                ") ENGINE = InnoDB;")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserData loadUserData(String uuid) {
        try (PreparedStatement ps = hikariDataSource.getConnection().prepareStatement("SELECT `secret`, `last_ip`, `status`, `yubiotp`\n" +
                "FROM `2fa_players`\n" +
                "WHERE uuid = ?")) {
            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserData userData = new UserData();
                userData.setSecret(rs.getString("secret"));
                userData.setLastIpHash(rs.getString("last_ip"));
                userData.setStatus(TwoFactorState.valueOf(rs.getString("status")));
                userData.setYubiOtp(rs.getString("yubiotp"));
                return userData;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveUserData(String uuid, UserData user) {
        try (PreparedStatement preparedStatement = hikariDataSource.getConnection().prepareStatement("INSERT INTO `2fa_players`(`uuid`, `secret`, `last_ip`, `status`, `yubiotp`)\n" +
                "VALUES (?, ?, ?, ?, ?)\n" +
                "ON DUPLICATE KEY UPDATE `secret`  = values(secret),\n" +
                "                        `last_ip` = values(`last_ip`),\n" +
                "                        `status`  = values(`status`),\n" +
                "                        `yubiotp` = values(`yubiotp`);")) {
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, user.getSecret());
            preparedStatement.setString(3, user.getLastIpHash());
            preparedStatement.setString(4, user.getStatus().toString());
            preparedStatement.setString(5, user.getYubiOtp());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUser(String uuid) {
        try (PreparedStatement ps = hikariDataSource.getConnection().prepareStatement("DELETE FROM `2fa_players` WHERE `uuid` = ?")) {
            ps.setString(1, uuid);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
