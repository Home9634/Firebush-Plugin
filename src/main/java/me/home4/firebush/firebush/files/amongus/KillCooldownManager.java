package me.home4.firebush.firebush.files.amongus;

import me.home4.firebush.firebush.Firebush;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

import static me.home4.firebush.firebush.Firebush.plugin;

public class KillCooldownManager {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, String> lastActionBarMessage = new HashMap<>();

    //    private final long cooldownTime = 20 * 60 * 1000; // 20 minutes in milliseconds
    private final long cooldownTime = 20 * 60 * 1000; // 25 minutes in milliseconds


    public void startCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
    }

    public void resetCooldown(Player player) {
        startCooldown(player); // Restart the cooldown
    }

    public boolean isCooldownActive(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }
        return System.currentTimeMillis() < cooldowns.get(uuid);
    }

    public long getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }
        return cooldowns.get(uuid) - System.currentTimeMillis();
    }

    public void handleCooldowns() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : cooldowns.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        long remainingTime = getRemainingCooldown(player);
                        if (remainingTime <= 0) {
                            if (!player.getInventory().contains(WeaponManager.getSharpnessAxe())) {
                                player.getInventory().addItem(WeaponManager.getSharpnessAxe());
                                player.sendMessage("Your kill cooldown has expired. You have received your axe.");
                            }
                            cooldowns.remove(uuid);
                            lastActionBarMessage.remove(uuid);
                        } else {
                            String timeFormatted = formatCooldown(remainingTime);
                            String actionBarMessage = "Kill Cooldown: " + timeFormatted;

                            // Only update the action bar if the message changes
                            if (!actionBarMessage.equals(lastActionBarMessage.get(uuid))) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                                lastActionBarMessage.put(uuid, actionBarMessage);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    String formatCooldown(long timeMillis) {
        long seconds = (timeMillis / 1000) % 60;
        long minutes = (timeMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void giveInitialWeapon(Player player) {
        // Ensure the player starts with the weapon
        if (!player.getInventory().contains(WeaponManager.getSharpnessAxe())) {
            player.getInventory().addItem(WeaponManager.getSharpnessAxe());
        }
    }
}
