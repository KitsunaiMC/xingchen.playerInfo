package xingchen.xingchenPlayerInfo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlayerInfoCommand implements CommandExecutor {
    private final DatabaseManager dbManager;

    public PlayerInfoCommand(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    private String formatDuration(long seconds) {
        long hrs = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§c用法: /playerinfo <玩家名称>");
            return true;
        }

        String name = args[0];
        DatabaseManager.PlayerData data = dbManager.getPlayerDataByName(name);

        if (data != null) {
            sender.sendMessage("§6玩家信息:");
            sender.sendMessage("§eUUID: §f" + data.uuid);
            sender.sendMessage("§e名称: §f" + data.name);
            sender.sendMessage("§e首次登录: §f" + data.firstJoin);
            sender.sendMessage("§e上次登录: §f" + data.lastJoin);
            sender.sendMessage("§e登录次数: §f" + data.loginCount);
            sender.sendMessage("§e总在线时间: §f" + formatDuration(data.totalOnlineTime));
            sender.sendMessage("§e最后登录IP: §f" + data.lastIp);
        } else {
            sender.sendMessage("§c未找到名为 " + name + " 的玩家记录");
        }
        return true;
    }
}