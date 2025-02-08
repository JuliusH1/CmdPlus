package me.JuliusH_1.chat.listeners;

import me.JuliusH_1.chat.TextUtils;
import me.JuliusH_1.cmdplus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerLeaveListener implements Listener {
    private final cmdplus plugin;

    public PlayerLeaveListener(cmdplus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getConfig().getBoolean("Chat.Settings.LeaveMessage", true)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", event.getPlayer().getName());
            String leaveMessage = plugin.getChatMessage("leave_message", placeholders);
            event.setQuitMessage(TextUtils.parse(leaveMessage).toString());
        }
    }
}