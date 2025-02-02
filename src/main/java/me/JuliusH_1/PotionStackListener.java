package me.JuliusH_1;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PotionStackListener implements Listener {
    private final FileConfiguration config;
    private final int maxStacked;
    private final int maxStackedSplash;
    private final List<String> blacklist;

    public PotionStackListener(FileConfiguration config) {
        this.config = config;
        this.maxStacked = config.getInt("Potions.Settings.max-stacked", 64);
        this.maxStackedSplash = config.getInt("Potions.Settings.max-stacked-splash", 64);
        this.blacklist = config.getStringList("Potions.Blacklist");
    }

    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (isPotion(item) && !isBlacklisted(item)) {
            reduceItemStack(event.getPlayer().getInventory().getItemInMainHand());
        }
    }

    @EventHandler
    public void onPotionThrow(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("RIGHT_CLICK") && event.getItem() != null) {
            ItemStack item = event.getItem();
            if (isPotion(item) && !isBlacklisted(item)) {
                reduceItemStack(item);
            }
        }
    }

    private boolean isPotion(ItemStack item) {
        Material type = item.getType();
        return type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION;
    }

    private boolean isBlacklisted(ItemStack item) {
        return blacklist.contains(item.getType().toString().toLowerCase());
    }

    private void reduceItemStack(ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            item.setType(Material.AIR);
        }
    }
}