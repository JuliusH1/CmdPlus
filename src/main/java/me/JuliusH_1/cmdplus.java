package me.JuliusH_1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class cmdplus extends JavaPlugin implements TabCompleter {

    private FileConfiguration config;
    private FileConfiguration messages;
    private configsettings configSettings;
    private cmdalias cmdAliasHandler;
    private cmdsign cmdSignHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        configSettings = new configsettings(this);
        cmdAliasHandler = new cmdalias(this);
        cmdSignHandler = new cmdsign(this);
        saveDefaultLangFiles();
        loadMessages();
        getCommand("cmdplus").setTabCompleter(this);


        // Register the trash command
        getCommand("trash").setExecutor(new TrashCommand(this));
        getCommand("bin").setExecutor(new TrashCommand(this));

        // Register the private chest command
        getCommand("privatechest").setExecutor(new PrivateChestCommand(this));

        // Translate color codes in the plugin prefix
        String translatedPrefix = ChatColor.translateAlternateColorCodes('&', configSettings.getPluginPrefix());
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "===============================");
        Bukkit.getConsoleSender().sendMessage(translatedPrefix + "CmdPlus is now enabled!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "===============================");
    }

    private void saveDefaultLangFiles() {
        saveResource("lang/messages_en.yml", false);
        saveResource("lang/messages_ja.yml", false);
        saveResource("lang/messages_ru.yml", false);
        saveResource("lang/messages_de.yml", false);
        saveResource("lang/messages_es.yml", false);
        saveResource("lang/messages_fr.yml", false);
        saveResource("lang/messages_it.yml", false);
        saveResource("lang/messages_nl.yml", false);
        saveResource("lang/messages_pt.yml", false);
        saveResource("lang/messages_zh.yml", false);
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "CmdPlus is now disabled D:");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============================");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        configSettings.reloadConfig();
        cmdAliasHandler.reloadAliases();
        cmdSignHandler.reloadSigns();
        loadMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cmdplus")) {
            if (args.length < 2 || !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessage("cmdplus_usage")));
                return true;
            }

            long startTime = System.nanoTime();

            if (args[1].equalsIgnoreCase("all")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading CmdPlus..."));
                reloadPluginConfig();
                cmdAliasHandler.reloadAliases();
                cmdSignHandler.reloadSigns();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "CmdPlus &areloaded!"));
            } else if (args[1].equalsIgnoreCase("alias")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading aliases..."));
                cmdAliasHandler.reloadAliases();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Aliases &areloaded!"));
            } else if (args[1].equalsIgnoreCase("sign")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading signs..."));
                cmdSignHandler.reloadSigns();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Signs &areloaded!"));
            } else if (args[1].equalsIgnoreCase("config")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading config..."));
                reloadPluginConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Config &areloaded!"));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Unknown reload target: " + args[1]));
                return true;
            }

            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            double seconds = duration / 1_000_000_000.0;
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("Reload took %.3f seconds.", seconds)));

            return true;
        }
        return false;
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(key, "Message not found: " + key));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cmdplus")) {
            if (args.length == 1) {
                return Arrays.asList("reload");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
                return Arrays.asList("all", "alias", "sign", "config");
            }
        }
        return Collections.emptyList();
    }
}