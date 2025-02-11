package me.JuliusH_1.othercommands.vanish;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VanishListener implements Listener {
    private final VanishManager vanishManager;
    private final JavaPlugin plugin;

    public VanishListener(VanishManager vanishManager, JavaPlugin plugin) {
        this.vanishManager = vanishManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();
        for (Player vanishedPlayer : vanishManager.getVanishedPlayers()) {
            if (!joiningPlayer.hasPermission("vanish.see")) {
                joiningPlayer.hidePlayer(plugin, vanishedPlayer);
            }
            if (!joiningPlayer.hasPermission("vanish.see.tab")) {
                joiningPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
}