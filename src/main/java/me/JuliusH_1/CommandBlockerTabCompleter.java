package me.JuliusH_1;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandBlockerTabCompleter implements Listener {
    private final CmdBlocker cmdBlocker;

    public CommandBlockerTabCompleter(CmdBlocker cmdBlocker) {
        this.cmdBlocker = cmdBlocker;
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (event.getSender() instanceof Player) {
            Player player = (Player) event.getSender();
            if (player.isOp() || player.hasPermission("cmdplus.blockedcmds.show")) {
                return; // Do not block commands for operators or players with the permission
            }
        }

        List<String> completions = event.getCompletions();
        List<String> blockedCommands = cmdBlocker.getBlockedCommands();

        completions.removeIf(cmd -> blockedCommands.contains(cmd.toLowerCase()));
    }
}