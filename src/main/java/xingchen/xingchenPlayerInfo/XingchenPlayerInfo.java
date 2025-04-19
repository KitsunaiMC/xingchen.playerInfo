package xingchen.xingchenPlayerInfo;

import org.bukkit.plugin.java.JavaPlugin;

public final class XingchenPlayerInfo extends JavaPlugin {
    public static JavaPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        DatabaseManager dbManager = new DatabaseManager(getConfig(), getDataFolder());
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(dbManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(dbManager), this);
        getCommand("playerinfo").setExecutor(new PlayerInfoCommand(dbManager));
        getLogger().info("XingchenPlayerInfo插件加载成功");
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
