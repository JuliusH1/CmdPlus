package me.JuliusH_1.othercommands.vanish;

import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;

public class VanishManager {
    private final Set<Player> vanishedPlayers = new HashSet<>();

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player);
    }

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player);
        } else {
            vanishedPlayers.remove(player);
        }
    }

    public Set<Player> getVanishedPlayers() {
        return vanishedPlayers;
    }
}