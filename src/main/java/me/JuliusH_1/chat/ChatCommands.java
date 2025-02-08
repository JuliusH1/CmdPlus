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
        if (args.length < 3) {
            sender.sendMessage("Usage: /" + label + " <player> <time> <reason>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found.");
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
                target.kickPlayer(plugin.getChatMessage("Messages.ban_message", placeholders).toLowerCase());
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());
                break;
            case "mute":
                chatUtils.sendFormattedMessage(target, plugin.getChatMessage("Messages.mute_message", placeholders));
                break;
            case "kick":
                target.kickPlayer(plugin.getChatMessage("Messages.kick_message", placeholders).toLowerCase());
                break;
            case "tempban":
                target.kickPlayer(plugin.getChatMessage("Messages.tempban_message", placeholders).toLowerCase());
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());
                break;
            default:
                return false;
        }
        return true;
    }
}