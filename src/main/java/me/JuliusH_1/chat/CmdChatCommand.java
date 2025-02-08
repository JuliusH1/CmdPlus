package me.JuliusH_1.chat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Random;

public class CmdChatCommand implements CommandExecutor {
    private final AnnouncementManager announcementManager;

    public CmdChatCommand(AnnouncementManager announcementManager) {
        this.announcementManager = announcementManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /cmdchat testannouncement [number]");
            return true;
        }

        if (args[0].equalsIgnoreCase("testannouncement")) {
            if (args.length == 1) {
                // Send a random announcement
                List<String> announcements = announcementManager.getAnnouncements();
                String randomAnnouncement = announcements.get(new Random().nextInt(announcements.size()));
                announcementManager.broadcastAnnouncement(randomAnnouncement);
            } else {
                try {
                    int announcementNumber = Integer.parseInt(args[1]);
                    List<String> announcements = announcementManager.getAnnouncements();
                    if (announcementNumber > 0 && announcementNumber <= announcements.size()) {
                        String specificAnnouncement = announcements.get(announcementNumber - 1);
                        announcementManager.broadcastAnnouncement(specificAnnouncement);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid announcement number.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid announcement number.");
                }
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        return true;
    }
}