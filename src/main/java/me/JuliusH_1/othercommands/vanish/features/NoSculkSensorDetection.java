package me.JuliusH_1.othercommands.vanish.features;

import me.JuliusH_1.othercommands.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class NoSculkSensorDetection implements Listener {
    private final VanishManager vanishManager;

    public NoSculkSensorDetection(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.SCULK_SENSOR) {
            Player player = (Player) event.getPlayer();
            if (vanishManager.isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }
}