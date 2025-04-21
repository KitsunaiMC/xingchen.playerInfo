package xingchen.xingchenPlayerInfo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerJoinListener implements Listener {
    private final DatabaseManager dbManager;
    private static final Map<String, Timestamp> loginTimes = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PlayerJoinListener.class);

    public PlayerJoinListener(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        String name = event.getPlayer().getName();
        String ip = Objects.requireNonNull(event.getPlayer().getAddress()).getHostName();
        Timestamp now = Timestamp.from(Instant.now());
        // 记录玩家登录时间
        loginTimes.put(name, now);
        // 检查玩家是否已有记录
        DatabaseManager.PlayerData existingData;
        try {
            existingData = dbManager.getPlayerDataByName(name);
        } catch (Exception e) {
            logger.error("获取玩家数据失败: " + name, e);
            return;
        }
        int loginCount = 1; // 修改: 默认值为 1
        Timestamp firstJoin = now;
        if (existingData != null) {
            loginCount = existingData.loginCount + 1; // 修改: 递增登录次数
            firstJoin = existingData.firstJoin;
        }

        // 记录或更新玩家数据
        try {
            dbManager.recordPlayerLogin(uuid, name, firstJoin, now, loginCount, ip); // 修改: 传递正确的 loginCount
        } catch (Exception e) {
            logger.error("记录玩家登录信息失败: " + name, e);
            return;
        }

        // 调试日志：打印玩家登录信息
        logger.debug("玩家 {} 登录，lastJoin 更新为: {}", name, now);
    }

    public static Timestamp getLoginTime(String name) {
        return loginTimes.get(name);
    }

    public static void removeLoginTime(String name) {
        loginTimes.remove(name);
    }
}