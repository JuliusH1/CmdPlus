package me.JuliusH_1.chat;

import me.JuliusH_1.cmdplus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AnnouncementManager {
    private final cmdplus plugin;
    private final List<String> announcements;
    private final int interval;
    private final boolean randomOrder;
    private int currentIndex;

    public AnnouncementManager(cmdplus plugin) {
        this.plugin = plugin;
        this.announcements = new ArrayList<>();
        this.currentIndex = 0;

        // Load announcements from the configuration
        FileConfiguration config = plugin.getConfig();
        this.interval = config.getInt("announcements.time", 15) * 60 * 20; // Convert minutes to ticks
        this.randomOrder = config.getBoolean("announcements.random", true);

        int index = 1;
        while (config.contains("announcements." + index)) {
            List<String> messages = config.getStringList("announcements." + index + ".message");
            for (String message : messages) {
                announcements.add(processCenterTag(message));
            }
            index++;
        }

        if (randomOrder) {
            Collections.shuffle(announcements);
        }

        startAnnouncements();
    }

    public List<String> getAnnouncements() {
        return announcements;
    }

    public void broadcastAnnouncement(String announcement) {
        Bukkit.broadcastMessage(announcement);
    }

    private void startAnnouncements() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (announcements.isEmpty()) {
                    return;
                }

                String announcement;
                if (randomOrder) {
                    announcement = announcements.get(new Random().nextInt(announcements.size()));
                } else {
                    announcement = announcements.get(currentIndex);
                    currentIndex = (currentIndex + 1) % announcements.size();
                }

                Bukkit.broadcastMessage(announcement);
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    private String processCenterTag(String message) {
        String centerTag = "<center>";
        String endCenterTag = "</center>";
        if (message.contains(centerTag) && message.contains(endCenterTag)) {
            int start = message.indexOf(centerTag) + centerTag.length();
            int end = message.indexOf(endCenterTag);
            String textToCenter = message.substring(start, end);
            String centeredText = TextUtils.centerText(textToCenter, 80); // Assuming 80 characters width
            return message.replace(centerTag + textToCenter + endCenterTag, centeredText);
        }
        return message;
    }
}