package me.JuliusH_1;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerJoinListener implements Listener {
    private final cmdplus plugin;

    public PlayerJoinListener(cmdplus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getChatConfig().getBoolean("Settings.JoinMessage", true)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", event.getPlayer().getName());
            String joinMessage = plugin.getChatMessage("Messages.join_message", placeholders);
            event.setJoinMessage(TextUtils.parse(joinMessage).toString());
        }
    }
}