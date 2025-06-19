package xingchen.xingchenPlayerInfo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.time.Instant;



public class PlayerQuitListener implements Listener {
    private final DatabaseManager dbManager;

    public PlayerQuitListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName();
        Timestamp now = Timestamp.from(Instant.now());

        Timestamp loginTime = lastSaveTime.getLoginTime(name);

        if (loginTime == null) {
            XingchenPlayerInfo.instance.getLogger().warning("玩家 " + name + " 的登录时间未记录，无法计算在线时间");
            return;
        }
        updateOnlineTime(name, now, loginTime, dbManager);
        lastSaveTime.removeLoginTime(name);
    }

    public void updateOnlineTime(String name, Timestamp now, Timestamp loginTime, DatabaseManager dbManager) {
        if (loginTime == null) {
            XingchenPlayerInfo.instance.getLogger().warning("玩家"+name+"的登录时间为空，无法计算在线时间");
            return;
        }
        long onlineTime = (now.getTime() - loginTime.getTime()) / 1000;
        DatabaseManager.PlayerData existingData = dbManager.getPlayerDataByName(name);
        if (existingData != null) {
            long updatedTotal = existingData.totalOnlineTime + onlineTime;
            dbManager.updatePlayerOnlineTime(name, updatedTotal);
            lastSaveTime.removeLoginTime(name);
        } else {
            XingchenPlayerInfo.instance.getLogger().warning("玩家"+name+"的数据不存在，无法更新在线时间");
        }
    }
}