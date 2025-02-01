package me.JuliusH_1;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

public class ChatUtils {
    private final cmdplus plugin;

    public ChatUtils(cmdplus plugin) {
        this.plugin = plugin;
    }

    public void sendFormattedMessage(Player player, String message) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component component = miniMessage.deserialize(message.toLowerCase());
        plugin.adventure().player(player).sendMessage(component);
    }
}