package me.JuliusH_1;

import me.JuliusH_1.chat.*;
import me.JuliusH_1.chat.listeners.ChatListener;
import me.JuliusH_1.chat.listeners.PlayerJoinListener;
import me.JuliusH_1.chat.listeners.PlayerLeaveListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.JuliusH_1.othercommands.vanish.VanishCommand;
import me.JuliusH_1.othercommands.vanish.VanishListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class cmdplus extends JavaPlugin implements TabCompleter {
    private FileConfiguration config;
    private BukkitAudiences adventure;
    private ConfigSettings configSettings;
    private final Map<String, Boolean> commandStatus = new HashMap<>();
    private cmdalias cmdAliasHandler;
    private cmdsign cmdSignHandler;
    private AnnouncementManager announcementManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        adventure = BukkitAudiences.create(this);

        ChatCommands chatCommands = new ChatCommands(this);
        VanishCommand vanishCommand = new VanishCommand(this);
        if (getCommand("ban") != null) {
            getCommand("ban").setExecutor(chatCommands);
        }
        if (getCommand("mute") != null) {
            getCommand("mute").setExecutor(chatCommands);
        }
        if (getCommand("kick") != null) {
            getCommand("kick").setExecutor(chatCommands);
        }
        if (getCommand("vanish") != null) {
            getCommand("vanish").setExecutor(vanishCommand);
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new PotionStackListener(getConfig()), this);
        getServer().getPluginManager().registerEvents(new VanishListener(vanishCommand), this);

        config = getConfig();
        configSettings = new ConfigSettings(this);
        cmdAliasHandler = new cmdalias(this);
        cmdSignHandler = new cmdsign(this);
        new CommandPreprocessListener(this);
        saveDefaultLangFiles();
        createConfigFiles();
        loadCommandStatus();
        registerCommands();
        getCommand("cmdplus").setTabCompleter(this);

        // Initialize EconomyManager
        if (!EconomyManager.setupEconomy(this)) {
            getLogger().severe("Disabling plugin due to no economy system found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Economy system found and integrated.");

        // Register SellChestListener
        getServer().getPluginManager().registerEvents(new SellChestListener(this), this);

        // Initialize CmdBlocker
        CmdBlocker cmdBlocker = new CmdBlocker(this);
        getServer().getPluginManager().registerEvents(new CommandBlockerTabCompleter(cmdBlocker), this);

        // Translate color codes in the plugin prefix
        String translatedPrefix = ChatColor.translateAlternateColorCodes('&', configSettings.getPluginPrefix());
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "===============================");
        Bukkit.getConsoleSender().sendMessage(translatedPrefix + "cmdplus is now enabled!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "===============================");

        // Initialize AnnouncementManager
        announcementManager = new AnnouncementManager(this);
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "cmdplus is now disabled D:");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============================");
    }

    public String getChatMessage(String key, Map<String, String> placeholders) {
        String message = config.getString("Chat.Messages." + key, "Message not found");
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("Chat.Settings.JoinMessage", true);
    }

    public boolean isLeaveMessageEnabled() {
        return config.getBoolean("Chat.Settings.LeaveMessage", true);
    }

    public BukkitAudiences adventure() {
        return adventure;
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

    private void createConfigFiles() {
        createFileIfNotExists(new File(getDataFolder(), "commands.yml"), "default commands content");
        createFileIfNotExists(new File(getDataFolder(), "prices.yml"), "default prices content");
        createFileIfNotExists(new File(getDataFolder(), "blockedcmds.yml"), "default blocked commands content");
        createFileIfNotExists(new File(getDataFolder(), "aliases.yml"), "default aliases content");
    }

    private void createFileIfNotExists(File file, String defaultContent) {
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(defaultContent);
            } catch (IOException e) {
                getLogger().severe("Could not create file: " + file.getName());
            }
        }
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
        if (commandStatus.getOrDefault("tempban", false)) {
            getCommand("tempban").setExecutor(new ChatCommands(this));
        }
        if (commandStatus.getOrDefault("mute", false)) {
            getCommand("mute").setExecutor(new ChatCommands(this));
        }
        if (commandStatus.getOrDefault("ban", false)) {
            getCommand("ban").setExecutor(new ChatCommands(this));
        }
        if (commandStatus.getOrDefault("kick", false)) {
            getCommand("kick").setExecutor(new ChatCommands(this));
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        configSettings.reloadConfig();
        cmdAliasHandler.reloadAliases();
        cmdSignHandler.reloadSigns();
        loadCommandStatus();
        registerCommands();

        // Log new config.yml settings
        getLogger().info("New config.yml settings:");
        for (String key : config.getKeys(true)) {
            getLogger().info(key + ": " + config.get(key));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cmdplus")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getChatMessage("cmdplus_usage", null)));
                return true;
            }

            long startTime = System.nanoTime();

            switch (args[0].toLowerCase()) {
                case "reload":
                    if (args.length == 1) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getChatMessage("cmdplus_reload_usage", null)));
                        return true;
                    }
                    switch (args[1].toLowerCase()) {
                        case "all":
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading cmdplus..."));
                            reloadPluginConfig();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "cmdplus &areloaded!"));
                            break;
                        case "alias":
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading aliases..."));
                            cmdAliasHandler.reloadAliases();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Aliases &areloaded!"));
                            break;
                        case "sign":
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading signs..."));
                            cmdSignHandler.reloadSigns();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Signs &areloaded!"));
                            break;
                        case "config":
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading config..."));
                            reloadConfig();
                            config = getConfig();
                            configSettings.reloadConfig();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Config &areloaded!"));
                            break;
                        case "chat":
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Reloading chat config..."));
                            reloadPluginConfig();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "Chat config &areloaded!"));
                            break;
                        default:
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getChatMessage("cmdplus_reload_usage", null)));
                            return true;
                    }
                    long endTime = System.nanoTime();
                    long duration = endTime - startTime;
                    double seconds = duration / 1_000_000_000.0;
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("Reload took %.3f seconds.", seconds)));
                    return true;

                default:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getChatMessage("cmdplus_usage", null)));
                    return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cmdplus")) {
            if (args.length == 1) {
                return Arrays.asList("reload");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
                return Arrays.asList("all", "alias", "sign", "config", "chat");
            }
        }
        return Collections.emptyList();
    }
}