package me.JuliusH_1;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class cmdsign implements Listener {

    private final JavaPlugin plugin;
    private final List<SignData> signs = new ArrayList<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cmdsignCooldown;
    private String pluginPrefix;
    private final Map<UUID, Location> pendingRemovals = new HashMap<>();
    private FileConfiguration messages;
    private configsettings configSettings;

    public cmdsign(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configSettings = new configsettings(plugin);
        this.cmdsignCooldown = configSettings.getCmdsignCooldown();
        this.pluginPrefix = configSettings.getPluginPrefix();
        loadMessages(configSettings.getLanguage());
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadMessages(String language) {
        File messagesFile = new File(this.plugin.getDataFolder(), "lang/messages_" + language.toLowerCase() + ".yml");
        if (!messagesFile.exists()) {
            messagesFile = new File(this.plugin.getDataFolder(), "lang/messages_en.yml");
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return messages.getString(key, "Message not found: " + key);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null && isSign(block.getType())) {
            Sign sign = (Sign) block.getState();
            Player player = event.getPlayer();

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (sign.getLine(0).equalsIgnoreCase("[cmd]")) {
                    if (player.isSneaking()) {
                        if (!player.hasPermission("cmdsign.edit")) {
                            player.sendMessage(pluginPrefix + plugin.getConfig().getString("messages.no_permission"));
                            return;
                        }
                        // Allow player to edit the sign
                        player.openSign(sign);
                    } else {
                        if (!player.hasPermission("cmdsign.use")) {
                            player.sendMessage(pluginPrefix + plugin.getConfig().getString("messages.no_permission"));
                            event.setCancelled(true);
                            return;
                        }

                        UUID playerId = player.getUniqueId();
                        long currentTime = System.currentTimeMillis();

                        if (cooldowns.containsKey(playerId) && (currentTime - cooldowns.get(playerId)) < cmdsignCooldown) {
                            player.sendMessage(pluginPrefix + getMessage("on_cmdsign_cooldown"));
                            event.setCancelled(true);
                            return;
                        }

                        String command = sign.getLine(1);
                        String subCommand1 = sign.getLine(2);
                        String subCommand2 = sign.getLine(3);
                        String fullCommand = command + " " + subCommand1 + " " + subCommand2;
                        player.performCommand(fullCommand);
                        cooldowns.put(playerId, currentTime);
                        event.setCancelled(true);
                    }
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (sign.getLine(0).equalsIgnoreCase("[cmd]")) {
                    UUID playerId = player.getUniqueId();
                    Location signLocation = sign.getLocation();

                    if (pendingRemovals.containsKey(playerId) && pendingRemovals.get(playerId).equals(signLocation)) {
                        block.breakNaturally(new ItemStack(Material.AIR));
                        pendingRemovals.remove(playerId);
                        player.sendMessage(pluginPrefix + getMessage("cmdsign_removed"));
                    } else {
                        pendingRemovals.put(playerId, signLocation);
                        player.sendMessage(pluginPrefix + getMessage("confirm_remove_cmdsign"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getLine(0).equalsIgnoreCase("[cmd]")) {
            if (!player.hasPermission("cmdsign.create")) {
                player.sendMessage(pluginPrefix + plugin.getConfig().getString("messages.no_permission"));
                return;
            }
            String command = event.getLine(1);
            String subCommand1 = event.getLine(2);
            String subCommand2 = event.getLine(3);
            String fullCommand = command + " " + subCommand1 + " " + subCommand2;
            boolean success = player.performCommand(fullCommand);
            if (success) {
                player.sendMessage(pluginPrefix + "CmdSign successfully created!");
            } else {
                player.sendMessage(pluginPrefix + "CmdSign failed to create!");
            }
        }
    }

    private boolean isSign(Material material) {
        return material == Material.OAK_SIGN || material == Material.OAK_WALL_SIGN ||
                material == Material.SPRUCE_SIGN || material == Material.SPRUCE_WALL_SIGN ||
                material == Material.BIRCH_SIGN || material == Material.BIRCH_WALL_SIGN ||
                material == Material.JUNGLE_SIGN || material == Material.JUNGLE_WALL_SIGN ||
                material == Material.ACACIA_SIGN || material == Material.ACACIA_WALL_SIGN ||
                material == Material.DARK_OAK_SIGN || material == Material.DARK_OAK_WALL_SIGN ||
                material == Material.CRIMSON_SIGN || material == Material.CRIMSON_WALL_SIGN ||
                material == Material.WARPED_SIGN || material == Material.WARPED_WALL_SIGN ||
                material == Material.MANGROVE_SIGN || material == Material.MANGROVE_WALL_SIGN ||
                material == Material.BAMBOO_SIGN || material == Material.BAMBOO_WALL_SIGN ||
                material == Material.CHERRY_SIGN || material == Material.CHERRY_WALL_SIGN;
    }

    public void saveSign(Sign sign) {
        signs.add(new SignData(sign.getLocation(), sign.getLines()));
    }

    public void reloadSigns() {
        for (SignData signData : signs) {
            Block block = signData.getLocation().getBlock();
            if (isSign(block.getType())) {
                Sign sign = (Sign) block.getState();
                String[] lines = signData.getLines();
                for (int i = 0; i < lines.length; i++) {
                    sign.setLine(i, lines[i]);
                }
                sign.update();
            }
        }
    }

    private static class SignData {
        private final Location location;
        private final String[] lines;

        public SignData(Location location, String[] lines) {
            this.location = location;
            this.lines = lines;
        }

        public Location getLocation() {
            return location;
        }

        public String[] getLines() {
            return lines;
        }
    }
}