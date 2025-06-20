package xingchen.xingchenPlayerInfo;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;

public class DatabaseManager {
    private final String url;

    public DatabaseManager(FileConfiguration config, File dataFolder) {
        this.url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + File.separator +
                config.getString("database.file", "playerdata.db");
        initDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initDatabase() {
        String firstJoin = "first_join";
        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_logins (" +
                "name TEXT PRIMARY KEY," +
                "uuid TEXT NOT NULL," +
                firstJoin + " DATETIME NOT NULL," +
                "last_join DATETIME NOT NULL," +
                "login_count INTEGER NOT NULL," +
                "total_online_time INTEGER NOT NULL DEFAULT 0," +
                "last_ip TEXT" +
                ")";
        String createIndexSQL = "CREATE INDEX IF NOT EXISTS idx_name ON player_logins (name)";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            stmt.execute(createIndexSQL);
        } catch (SQLException e) {
            XingchenPlayerInfo.instance.getLogger().warning("创建数据库表失败: ");
        }
    }

    public void recordPlayerLogin(String uuid, String name, Timestamp firstJoin, Timestamp lastJoin, int loginCount, String ip) {
        String upsertSQL = "INSERT INTO player_logins (name, uuid, first_join, last_join, login_count, last_ip) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(name) DO UPDATE SET " +
                "uuid = excluded.uuid, " +
                "last_join = excluded.last_join, " +
                "login_count = COALESCE(player_logins.login_count, 0) + 1, " +
                "last_ip = excluded.last_ip";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(upsertSQL)) {
            stmt.setString(1, name);
            stmt.setString(2, uuid);
            stmt.setTimestamp(3, firstJoin);
            stmt.setTimestamp(4, lastJoin);
            stmt.setInt(5, loginCount);
            stmt.setString(6, ip);
            stmt.executeUpdate();
        } catch (SQLException e) {
            XingchenPlayerInfo.instance.getLogger().warning("记录玩家登录信息失败: ");
        }
    }

    public static class PlayerData {
        public final String uuid;
        public final String name;
        public final Timestamp firstJoin;
        public final Timestamp lastJoin;
        public final int loginCount;
        public final long totalOnlineTime;
        public final String lastIp;

        public PlayerData(String uuid, String name, Timestamp firstJoin, Timestamp lastJoin, int loginCount, long totalOnlineTime, String lastIp) {
            this.uuid = uuid;
            this.name = name;
            this.firstJoin = firstJoin;
            this.lastJoin = lastJoin;
            this.loginCount = loginCount;
            this.totalOnlineTime = totalOnlineTime;
            this.lastIp = lastIp;
        }
    }

    public PlayerData getPlayerDataByName(String name) {
        String selectSQL = "SELECT uuid, name, first_join, last_join, login_count, total_online_time, last_ip FROM player_logins WHERE name = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PlayerData(
                        rs.getString("uuid"),
                        rs.getString("name"),
                        rs.getTimestamp("first_join"),
                        rs.getTimestamp("last_join"),
                        rs.getInt("login_count"),
                        rs.getLong("total_online_time"),
                        rs.getString("last_ip")
                );
            }
        } catch (SQLException e) {
            XingchenPlayerInfo.instance.getLogger().warning("查询玩家数据失败: ");
        }
        return null;
    }

    public void updatePlayerOnlineTime(String name, long additionalOnlineTime) {
        String updateSQL = "UPDATE player_logins SET total_online_time = ? WHERE name = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setLong(1, additionalOnlineTime);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            XingchenPlayerInfo.instance.getLogger().warning("更新玩家在线时间失败: ");
        }
    }
}
