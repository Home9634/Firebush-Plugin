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

import java.util.*;

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
        String oldTask = Players.getTask(playerStringUUID).getName();

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

        if (Players.getLives(playerStringUUID) == 1) {
            player.sendMessage("Red lives can't reroll quests!");
            return true;
        }

        TaskManager.removeTask(playerStringUUID);

        Collections.shuffle(hardTasks);

        Task hardTask = hardTasks.get(0);
        Players.setTask(playerStringUUID, hardTask);
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

            // Define page limits for a Minecraft book (255 characters per page in Minecraft 1.21.3)
            final int PAGE_LIMIT = 255;

            // Add the task name to the first page
            String taskHeader = ChatColor.BOLD + hardTask.getName() + "\n\n" + ChatColor.RESET;

            // Combine the header with the task description
            String fullText = taskHeader + hardTask.getTask();

            System.out.println("Full Text Length" + fullText.length());

            // Split the full text into pages
            List<String> pages = new ArrayList<>();
            while (fullText.length() > PAGE_LIMIT) {
                int splitIndex = PAGE_LIMIT;

                // Avoid cutting off mid-word
                while (splitIndex > 0 && fullText.charAt(splitIndex) != ' ') {
                    splitIndex--;
                }
                if (splitIndex == 0) {
                    splitIndex = PAGE_LIMIT; // If no spaces found, force a split
                }

                // Add the split text to the pages list
                System.out.println(fullText);
                pages.add(fullText.substring(0, splitIndex).trim());
                fullText = fullText.substring(splitIndex).trim(); // Remaining text
            }

            // Add the last chunk as the final page
            pages.add(fullText);

            // Add all pages to the book
            bookMeta.setPages(pages);

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
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1, 1);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
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
                    player.getWorld().playSound(droppedBook.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
                    if (ticks % 5 == 0) {
//                        player.getWorld().playSound(droppedBook.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
                    }
                    player.spawnParticle(Particle.FLAME, droppedBook.getLocation().add(0, 0.5, 0), 10, 0.1, 0.1, 0.1, 0.02);
                    player.spawnParticle(Particle.SMOKE, droppedBook.getLocation().add(0, 0.5, 0 ), 5, 0.1, 0.1, 0.1, 0.02);
                }
                if (ticks == 20) {
                    player.getWorld().playSound(droppedBook.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1);
                    player.getWorld().playSound(droppedBook.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
                    player.getWorld().playSound(droppedBook.getLocation(), Sound.BLOCK_SMOKER_SMOKE, 0.5f, 1);
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
                    player.getWorld().playSound(rewardsLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
                    player.getWorld().playSound(rewardsLocation, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
                    droppedWrittenBook = player.getWorld().dropItem(rewardsLocation, writtenBookItem);
                    droppedWrittenBook.setPickupDelay(Integer.MAX_VALUE);
                    droppedWrittenBook.setGravity(false);
                }

                ticks++;
                if (ticks > 90) {
                    player.getWorld().playSound(droppedWrittenBook.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 1, 1);

                    droppedWrittenBook.setPickupDelay(0);
                    droppedWrittenBook.setOwner(player.getUniqueId());
                        droppedWrittenBook.setVelocity(new Vector(0, -0.1, 0)); // Add a slow downward motion

                    cancel();
                }

                if (ticks == 95) {
                    player.getWorld().playSound(droppedWrittenBook.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 2);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Runs every 2 ticks (0.1 second intervals)


        return true;
    }
}
