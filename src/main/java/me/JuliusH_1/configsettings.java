package me.JuliusH_1;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class configsettings {
    private final JavaPlugin plugin;
    private long commandCooldown;
    private long cmdsignCooldown;
    private String pluginPrefix;
    private String language;

    public configsettings(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        commandCooldown = parseCooldown(config.getString("settings.cmd_cooldown", "1s"));
        cmdsignCooldown = parseCooldown(config.getString("settings.cmdsign_cooldown", "10s"));
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("settings.plugin_prefix")));
        language = config.getString("settings.language", "EN").toLowerCase();
        logConfigSettings();
    }

    public void reload(FileConfiguration config) {
        commandCooldown = parseCooldown(config.getString("settings.cmd_cooldown", "1s"));
        cmdsignCooldown = parseCooldown(config.getString("settings.cmdsign_cooldown", "10s"));
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("settings.plugin_prefix")));
        language = config.getString("settings.language", "EN").toLowerCase();
        logConfigSettings();
    }

    private void logConfigSettings() {
        plugin.getLogger().info("Config Settings:");
        plugin.getLogger().info("Command Cooldown: " + commandCooldown);
        plugin.getLogger().info("Cmdsign Cooldown: " + cmdsignCooldown);
        plugin.getLogger().info("Plugin Prefix: " + pluginPrefix);
        plugin.getLogger().info("Language: " + language);
    }

    private long parseCooldown(String cooldownString) {
        long multiplier = 1000; // default to seconds
        if (cooldownString.endsWith("m")) {
            multiplier = 60000; // minutes
            cooldownString = cooldownString.substring(0, cooldownString.length() - 1);
        } else if (cooldownString.endsWith("s")) {
            cooldownString = cooldownString.substring(0, cooldownString.length() - 1);
        }
        return Long.parseLong(cooldownString) * multiplier;
    }

    public long getCommandCooldown() {
        return commandCooldown;
    }

    public long getCmdsignCooldown() {
        return cmdsignCooldown;
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public String getLanguage() {
        return language;
    }
}