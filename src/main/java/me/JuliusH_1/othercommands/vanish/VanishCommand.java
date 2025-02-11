package me.JuliusH_1.othercommands.vanish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final VanishManager vanishManager;

    public VanishCommand(JavaPlugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!plugin.getConfig().getBoolean("Commands.vanish", true)) {
            sender.sendMessage(ChatColor.RED + "This command is disabled.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("vanish.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (vanishManager.isVanished(player)) {
            showPlayer(player);
            vanishManager.setVanished(player, false);
            player.sendMessage(ChatColor.GREEN + "You are now visible.");
        } else {
            hidePlayer(player);
            vanishManager.setVanished(player, true);
            player.sendMessage(ChatColor.GREEN + "You are now vanished.");
        }

        return true;
    }

    private void hidePlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("vanish.see")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
            if (!onlinePlayer.hasPermission("vanish.see.tab")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
    }

    private void showPlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
    }
}