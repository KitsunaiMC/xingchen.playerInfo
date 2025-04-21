package xingchen.xingchenPlayerInfo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

public class PlayerJoinListener implements Listener {
    private final DatabaseManager dbManager;

    public PlayerJoinListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        String name = event.getPlayer().getName();
        String ip = Objects.requireNonNull(event.getPlayer().getAddress()).getHostName();
        Timestamp now = Timestamp.from(Instant.now());
        lastSaveTime.setLoginTime(name,now);
        DatabaseManager.PlayerData existingData;
        try {
            existingData = dbManager.getPlayerDataByName(name);
        } catch (Exception e) {
            XingchenPlayerInfo.instance.getLogger().warning("获取"+name+"玩家数据失败:");
            return;
        }
        int loginCount = 1;
        Timestamp firstJoin = now;
        if (existingData != null) {
            loginCount = existingData.loginCount + 1;
            firstJoin = existingData.firstJoin;
        }
        try {
            dbManager.recordPlayerLogin(uuid, name, firstJoin, now, loginCount, ip);
        } catch (Exception e) {
            XingchenPlayerInfo.instance.getLogger().warning("记录玩家登录信息失败: ");
            return;
        }
        XingchenPlayerInfo.instance.getLogger().info("玩家"+name+"登录，lastJoin 更新为:"+now);
    }



}
