package me.home4.firebush.firebush.commands.quests;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.classes.Task;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.SessionManager;
import me.home4.firebush.firebush.files.TaskManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static me.home4.firebush.firebush.Firebush.plugin;
import static org.bukkit.Bukkit.getServer;

public class RerollQuestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage("You're not allowed to use this command manually!");
            return true;
        }

        String playerNick = sender.getName();
        String playerStringUUID = Players.getUUIDfromNick(playerNick);
        Player player = Bukkit.getPlayer(UUID.fromString(playerStringUUID));
        String oldTask = Players.get().getString(playerStringUUID + ".task");

        Random random = new Random();

        TaskManager taskManager = new TaskManager();

        if (playerStringUUID.equals("")) {
            player.sendMessage("You're not participating in the game!");
            return true;
        }

        if (oldTask.equals("")) {
            player.sendMessage("You don't have a quest to reroll!");
            return true;
        }

        if (taskManager.getHardTasks(SessionManager.getSession()).contains(oldTask) || Players.get().getBoolean(playerStringUUID + ".hardtask")) {
            player.sendMessage("You already have a hard quest!");
            return true;
        }

        List<Task> hardTasks = taskManager.getHardTasks(SessionManager.getSession());
        if (hardTasks.isEmpty() ) {
            player.sendMessage("No available hard quests for you to recieve.");
            return true;
        }

        TaskManager.removeTask(playerStringUUID);

        Collections.shuffle(hardTasks);

        Task hardTask = hardTasks.get(0);
        Players.setTask(playerStringUUID, hardTask.getName());
        Players.setHard(playerStringUUID, true);

        FileConfiguration config = plugin.getConfig();

        Location rewardsLocation = new Location(getServer().getWorld("world"), config.getInt("rewardCoords.x"), config.getInt("rewardCoords.y"), config.getInt("rewardCoords.z"));

        ItemStack bookItem = new ItemStack(Material.BOOK);
        ItemStack writtenBookItem = new ItemStack(Material.WRITTEN_BOOK);
        Item droppedBook = player.getWorld().dropItem(rewardsLocation, writtenBookItem);
        droppedBook.setPickupDelay(Integer.MAX_VALUE);

        BookMeta bookMeta = (BookMeta) writtenBookItem.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setTitle(ChatColor.GOLD + player.getName() + "'s Secret Task");
            bookMeta.setAuthor("The Firebush");

            // Add pages with the task details
            String taskPage = ChatColor.BOLD + hardTask.getName() + "\n\n" +
                    ChatColor.RESET + hardTask.getTask();
            bookMeta.addPage(taskPage);

            // Apply the meta to the item
            writtenBookItem.setItemMeta(bookMeta);
        }

        World world = getServer().getWorld("world");
        new BukkitRunnable() {
            int ticks = 0;
            int countdown = 40;
            Item droppedWrittenBook;

            @Override
            public void run() {
                if (ticks == 0) {
                    droppedBook.setGravity(false);
                }
                droppedBook.setVelocity(new org.bukkit.util.Vector());

                if (droppedWrittenBook != null) {
                    droppedWrittenBook.setVelocity(new Vector());
                }

                if (ticks == 10) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
                }
                // Particles for burning effect
                if (ticks > 10 && ticks < 30) {
                    player.spawnParticle(Particle.FLAME, droppedBook.getLocation().add(0, 0.5, 0), 10, 0.1, 0.1, 0.1, 0.02);
                    player.spawnParticle(Particle.SMOKE, droppedBook.getLocation().add(0, 0.5, 0 ), 5, 0.1, 0.1, 0.1, 0.02);
                }
                if (ticks == 20) {
                    droppedBook.remove();
                }

                if (ticks > 40) {
                    // Generate particles from random points far away and move them closer to the reward spot
                    int particleCount = 10; // Number of particles per tick

                    // Create directional particles moving toward the reward location
                    for (int i = 0; i < 10; i++) { // Emit 10 particles per tick
                        double radius = (countdown * 5) / 40; // Distance from the center where particles originate
                        double angle = random.nextDouble() * 2 * Math.PI;

                        // Random starting point around the target
                        double x = rewardsLocation.getX() + radius * Math.cos(angle);
                        double y = rewardsLocation.getY() + random.nextDouble() * 1.5; // Random height
                        double z = rewardsLocation.getZ() + radius * Math.sin(angle);

                        Location startLocation = new Location(world, x, y, z);

                        // Calculate directional vector toward the center
                        Vector direction = rewardsLocation.toVector().subtract(startLocation.toVector()).normalize();

                        // Spawn directional particle
                        world.spawnParticle(Particle.LARGE_SMOKE,
                                startLocation, // Starting position
                                0,            // Count: 0 to enable directional behavior
                                direction.getX(), direction.getY(), direction.getZ(), // Directional vector
                                (countdown / 40));         // Speed: Adjust for a more dramatic effect
                    }

                    countdown--;
                }

                if (ticks == 80) {
                    droppedWrittenBook = player.getWorld().dropItem(rewardsLocation, writtenBookItem);
                    droppedWrittenBook.setPickupDelay(Integer.MAX_VALUE);
                    droppedWrittenBook.setGravity(false);
                }

                ticks++;
                if (ticks > 90) {
                    droppedWrittenBook.setPickupDelay(0);
                    droppedWrittenBook.setOwner(player.getUniqueId());
                        droppedWrittenBook.setVelocity(new Vector(0, -0.1, 0)); // Add a slow downward motion

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Runs every 2 ticks (0.1 second intervals)


        return true;
    }
}
