package me.JuliusH_1.othercommands.vanish.features;

import me.JuliusH_1.othercommands.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.entity.Player;

public class NoMobSpawn implements Listener {
    private final VanishManager vanishManager;

    public NoMobSpawn(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Player player = (Player) event.getEntity();
        if (vanishManager.isVanished(player)) {
            event.setCancelled(true);
        }
    }
}