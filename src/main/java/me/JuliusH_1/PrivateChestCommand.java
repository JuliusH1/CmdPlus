package me.JuliusH_1;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PrivateChestCommand implements CommandExecutor, Listener, TabCompleter {

    private final JavaPlugin plugin;
    private final HashMap<UUID, Inventory> privateChests = new HashMap<>();

    public PrivateChestCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("privatechest").setExecutor(this);
        plugin.getCommand("privatechest").setTabCompleter(this);
        new PrivateChestSign(plugin); // Register the SignListener
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0 && args[0].equalsIgnoreCase("view")) {
                if (args.length == 2) {
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
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /privatechest view <player>");
                    return false;
                }
            } else if (args.length == 0) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("privatechest")) {
            if (args.length == 1) {
                List<String> subCommands = new ArrayList<>();
                subCommands.add("view");
                return subCommands;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        }
        return null;
    }
}