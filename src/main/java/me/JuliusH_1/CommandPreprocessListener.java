package me.JuliusH_1;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CommandPreprocessListener implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration commandsConfig;

    public CommandPreprocessListener(JavaPlugin plugin) {
        this.plugin = plugin;
        File commandsFile = new File(plugin.getDataFolder(), "commands.yml");
        this.commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String command = message.split(" ")[0].substring(1).toLowerCase();

        if (!commandsConfig.getBoolean("Commands." + command, true)) {
            event.getPlayer().sendMessage(ChatColor.RED + "This command is disabled.");
            event.setCancelled(true);
        }
    }
}