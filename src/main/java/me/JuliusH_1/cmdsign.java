package me.JuliusH_1;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class cmdsign implements Listener {

    private final JavaPlugin plugin;
    private final List<SignData> signs = new ArrayList<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long cmdsignCooldown;
    private String pluginPrefix;
    private final Map<UUID, Location> pendingRemovals = new HashMap<>();
    private FileConfiguration messages;
    private ConfigSettings ConfigSettings;

    public cmdsign(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ConfigSettings = new ConfigSettings(plugin);
        this.cmdsignCooldown = ConfigSettings.getCmdsignCooldown();
        this.pluginPrefix = ConfigSettings.getPluginPrefix();
        loadMessages(ConfigSettings.getLanguage());
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
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (block != null && isSign(block.getType())) {
            Sign sign = (Sign) block.getState();

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (sign.getLine(0).equalsIgnoreCase("[cmd]")) {
                    if (player.isSneaking()) {
                        if (!player.hasPermission("cmdsign.edit")) {
                            player.sendMessage(pluginPrefix + plugin.getConfig().getString("messages.no_permission"));
                            return;
                        }
                        // Allow player to edit the sign
                        player.openSign(sign);
                        sign.update(); // Ensure the sign is updated
                    } else {
                        if (!player.hasPermission("cmdsign.use")) {
                            player.sendMessage(pluginPrefix + plugin.getConfig().getString("messages.no_permission"));
                            event.setCancelled(true);
                            return;
                        }

                        UUID playerId = player.getUniqueId();
                        long currentTime = System.currentTimeMillis();

                        if (cooldowns.containsKey(playerId) && (currentTime - cooldowns.get(playerId)) < cmdsignCooldown) {
                            player.sendMessage(pluginPrefix + ChatColor.RED + getMessage("on_cmdsign_cooldown"));
                            event.setCancelled(true);
                            return;
                        }

                        String command = sign.getLine(1);
                        String subCommand1 = sign.getLine(2);
                        String subCommand2 = sign.getLine(3);
                        String fullCommand = command + " " + subCommand1 + " " + subCommand2;
                        boolean success = player.performCommand(fullCommand);
                        sendCommandFeedback(player, success);
                        cooldowns.put(playerId, currentTime);
                        logSignAction(success ? "Used" : "Failed to use", player, sign);
                        event.setCancelled(true);
                    }
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (sign.getLine(0).equalsIgnoreCase("[cmd]")) {
                    UUID playerId = player.getUniqueId();
                    Location signLocation = sign.getLocation();

                    if (pendingRemovals.containsKey(playerId) && pendingRemovals.get(playerId).equals(signLocation)) {
                        if (canBreakSign(player, signLocation)) {
                            block.breakNaturally(new ItemStack(Material.AIR));
                            pendingRemovals.remove(playerId);
                            player.sendMessage(pluginPrefix + getMessage("cmdsign_removed"));
                            logSignAction("Removed", player, sign);
                        } else {
                            player.sendMessage(pluginPrefix + getMessage("no_permission_to_break"));
                        }
                    } else {
                        pendingRemovals.put(playerId, signLocation);
                        player.sendMessage(pluginPrefix + getMessage("confirm_remove_cmdsign"));
                    }
                } else if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    if (isDye(itemInHand.getType())) {
                        applyColorToSign(sign, itemInHand.getType());
                        player.sendMessage(pluginPrefix + ChatColor.GREEN + "Sign color updated!");
                        event.setCancelled(true);
                    } else if (itemInHand.getType() == Material.INK_SAC) {
                        resetSignColor(sign);
                        player.sendMessage(pluginPrefix + ChatColor.GREEN + "Sign color reset!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean isDye(Material material) {
        return material.name().endsWith("_DYE") || material == Material.GLOW_INK_SAC;
    }

    private void applyColorToSign(Sign sign, Material dye) {
        ChatColor color = getColorFromDye(dye);
        for (int i = 0; i < sign.getLines().length; i++) {
            sign.setLine(i, color + ChatColor.stripColor(sign.getLine(i)));
        }
        sign.update();
    }

    private ChatColor getColorFromDye(Material dye) {
        switch (dye) {
            case WHITE_DYE: return ChatColor.WHITE;
            case ORANGE_DYE: return ChatColor.GOLD;
            case MAGENTA_DYE: return ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE_DYE: return ChatColor.AQUA;
            case YELLOW_DYE: return ChatColor.YELLOW;
            case LIME_DYE: return ChatColor.GREEN;
            case PINK_DYE: return ChatColor.LIGHT_PURPLE;
            case GRAY_DYE: return ChatColor.DARK_GRAY;
            case LIGHT_GRAY_DYE: return ChatColor.GRAY;
            case CYAN_DYE: return ChatColor.DARK_AQUA;
            case PURPLE_DYE: return ChatColor.DARK_PURPLE;
            case BLUE_DYE: return ChatColor.BLUE;
            case BROWN_DYE: return ChatColor.GOLD;
            case GREEN_DYE: return ChatColor.DARK_GREEN;
            case RED_DYE: return ChatColor.RED;
            case BLACK_DYE: return ChatColor.BLACK;
            case GLOW_INK_SAC: return ChatColor.BOLD; // Example: using bold for glow ink sac
            default: return ChatColor.WHITE;
        }
    }

    private void resetSignColor(Sign sign) {
        for (int i = 0; i < sign.getLines().length; i++) {
            sign.setLine(i, ChatColor.stripColor(sign.getLine(i)));
        }
        sign.update();
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
            if (Bukkit.getServer().getPluginCommand(fullCommand.split(" ")[0]) != null) {
                player.sendMessage(pluginPrefix + "CmdSign successfully created!");
                logSignAction("Created", player, (Sign) event.getBlock().getState());
            } else {
                player.sendMessage(pluginPrefix + "CmdSign failed to create!");
                logSignAction("Failed to create", player, (Sign) event.getBlock().getState());
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

    private boolean canBreakSign(Player player, Location signLocation) {
        TownyAPI townyAPI = TownyAPI.getInstance();
        TownBlock townBlock = townyAPI.getTownBlock(signLocation);
        if (townBlock != null) {
            Town signTown = townBlock.getTownOrNull();
            Town playerTown = townyAPI.getResident(player).getTownOrNull();
            return signTown != null && playerTown != null && !signTown.equals(playerTown);
        }
        return false;
    }

    private void displayCooldown(Player player, long remainingTime) {
        long seconds = (remainingTime / 1000) % 60;
        long minutes = (remainingTime / (1000 * 60)) % 60;
        long hours = (remainingTime / (1000 * 60 * 60)) % 24;
        player.sendMessage(pluginPrefix + ChatColor.RED + "You must wait " + hours + "h " + minutes + "m " + seconds + "s before using this sign again.");
    }

    private void sendCommandFeedback(Player player, boolean success) {
        if (success) {
            player.sendMessage(pluginPrefix + ChatColor.GREEN + "Command executed successfully!");
        } else {
            player.sendMessage(pluginPrefix + ChatColor.RED + "Failed to execute the command.");
        }
    }

    private void logSignAction(String action, Player player, Sign sign) {
        File logFile = new File(plugin.getDataFolder(), "cmdsignlogs.txt");
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write("[" + new Date() + "] " + action + " by " + player.getName() + " at " + sign.getLocation() + "\n");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to log sign action: " + e.getMessage());
        }
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