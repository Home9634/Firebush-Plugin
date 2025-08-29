package me.home4.firebush.firebush.commands.quests;

import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.TaskManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static me.home4.firebush.firebush.Firebush.plugin;
import static org.bukkit.Bukkit.getServer;

public class FailQuestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage("You're not allowed to use this command manually!");
            return true;
        }

        String playerNick = sender.getName();
        String playerStringUUID = Players.getUUIDfromNick(playerNick);
        Player player = Bukkit.getPlayer(UUID.fromString(playerStringUUID));

        if (playerStringUUID.equals("")) {
            player.sendMessage("You're not participating in the game!");
            return true;
        }

        if (Players.getTask(playerStringUUID).getName().equals("")) {
            player.sendMessage("You don't have a quest to fail!");
            return true;
        }

//        if (Players.getLives(playerStringUUID) == 1) {
//            player.sendMessage("Red lives can't fail quests! Succeed your quest!");
//            return true;
//        }

        PlayerInventory inventory = player.getInventory();
        TaskManager.removeTask(playerStringUUID);

        Boolean hardtask = Players.get().getBoolean(playerStringUUID + ".hardtask");
        int penalty = 0;
        int maxHealth = (int) player.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (Players.get().getBoolean(playerStringUUID + ".hardtask")) {
            penalty = 20;
        }

        FileConfiguration config = plugin.getConfig();

        Location rewardsLocation = new Location(getServer().getWorld("world"), config.getInt("rewardCoords.x"), config.getInt("rewardCoords.y"), config.getInt("rewardCoords.z"));
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Math.max(maxHealth - penalty, 0));
        player.setHealth(Math.max(0, maxHealth - penalty));

        Players.setTask(playerStringUUID, null);

        // Play animation signifying that the player failed.
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 1)); // Darkness for 5 seconds
        ItemStack writtenBookItem = new ItemStack(Material.WRITTEN_BOOK);
        Item droppedBook = player.getWorld().dropItem(rewardsLocation, writtenBookItem);
        droppedBook.setPickupDelay(Integer.MAX_VALUE);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 50) { // Stop after 5 seconds
                    cancel();
                    return;
                }

                if (ticks >= 30 && ticks <= 40) {
                    LightningStrike lightning = player.getWorld().spawn(rewardsLocation, LightningStrike.class);
                }

                if (ticks >= 30) {
                    droppedBook.remove();

//                    player.getWorld().strikeLightning(rewardsLocation);
                }

                if (ticks == 0) {
                    droppedBook.setGravity(false);
                }

                droppedBook.setVelocity(new org.bukkit.util.Vector());
                player.spawnParticle(Particle.SOUL, rewardsLocation.add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0.01);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L); // Runs every 2 ticks

        // Temporary sanity check
        player.sendMessage("You have failed your quest. The Firebush is not pleased.");



        return true;
    }
}
