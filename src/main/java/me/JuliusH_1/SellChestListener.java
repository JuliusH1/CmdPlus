package me.JuliusH_1;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class SellChestListener implements Listener {

    private final JavaPlugin plugin;
    private final Essentials essentials;
    private final Map<Material, Double> itemPrices = new HashMap<>();
    private final Map<UUID, List<Transaction>> transactionHistory = new HashMap<>();
    private String pluginPrefix;

    public SellChestListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (this.essentials == null) {
            plugin.getLogger().severe("Essentials plugin not found!");
            return;
        }
        plugin.getLogger().info("Essentials plugin found and loaded.");
        this.pluginPrefix = plugin.getConfig().getString("pluginPrefix", "[SellChest]");
        loadPrices();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("SellChestListener registered.");
    }

    private void loadPrices() {
        File pricesFile = new File(plugin.getDataFolder(), "prices.yml");
        if (!pricesFile.exists()) {
            plugin.saveResource("prices.yml", false);
        }
        FileConfiguration pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
        for (String key : pricesConfig.getKeys(false)) {
            Material material = Material.getMaterial(key.toUpperCase());
            if (material != null) {
                itemPrices.put(material, pricesConfig.getDouble(key));
                plugin.getLogger().info("Loaded price for " + material + ": " + pricesConfig.getDouble(key));
            } else {
                plugin.getLogger().warning("Invalid material in prices.yml: " + key);
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        plugin.getLogger().info("SignChangeEvent triggered by player: " + player.getName());
        if (event.getLine(0).equalsIgnoreCase("[sellchest]")) {
            handleSellChestSignCreation(event, player, block);
        }
    }

    private void handleSellChestSignCreation(SignChangeEvent event, Player player, Block block) {
        if (event.getLine(1).isEmpty()) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " You must specify a name on the second line.");
            event.setCancelled(true);
            return;
        }
        Block attachedBlock = block.getRelative(((Directional) block.getBlockData()).getFacing().getOppositeFace());
        if (!isChest(attachedBlock.getType())) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " The sign must be attached to a chest.");
            event.setCancelled(true);
            return;
        }
        if (!player.hasPermission("sellchest.create")) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " You do not have permission to create a SellChest.");
            event.setCancelled(true);
            event.setLine(0, ChatColor.RED + "[sellchest]");
            return;
        }
        event.setLine(0, ChatColor.GREEN + "[sellchest]");
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " SellChest created successfully!");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && isSign(block.getType())) {
                Sign sign = (Sign) block.getState();
                plugin.getLogger().info("PlayerInteractEvent triggered by player: " + event.getPlayer().getName());
                if (sign.getLine(0).equalsIgnoreCase("[sellchest]")) {
                    Block chestBlock = block.getRelative(((Directional) sign.getBlockData()).getFacing().getOppositeFace());
                    if (isChest(chestBlock.getType())) {
                        sellItems(event.getPlayer(), (Chest) chestBlock.getState(), sign.getLine(1));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getClickedInventory().getHolder();
            Block block = chest.getBlock().getRelative(BlockFace.UP);
            if (isSign(block.getType())) {
                Sign sign = (Sign) block.getState();
                plugin.getLogger().info("InventoryClickEvent triggered by player: " + event.getWhoClicked().getName());
                if (sign.getLine(0).equalsIgnoreCase("[sellchest]")) {
                    Bukkit.getScheduler().runTask(plugin, () -> sellItems((Player) event.getWhoClicked(), chest, sign.getLine(1)));
                }
            }
        }
    }

    private boolean isChest(Material material) {
        return material == Material.CHEST || material == Material.TRAPPED_CHEST;
    }

    private boolean isSign(Material material) {
        return material.name().endsWith("_SIGN") || material.name().endsWith("_WALL_SIGN");
    }

    private void sellItems(Player player, Chest chest, String targetPlayerName) {
        double totalValue = 0.0;
        ItemStack[] contents = chest.getInventory().getContents();
        plugin.getLogger().info("Starting to process chest contents for selling.");

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null) {
                plugin.getLogger().info("Processing item: " + item.getType() + " x" + item.getAmount());
                if (itemPrices.containsKey(item.getType())) {
                    double itemPrice = itemPrices.get(item.getType());
                    int itemAmount = item.getAmount();
                    totalValue += itemPrice * itemAmount;
                    chest.getInventory().setItem(i, null); // Remove the item from the chest
                    plugin.getLogger().info("Removed item: " + item.getType() + " x" + itemAmount);
                } else {
                    plugin.getLogger().info("Item not priced: " + item.getType());
                }
            }
        }

        plugin.getLogger().info("Total value of sold items: $" + totalValue);
        if (totalValue > 0) {
            try {
                Economy.add(targetPlayerName, totalValue);
                player.sendMessage(pluginPrefix + " Sold items for $" + totalValue);
                plugin.getLogger().info("Added $" + totalValue + " to " + targetPlayerName);
                logTransaction(player, totalValue);
            } catch (MaxMoneyException | com.earth2me.essentials.api.UserDoesNotExistException | com.earth2me.essentials.api.NoLoanPermittedException e) {
                player.sendMessage(pluginPrefix + " Transaction failed: " + e.getMessage());
                plugin.getLogger().severe("Transaction failed: " + e.getMessage());
            }
        } else {
            player.sendMessage(pluginPrefix + " No sellable items found in the chest.");
            plugin.getLogger().info("No sellable items found in the chest.");
        }
    }

    private void logTransaction(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        transactionHistory.putIfAbsent(playerId, new ArrayList<>());
        transactionHistory.get(playerId).add(new Transaction(player.getName(), amount, System.currentTimeMillis()));
    }

    public List<Transaction> getTransactionHistory(Player player) {
        return transactionHistory.getOrDefault(player.getUniqueId(), Collections.emptyList());
    }
}