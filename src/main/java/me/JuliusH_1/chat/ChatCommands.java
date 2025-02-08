package me.JuliusH_1.chat;

import me.JuliusH_1.cmdplus;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatCommands implements CommandExecutor {
    private final cmdplus plugin;
    private final ChatUtils chatUtils;

    public ChatCommands(cmdplus plugin) {
        this.plugin = plugin;
        this.chatUtils = new ChatUtils(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 3) {
            player.sendMessage("Usage: /" + label + " <player> <time> <reason>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return false;
        }

        String time = args[1];
        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("time", time);
        placeholders.put("reason", reason);

        switch (label.toLowerCase()) {
            case "ban":
                if (!player.hasPermission("cmdplus.ban")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                target.kickPlayer(plugin.getChatMessage("Messages.ban_message", placeholders).toLowerCase());
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());
                break;
            case "mute":
                if (!player.hasPermission("cmdplus.mute")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                chatUtils.sendFormattedMessage(target, plugin.getChatMessage("Messages.mute_message", placeholders));
                break;
            case "kick":
                if (!player.hasPermission("cmdplus.kick")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                target.kickPlayer(plugin.getChatMessage("Messages.kick_message", placeholders).toLowerCase());
                break;
            case "tempban":
                if (!player.hasPermission("cmdplus.tempban")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                target.kickPlayer(plugin.getChatMessage("Messages.tempban_message", placeholders).toLowerCase());
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());
                break;
            case "tempmute":
                if (!player.hasPermission("cmdplus.tempmute")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                chatUtils.sendFormattedMessage(target, plugin.getChatMessage("Messages.tempmute_message", placeholders));
                break;
            default:
                return false;
        }
        return true;
    }
}