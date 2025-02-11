package me.JuliusH_1.othercommands.vanish.features;

import me.JuliusH_1.othercommands.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.entity.Player;

public class HideAdvancementMessages implements Listener {
    private final VanishManager vanishManager;

    public HideAdvancementMessages(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (vanishManager.isVanished(player)) {
            event.getPlayer().sendMessage(""); // Hide the advancement message
        }
    }
}