package me.JuliusH_1;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdPlusTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("cmdplus")) {
            if (args.length == 1) {
                return Arrays.asList("reload");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
                return Arrays.asList("alias", "sign", "config", "all");
            }
        }
        return new ArrayList<>();
    }
}