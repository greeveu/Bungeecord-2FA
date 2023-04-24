package eu.greev.twofa.dao.impl;

import com.zaxxer.hikari.HikariDataSource;
import eu.greev.twofa.dao.TwoFaDao;
import eu.greev.twofa.entities.UserData;
import eu.greev.twofa.entities.YubicoOtp;
import eu.greev.twofa.utils.TwoFactorState;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

@RequiredArgsConstructor
public class TwoFaDaoImpl implements TwoFaDao {
    private final HikariDataSource hikariDataSource;

    @Override
    public void createTables() {
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `2fa_players`\n" +
                "(\n" +
                "    `uuid`    varchar(64) NOT NULL,\n" +
                "    `secret`  varchar(16) NOT NULL,\n" +
                "    `last_ip` varchar(64) NOT NULL,\n" +
                "    `status`  varchar(16) NOT NULL,\n" +
                "    PRIMARY KEY (`uuid`)\n" +
                ") ENGINE = InnoDB\n" +
                "  DEFAULT CHARSET = utf8mb4;")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `2fa_yubikey`\n" +
                     "(\n" +
                     "    `uuid`      varchar(36) NOT NULL,\n" +
                     "    `public_id` varchar(16) NOT NULL,\n" +
                     "    `name`      varchar(24) NOT NULL,\n" +
                     "    PRIMARY KEY (`uuid`, `public_" +
                     "id`)\n" +
                     ") ENGINE = InnoDB\n" +
                     "  DEFAULT CHARSET = utf8mb4;")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserData loadUserData(String uuid) {
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT `secret`, `last_ip`, `status`, `public_id`, `name`\n" +
                     "FROM `2fa_players`\n" +
                     "         LEFT JOIN `2fa_yubikey` ON `2fa_players`.uuid = `2fa_yubikey`.uuid\n" +
                     "WHERE `2fa_players`.uuid = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ps.setString(1, uuid);

            ResultSet resultSet = ps.executeQuery();

            UserData userData = new UserData();

            if (resultSet.next()) {
                userData.setSecret(resultSet.getString("secret"));
                userData.setLastIpHash(resultSet.getString("last_ip"));
                userData.setStatus(TwoFactorState.valueOf(resultSet.getString("status")));
            } else {
                return null;
            }

            resultSet.beforeFirst();

            while (resultSet.next()) {
                //Should the public_id ever be null (meaning it's not set / the user has no YubiKeys active) just return and give out the user without YubiKeys
                if (resultSet.getString("public_id") == null) {
                    return userData;
                }
                userData.getYubiOtp().add(new YubicoOtp(resultSet.getString("name"), resultSet.getString("public_id")));
            }

            return userData;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveUserData(String uuid, UserData user) {
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `2fa_players`(`uuid`, `secret`, `last_ip`, `status`)\n" +
                "VALUES (?, ?, ?, ?)\n" +
                "ON DUPLICATE KEY UPDATE `secret`  = values(secret),\n" +
                "                        `last_ip` = values(`last_ip`),\n" +
                "                        `status`  = values(`status`);")) {
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, user.getSecret());
            preparedStatement.setString(3, user.getLastIpHash());
            preparedStatement.setString(4, user.getStatus().toString());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        saveYubikeys(uuid, user.getYubiOtp());
    }

    @Override
    public void deleteUser(String uuid) {
        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM `2fa_players` WHERE `uuid` = ?")) {
            ps.setString(1, uuid);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveYubikeys(String uuid, Set<YubicoOtp> yubicoOtps) {
        if (yubicoOtps.isEmpty()) {
            return;
        }

        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO `2fa_yubikey`(`uuid`, `public_id`, `name`)\n" +
                "VALUES (?, ?, ?)\n" +
                "ON DUPLICATE KEY UPDATE `uuid`  = values(uuid),\n" +
                "                        `public_id` = values(`public_id`),\n" +
                "                        `name`  = values(`name`);");
        ) {
            int i = 0;

            for (YubicoOtp entity : yubicoOtps) {
                statement.setString(1, uuid);
                statement.setString(2, entity.publicId());
                statement.setString(3, entity.name());

                statement.addBatch();
                i++;

                if (i % 100 == 0 || i == yubicoOtps.size()) {
                    statement.executeBatch();
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
