package me.home4.firebush.firebush.files.amongus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class WeaponListener implements Listener {

    private final KillCooldownManager cooldownManager;


    public WeaponListener(KillCooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is the Murder Weapon
        if (item != null && item.getType() == Material.WOODEN_AXE && item.getItemMeta() != null &&
                item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("Murder Weapon")) {
            // Check if cooldown is active
            if (cooldownManager.isCooldownActive(player)) {
                long remaining = cooldownManager.getRemainingCooldown(player);
                player.sendMessage("You cannot use the axe yet. Cooldown remaining: " + cooldownManager.formatCooldown(remaining));
                event.setCancelled(true); // Prevent usage
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            ItemStack item = killer.getInventory().getItemInMainHand();

            // Check if the killer used the Murder Weapon
            if (item != null && item.getType() == Material.WOODEN_AXE && item.getItemMeta() != null &&
                    item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals("Murder Weapon")) {
                // Remove the axe from the killer's inventory
                killer.getInventory().removeItem(item);
                cooldownManager.startCooldown(killer); // Start cooldown

//                event.setDeathMessage(null);
            }
        }

        // Hide death messages
    }
}