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
import java.util.HashMap;
import java.util.Map;

public class SellChestListener implements Listener {

    private final JavaPlugin plugin;
    private final Essentials essentials;
    private final Map<Material, Double> itemPrices = new HashMap<>();
    private String pluginPrefix;

    public SellChestListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        this.pluginPrefix = plugin.getConfig().getString("pluginPrefix", "[SellChest]");
        loadPrices();
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock().getRelative(BlockFace.UP);
        if (event.getLine(0).equalsIgnoreCase("[sellchest]")) {
            if (event.getLine(1).isEmpty()) {
                player.sendMessage(pluginPrefix + ChatColor.RED + " You must specify a name on the second line.");
                event.setCancelled(true);
                return;
            }
            if (!isChest(block.getType())) {
                player.sendMessage(pluginPrefix + ChatColor.RED + " There must be a chest below the sign.");
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
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && isSign(block.getType())) {
                Sign sign = (Sign) block.getState();
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

    private void sellItems(Player player, Chest chest, String targetPlayerName) {
        double totalValue = 0.0;
        ItemStack[] contents = chest.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && itemPrices.containsKey(item.getType())) {
                totalValue += itemPrices.get(item.getType()) * item.getAmount();
                chest.getInventory().remove(item);
            }
        }
        if (totalValue > 0) {
            try {
                Economy.add(targetPlayerName, totalValue);
                player.sendMessage(pluginPrefix + " Sold items for $" + totalValue);
            } catch (MaxMoneyException e) {
                player.sendMessage(pluginPrefix + " You have reached the maximum amount of money.");
            } catch (com.earth2me.essentials.api.UserDoesNotExistException e) {
                player.sendMessage(pluginPrefix + " User does not exist.");
            } catch (com.earth2me.essentials.api.NoLoanPermittedException e) {
                player.sendMessage(pluginPrefix + " No loan permitted.");
            }
        } else {
            player.sendMessage(pluginPrefix + " No sellable items found in the chest.");
        }
    }
}