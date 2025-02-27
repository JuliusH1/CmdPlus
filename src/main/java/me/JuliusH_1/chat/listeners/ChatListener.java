package me.JuliusH_1.chat.listeners;

import me.JuliusH_1.cmdplus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class ChatListener implements Listener {
    private final cmdplus plugin;
    private final Set<String> mutedPlayers = new HashSet<>();

    public ChatListener(cmdplus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (mutedPlayers.contains(event.getPlayer().getName())) {
            event.getPlayer().sendMessage("You are muted.");
            event.setCancelled(true);
        }
    }

    public void mutePlayer(String playerName) {
        mutedPlayers.add(playerName);
    }

    public void unmutePlayer(String playerName) {
        mutedPlayers.remove(playerName);
    }
}