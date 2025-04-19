package xingchen.xingchenPlayerInfo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerQuitListener implements Listener {
    private final DatabaseManager dbManager;
    private static final Logger logger = LoggerFactory.getLogger(PlayerQuitListener.class);

    public PlayerQuitListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName();
        Timestamp now = Timestamp.from(Instant.now());

        Timestamp loginTime = PlayerJoinListener.getLoginTime(name);

        if (loginTime != null) {
            long onlineTime = (now.getTime() - loginTime.getTime()) / 1000;
            DatabaseManager.PlayerData existingData = dbManager.getPlayerDataByName(name);
            if (existingData != null) {
                long updatedTotal = existingData.totalOnlineTime + onlineTime;
                logger.debug("玩家 {} 登出，本次在线: {} 秒，总时长更新为: {} 秒", name, onlineTime, updatedTotal);
                dbManager.updatePlayerOnlineTime(name, updatedTotal);
            } else {
                logger.warn("玩家 {} 的数据不存在，无法更新在线时间", name);
            }
        } else {
            logger.warn("玩家 {} 的登录时间未记录，无法计算在线时间", name);
        }
    }
}
