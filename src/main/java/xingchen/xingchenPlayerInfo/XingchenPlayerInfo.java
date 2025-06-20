package xingchen.xingchenPlayerInfo;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

import org.bukkit.scheduler.BukkitRunnable;
import xingchen.xingchenPlayerInfo.api.PlayerInfoAPI;

public final class XingchenPlayerInfo extends JavaPlugin implements PlayerInfoAPI {
    public static JavaPlugin instance;
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getServicesManager().register(
                PlayerInfoAPI.class,
                this,
                this,
                ServicePriority.Normal
        );
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
        SaveAllPlayersOnlineTime();
    }
    private void SaveAllPlayersOnlineTime() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String name = player.getName();
            Timestamp now = Timestamp.from(Instant.now());
            Timestamp loginTime = lastSaveTime.getLoginTime(name);

            if (loginTime != null) {
                long onlineTime = (now.getTime() - loginTime.getTime()) / 1000; // 计算本次在线时间
                DatabaseManager.PlayerData existingData = dbManager.getPlayerDataByName(name);

                if (existingData != null) {
                    long updatedTotal = existingData.totalOnlineTime + onlineTime; // 更新总在线时间
                    dbManager.updatePlayerOnlineTime(name, updatedTotal);
                    lastSaveTime.setLoginTime(name, now); // 更新登录时间为当前时间
                } else {
                    getLogger().warning("玩家 " + name + " 无法更新在线时间,数据库返回总时长为空!");
                }
            } else {
                getLogger().warning("玩家 " + name + " 的登录时间未记录，无法计算在线时间,loginTime为空!");
            }
        }
    }

    @Override
    public boolean isNewComer(Player player) {
        DatabaseManager.PlayerData data = dbManager.getPlayerDataByName(player.getName());
        return data.firstJoin.equals(data.lastJoin);
    }
}
