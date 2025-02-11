package me.JuliusH_1.othercommands.vanish.features;

import me.JuliusH_1.othercommands.vanish.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class NightVision implements Listener {
    private final JavaPlugin plugin;
    private final VanishManager vanishManager;

    public NightVision(JavaPlugin plugin, VanishManager vanishManager) {
        this.plugin = plugin;
        this.vanishManager = vanishManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfig().getBoolean("Vanish.Night_vision", true) && vanishManager.isVanished(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
    }
}
