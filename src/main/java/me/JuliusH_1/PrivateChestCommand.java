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
        new PrivateChestSign(plugin); // Register the SignListener
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("pca")) {
                if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
                    if (player.hasPermission("cmdplus.privatechest.admin")) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            Inventory targetChest = privateChests.get(target.getUniqueId());
                            if (targetChest != null) {
                                player.openInventory(targetChest);
                                player.sendMessage(ChatColor.GREEN + "You are now viewing " + target.getName() + "'s private chest.");
                            } else {
                                player.sendMessage(ChatColor.RED + "The player " + target.getName() + " does not have a private chest.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "The player " + args[1] + " is not online.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    return true;
                }
            } else if (label.equalsIgnoreCase("pc")) {
                if (player.hasPermission("cmdplus.privatechest")) {
                    Inventory privateChest = privateChests.computeIfAbsent(player.getUniqueId(), k -> Bukkit.createInventory(null, 27, ChatColor.GREEN + "Private Chest"));
                    player.openInventory(privateChest);
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return false;
                }
            }
        } else {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }
        return false;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Private Chest")) {
            Player player = (Player) event.getPlayer();
            privateChests.put(player.getUniqueId(), event.getInventory());
        }
    }
}