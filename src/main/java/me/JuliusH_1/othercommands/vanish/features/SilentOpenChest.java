package me.JuliusH_1.othercommands.vanish.features;

import me.JuliusH_1.othercommands.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.Player;

public class SilentOpenChest implements Listener {
    private final VanishManager vanishManager;

    public SilentOpenChest(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getType() == InventoryType.CHEST && vanishManager.isVanished(player)) {
            event.setCancelled(true);
            player.openInventory(event.getInventory());
        }
    }
}