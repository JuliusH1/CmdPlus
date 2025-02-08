package me.JuliusH_1.othercommands.vanish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class VanishCommand implements CommandExecutor {
    public final JavaPlugin plugin;
    private final Set<Player> vanishedPlayers = new HashSet<>();

    public VanishCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("vanish.use")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (vanishedPlayers.contains(player)) {
            showPlayer(player);
            vanishedPlayers.remove(player);
            player.sendMessage(ChatColor.GREEN + "You are now visible.");
        } else {
            hidePlayer(player);
            vanishedPlayers.add(player);
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

    public Set<Player> getVanishedPlayers() {
        return vanishedPlayers;
    }
}