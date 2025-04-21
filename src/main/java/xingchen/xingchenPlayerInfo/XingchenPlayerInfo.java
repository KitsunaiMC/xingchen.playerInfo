package xingchen.xingchenPlayerInfo;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XingchenPlayerInfo extends JavaPlugin {
    public static JavaPlugin instance;
    private DatabaseManager dbManager;
    private static final Logger logger = LoggerFactory.getLogger(XingchenPlayerInfo.class);
    private int saveTaskId;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        dbManager = new DatabaseManager(getConfig(), getDataFolder());
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(dbManager), this);
        Objects.requireNonNull(getServer().getPluginManager()).registerEvents(new PlayerQuitListener(dbManager), this);
        Objects.requireNonNull(getCommand("playerinfo")).setExecutor(new PlayerInfoCommand(dbManager));
        getLogger().info("XingchenPlayerInfo插件加载成功");

        int saveInterval = getConfig().getInt("save-interval", 5); // 默认 5 分钟
        new BukkitRunnable() {
            @Override
            public void run() {
                SaveAllPlayersOnlineTime();
            }
        }.runTaskTimerAsynchronously(this, 60 * 20L, saveInterval * 60 * 20L); // 60秒后开始
    }


    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(saveTaskId);
        SaveAllPlayersOnlineTime();
        getLogger().info("XingchenPlayerInfo插件卸载成功");
    }

    private void SaveAllPlayersOnlineTime() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            Timestamp now = Timestamp.from(Instant.now());

            Timestamp loginTime = PlayerJoinListener.getLoginTime(name);
            if (loginTime != null) {
                updateOnlineTime(name, now, loginTime);
                PlayerJoinListener.setLoginTime(name, now);
                getLogger().info("保存玩家在线时间成功");
            } else {
                logger.warn("玩家 {} 的登录时间未记录，无法计算在线时间", name);
            }
        }
    }

    private void updateOnlineTime(String name, Timestamp now, Timestamp loginTime) {
        // 计算玩家本次在线时间（秒）
        if (loginTime == null) {
            logger.error("玩家 {} 的登录时间为空，无法计算在线时间", name);
            return;
        }
        long onlineTime = (now.getTime() - loginTime.getTime()) / 1000;
        logger.debug("玩家 {} 的登录时间: {}, 当前时间: {}, 计算出的在线时间: {} 秒", name, loginTime, now, onlineTime);
        DatabaseManager.PlayerData existingData = dbManager.getPlayerDataByName(name);
        if (existingData != null) {
            long updatedTotal = existingData.totalOnlineTime + onlineTime;
            logger.debug("玩家 {} 本次在线: {} 秒，总时长更新为: {} 秒", name, onlineTime, updatedTotal);
            dbManager.updatePlayerOnlineTime(name, updatedTotal);
        } else {
            logger.warn("玩家 {} 的数据不存在，无法更新在线时间", name);
        }
    }
}
