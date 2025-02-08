package me.JuliusH_1.chat;

import me.JuliusH_1.cmdplus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

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