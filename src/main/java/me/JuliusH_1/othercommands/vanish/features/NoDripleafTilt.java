package me.JuliusH_1.othercommands.vanish.features;

import me.JuliusH_1.othercommands.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class NoDripleafTilt implements Listener {
    private final VanishManager vanishManager;

    public NoDripleafTilt(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.BIG_DRIPLEAF) {
            Player player = event.getPlayer();
            if (vanishManager.isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }
}