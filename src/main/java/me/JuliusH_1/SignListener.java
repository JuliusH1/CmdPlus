package me.JuliusH_1;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, String> privateChests = new HashMap<>();
    private ConfigSettings configSettings;
    private String pluginPrefix;

    public SignListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configSettings = new ConfigSettings(plugin); // Initialize configSettings
        this.pluginPrefix = configSettings.getPluginPrefix();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getLine(0).equalsIgnoreCase("[privatechest]")) {
            String ownerName = event.getLine(1);
            if (ownerName != null && !ownerName.isEmpty()) {
                event.setLine(0, ChatColor.GREEN + "[PrivateChest]");
                event.setLine(1, ownerName);
                player.sendMessage(pluginPrefix + ChatColor.GREEN + "Private chest sign created for " + ownerName);
            } else {
                player.sendMessage(pluginPrefix + ChatColor.RED + "You must specify a player name on the second line!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)) {
                InventoryHolder holder = ((Chest) block.getState()).getInventory().getHolder();
                if (holder instanceof DoubleChest) {
                    DoubleChest doubleChest = (DoubleChest) holder;
                    checkChestAccess(event, doubleChest.getLeftSide().getInventory().getLocation().getBlock());
                    checkChestAccess(event, doubleChest.getRightSide().getInventory().getLocation().getBlock());
                } else {
                    checkChestAccess(event, block);
                }
            }
        }
    }

    private void checkChestAccess(PlayerInteractEvent event, Block block) {
        for (Block face : new Block[]{block.getRelative(0, 1, 0), block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0), block.getRelative(0, 0, 1), block.getRelative(0, 0, -1)}) {
            if (isSign(face.getType())) {
                Sign sign = (Sign) face.getState();
                if (sign.getLine(0).equals(ChatColor.GREEN + "[PrivateChest]")) {
                    String ownerName = sign.getLine(1);
                    String additionalName1 = sign.getLine(2);
                    String additionalName2 = sign.getLine(3);
                    Player player = event.getPlayer();
                    if (!player.getName().equalsIgnoreCase(ownerName) && !player.getName().equalsIgnoreCase(additionalName1) && !player.getName().equalsIgnoreCase(additionalName2) && !player.hasPermission("cmdplus.privatechest.bypass")) {
                        player.sendMessage(pluginPrefix + ChatColor.RED + "This chest is private and can only be opened by " + ownerName);
                        event.setCancelled(true);
                    } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !player.getName().equalsIgnoreCase(ownerName)) {
                        player.sendMessage(pluginPrefix + ChatColor.RED + "Only the owner can remove this sign.");
                        event.setCancelled(true);
                    }
                }
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
}