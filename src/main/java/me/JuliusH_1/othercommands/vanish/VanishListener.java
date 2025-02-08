package me.JuliusH_1.othercommands.vanish;

import me.JuliusH_1.othercommands.vanish.VanishCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class VanishListener implements Listener {
    private final VanishCommand vanishCommand;

    public VanishListener(VanishCommand vanishCommand) {
        this.vanishCommand = vanishCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();
        for (Player vanishedPlayer : vanishCommand.getVanishedPlayers()) {
            if (!joiningPlayer.hasPermission("vanish.see")) {
                joiningPlayer.hidePlayer(vanishCommand.plugin, vanishedPlayer);
            }
            if (!joiningPlayer.hasPermission("vanish.see.tab")) {
                joiningPlayer.hidePlayer(vanishCommand.plugin, vanishedPlayer);
            }
        }
    }
}