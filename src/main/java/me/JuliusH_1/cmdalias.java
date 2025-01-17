package me.JuliusH_1;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class cmdalias implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final Map<String, Alias> aliases = new HashMap<>();
    private FileConfiguration messages;
    private FileConfiguration aliasesConfig;
    private File aliasesFile;
    private final String pluginPrefix;

    public cmdalias(JavaPlugin plugin) {
        this.plugin = plugin;
        configsettings configSettings = new configsettings(plugin);
        this.pluginPrefix = configSettings.getPluginPrefix();
        loadMessages();
        loadAliasesConfig();
        reloadAliases();
    }

    private void loadMessages() {
        configsettings configSettings = new configsettings(plugin);
        String language = configSettings.getLanguage().toLowerCase();
        File messagesFile = new File(this.plugin.getDataFolder(), "lang/messages_" + language + ".yml");
        if (!messagesFile.exists()) {
            messagesFile = new File(this.plugin.getDataFolder(), "lang/messages_en.yml");
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void loadAliasesConfig() {
        aliasesFile = new File(this.plugin.getDataFolder(), "aliases.yml");
        if (!aliasesFile.exists()) {
            aliasesFile.getParentFile().mkdirs();
            plugin.saveResource("aliases.yml", false);
        }
        aliasesConfig = YamlConfiguration.loadConfiguration(aliasesFile);
    }

    public void saveAliasesConfig() {
        try {
            aliasesConfig.save(aliasesFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save aliases.yml!");
            e.printStackTrace();
        }
    }

    public void registerAliasCommand(String aliasName, String permission) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            BukkitCommand aliasCommand = new BukkitCommand(aliasName) {
                @Override
                public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                    return executeAlias(aliasName, sender);
                }
            };

            aliasCommand.setDescription("Dynamically registered alias command.");
            aliasCommand.setPermission(permission != null ? permission : "commandalias.use." + aliasName.toLowerCase());
            aliasCommand.setUsage("/" + aliasName);

            commandMap.register(plugin.getDescription().getName(), aliasCommand);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().severe("Failed to dynamically register command: " + aliasName);
            e.printStackTrace();
        }
    }

    private boolean executeAlias(String aliasName, CommandSender sender) {
        Alias alias = aliases.get(aliasName.toLowerCase());
        if (alias != null) {
            if (alias.getPermission() != null && !sender.hasPermission(alias.getPermission())) {
                sender.sendMessage(pluginPrefix + getMessage("no_permission"));
                return true;
            }

            String formattedCommand = alias.getCommand().startsWith("/") ? alias.getCommand() : "/" + alias.getCommand();

            if (sender instanceof Player) {
                Player player = (Player) sender;
                formattedCommand = PlaceholderAPI.setPlaceholders(player, formattedCommand);
            }

            Bukkit.dispatchCommand(sender, formattedCommand.substring(1));
            return true;
        }
        sender.sendMessage(pluginPrefix + getMessage("alias_not_found"));
        return false;
    }

    public void reloadAliases() {
        aliases.clear();
        loadAliasesConfig();
        aliasesConfig.getConfigurationSection("Aliases").getKeys(false).forEach(aliasName -> {
            String command = aliasesConfig.getString("Aliases." + aliasName + ".command");
            String permission = aliasesConfig.getString("Aliases." + aliasName + ".permission");
            createAlias(aliasName, command, permission);
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getLogger().info("onCommand called with args: " + Arrays.toString(args));

        if (args.length < 1) {
            sender.sendMessage(pluginPrefix + getMessage("cmdalias_usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            plugin.getLogger().info("Create command detected");

            if (args.length < 3) {
                sender.sendMessage(pluginPrefix + getMessage("cmdalias_create_usage"));
                return true;
            }

            String aliasName = args[1];
            String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).replace("-", " ");
            String permission = args.length > 3 ? args[3] : null;

            createAlias(aliasName, cmd, permission);
            sender.sendMessage(pluginPrefix + getMessage("alias_created")
                    .replace("{alias}", aliasName)
                    .replace("{command}", cmd));
            if (permission != null) {
                sender.sendMessage(pluginPrefix + getMessage("alias_permission").replace("{permission}", permission));
            }

            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            plugin.getLogger().info("List command detected");

            sender.sendMessage(pluginPrefix + getMessage("alias_list_header"));
            for (String alias : aliases.keySet()) {
                sender.sendMessage("- " + alias);
            }
            return true;
        }

        sender.sendMessage(pluginPrefix + getMessage("unknown_subcommand"));
        return true;
    }

    public boolean createAlias(String aliasName, String command, String permission) {
        aliasName = aliasName.toLowerCase();
        aliases.put(aliasName, new Alias(command, permission));
        aliasesConfig.set("Aliases." + aliasName + ".command", command);
        aliasesConfig.set("Aliases." + aliasName + ".permission", permission);

        saveAliasesConfig();

        registerAliasCommand(aliasName, permission);

        updateTabCompleter();

        return true;
    }

    private void updateTabCompleter() {
        plugin.getCommand("cmdalias").setTabCompleter(this);
    }

    public String getMessage(String key) {
        return messages.getString(key, "Message not found: " + key);
    }

    private static class Alias {
        private final String command;
        private final String permission;

        public Alias(String command, String permission) {
            this.command = command;
            this.permission = permission;
        }

        public String getCommand() {
            return command;
        }

        public String getPermission() {
            return permission;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list");
        }
        if (args[0].equalsIgnoreCase("create") && args.length == 2) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
//  add velocity compatibilty for the command /server