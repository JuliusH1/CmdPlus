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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class cmdplus extends JavaPlugin implements TabCompleter {

    private FileConfiguration config;
    private FileConfiguration messages;
    private ConfigSettings ConfigSettings;
    private cmdalias cmdAliasHandler;
    private cmdsign cmdSignHandler;
    private Map<String, Boolean> commandStatus = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        ConfigSettings = new ConfigSettings(this);
        cmdAliasHandler = new cmdalias(this);
        cmdSignHandler = new cmdsign(this);
        new CommandPreprocessListener(this);
        saveDefaultLangFiles();
        loadMessages();
        loadCommandStatus();
        registerCommands();
        getCommand("cmdplus").setTabCompleter(this);

        // Translate color codes in the plugin prefix
        String translatedPrefix = ChatColor.translateAlternateColorCodes('&', ConfigSettings.getPluginPrefix());
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
        String lang = config.getString("Language", "en");
        File messagesFile = new File(getDataFolder(), "lang/messages_" + lang + ".yml");
        if (!messagesFile.exists()) {
            saveResource("lang/messages_" + lang + ".yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void loadCommandStatus() {
        File commandsFile = new File(getDataFolder(), "commands.yml");
        if (!commandsFile.exists()) {
            saveResource("commands.yml", false);
        }
        FileConfiguration commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);
        for (String command : commandsConfig.getConfigurationSection("Commands").getKeys(false)) {
            commandStatus.put(command, commandsConfig.getBoolean("Commands." + command));
        }
    }

    private void registerCommands() {
        if (commandStatus.getOrDefault("trash", false)) {
            getCommand("trash").setExecutor(new TrashCommand(this));
        }
        if (commandStatus.getOrDefault("bin", false)) {
            getCommand("bin").setExecutor(new TrashCommand(this));
        }
        if (commandStatus.getOrDefault("privatechest", false)) {
            getCommand("privatechest").setExecutor(new PrivateChestCommand(this));
        }
        // Add more commands as needed
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
        ConfigSettings.reloadConfig();
        cmdAliasHandler.reloadAliases();
        cmdSignHandler.reloadSigns();
        loadMessages();
        loadCommandStatus();
        registerCommands();
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
                config = getConfig();
                ConfigSettings.reloadConfig();
                cmdAliasHandler.reloadAliases();
                cmdSignHandler.reloadSigns();
                loadCommandStatus();
                registerCommands();
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
                config = getConfig();
                ConfigSettings.reloadConfig();
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