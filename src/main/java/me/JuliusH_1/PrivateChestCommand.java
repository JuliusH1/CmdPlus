package me.JuliusH_1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class PrivateChestCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Inventory> privateChests = new HashMap<>();

    public PrivateChestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("cmdplus.privatechest")) {
                Inventory privateChest = privateChests.computeIfAbsent(player.getUniqueId(), k -> Bukkit.createInventory(null, 27, ChatColor.GREEN + "Private Chest"));
                player.openInventory(privateChest);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return false;
            }
        } else {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Private Chest")) {
            Player player = (Player) event.getPlayer();
            privateChests.put(player.getUniqueId(), event.getInventory());
        }
    }
}
