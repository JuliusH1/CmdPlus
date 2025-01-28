package me.JuliusH_1;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class CmdBlocker {
    private List<String> blockedCommands;

    public CmdBlocker(JavaPlugin plugin) {
        loadBlockedCommands(plugin);
    }

    private void loadBlockedCommands(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "blockedcmds.yml");
        if (!file.exists()) {
            plugin.saveResource("blockedcmds.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        blockedCommands = config.getStringList("blocked-commands");
    }

    public List<String> getBlockedCommands() {
        return blockedCommands;
    }
}