package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.classes.Task;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.SessionManager;
import me.home4.firebush.firebush.files.TaskManager;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartSessionCommand implements CommandExecutor {

    private final Firebush plugin;
    private final FileConfiguration config;

    public StartSessionCommand(Firebush plugin) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    private Random random = new Random();


    public void startCountdown(Player player) {
        new CountdownTask(player).runTaskTimer(plugin, 0, 20); // Run every second (20 ticks)
    }

    private void chooseTargets() {
        ArrayList<String> alivePlayers = Players.getAlivePlayers();
        Collections.shuffle(alivePlayers);
        System.out.println(alivePlayers);
        for (int i = 0; i < alivePlayers.size() - 1; i++) {
            String uuid = alivePlayers.get(i);
            String target = alivePlayers.get(i + 1);
            Players.get().set(uuid + ".target", target);
            //ActionBar.setActionBarMessage(UUID.fromString(uuid), ChatColor.LIGHT_PURPLE + Players.get().getString(target + ".nick"));
        }

        String lastPlayerUUID = alivePlayers.get(alivePlayers.size() - 1);
        String lastTargetUUID = alivePlayers.get(0);

        Players.get().set(lastPlayerUUID + ".target", lastTargetUUID);
        //ActionBar.setActionBarMessage(UUID.fromString(lastPlayerUUID), ChatColor.LIGHT_PURPLE + Players.get().getString(lastTargetUUID + ".nick"));
        Players.save();
    }

    private class CountdownTask extends BukkitRunnable {
        private final Player player;
        private int countdown = 3; // Initial countdown value

        public CountdownTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            FileConfiguration config = plugin.getConfig();
            if (countdown > 0) {
                // Display countdown with color based on the value
                ChatColor color = ChatColor.GREEN;

                if (countdown == 2) {
                    color = ChatColor.YELLOW;
                } else if (countdown == 1) {
                    color = ChatColor.RED;
                }

                player.sendTitle("", color + Integer.toString(countdown), 10, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 1.0f);

                countdown--;
            } else {
                // Countdown complete
                player.sendTitle("", ChatColor.LIGHT_PURPLE + "Target Selected", 10, 20, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);

                if (config.getBoolean("assassins")) {
                    chooseTargets();
                }

                cancel(); // Stop the task
            }
        }
    }

    private void givePlayerTask(Player player, HashMap<String, Task> playerTasks) {

        ArrayList<String> alivePlayers = Players.getAlivePlayers();
        Task task = playerTasks.get(player.getUniqueId().toString());
        String taskDescription = task.getTask();
        HashMap<String, Task> similarTasks = new HashMap<>();

        // Regular expression to match {player_X}
        Pattern playerPattern = Pattern.compile("\\{player_(\\d+)\\}");
        Matcher playerMatcher = playerPattern.matcher(taskDescription);

        for (Map.Entry<String, Task> entry : playerTasks.entrySet()) {
            String tempUser = entry.getKey();
            Task tempTask = entry.getValue();

            if (tempTask.getName() == task.getName()) {
                 similarTasks.put(tempUser, tempTask);
            }
        }

        // Loop through all matches and replace them with actual player names
        while (playerMatcher.find()) {
            // Extract the number or identifier inside {player_X}
            String playerId = playerMatcher.group(1);

            String playerUUID = "";

            for (Map.Entry<String, Task> entry : playerTasks.entrySet()) {
                String tempUser = entry.getKey();
                Task tempTask = entry.getValue();

                if (tempTask.getId() == Integer.parseInt(playerId)) {
                    playerUUID = tempUser;
                }
            }


            if (alivePlayers.contains(playerUUID)) {
                String playerName = Players.getNick(playerUUID); // Get the player's name from the list
                taskDescription = taskDescription.replaceAll("\\{player_" + playerId + "}", playerName);
            } else {
                // Handle cases where the index is out of range (e.g., log a warning)
                System.out.println("Warning: Invalid player index " + playerId);
            }
        }
        if (taskDescription.contains("{owner}")) {
            taskDescription = taskDescription.replaceAll("\\{owner}", player.getName());
        }

        Players.setTask(player.getUniqueId().toString(), task.getName());

        // Step 1: Get the player's eye location and the direction they are facing
        Location eyeLocation = player.getEyeLocation(); // This gives us the eye-level location
        Vector direction = eyeLocation.getDirection();  // This gives the direction the player is looking

        // Step 2: Calculate the location one block in front of the player
        Location spawnLocation = eyeLocation.add(direction.multiply(1)); // 1 block ahead

        // Step 3: Spawn the book item at that location
        ItemStack bookItem = new ItemStack(Material.BOOK);
        ItemStack writtenBookItem = new ItemStack(Material.WRITTEN_BOOK);
        Item droppedBook = player.getWorld().dropItem(spawnLocation.add(0, -0.5, 0), bookItem);
        droppedBook.setPickupDelay(Integer.MAX_VALUE);

        BookMeta bookMeta = (BookMeta) writtenBookItem.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setTitle(ChatColor.GOLD + player.getName() + "'s Secret Task");
            bookMeta.setAuthor("The Firebush");

            // Add pages with the task details
            String taskPage = ChatColor.BOLD + task.getName() + "\n\n" +
                    ChatColor.RESET + taskDescription;
            bookMeta.addPage(taskPage);

            // Apply the meta to the item
            writtenBookItem.setItemMeta(bookMeta);
        }

        new BukkitRunnable() {
            int ticks = 0;
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

                if (ticks == 20) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
                }
                // Particles for burning effect
                if (ticks > 20 && ticks < 40) {
                    player.spawnParticle(Particle.FLAME, droppedBook.getLocation().add(0, 0.5, 0), 10, 0.1, 0.1, 0.1, 0.02);
                    player.spawnParticle(Particle.SMOKE, droppedBook.getLocation().add(0, 0.5, 0 ), 5, 0.1, 0.1, 0.1, 0.02);
                }
                if (ticks == 35) {
                    droppedBook.remove();
                    droppedWrittenBook = player.getWorld().dropItem(spawnLocation.add(0, -0.5, 0), writtenBookItem);
                    droppedWrittenBook.setPickupDelay(Integer.MAX_VALUE);
                    droppedWrittenBook.setGravity(false);
                }

                ticks++;
                if (ticks > 75) { // 1 second = 20 ticks
                    // Step 3: Remove the book and give the player a task
                    droppedWrittenBook.remove();
                    if (player.getInventory().firstEmpty() == -1) {
                        // Inventory is full
                        Item droppedItem = player.getWorld().dropItem(player.getLocation(), writtenBookItem);
                        droppedItem.setPickupDelay(0);
                        droppedItem.setOwner(player.getUniqueId());
                        // droppedItem.setGravity(true);
                        droppedItem.setGravity(false); // Disable gravity
                        droppedItem.setVelocity(new Vector(0, -0.1, 0)); // Add a slow downward motion
                    } else {
                        // Inventory has space
                        player.getInventory().addItem(writtenBookItem);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Runs every 2 ticks (0.1 second intervals)

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        HashMap<String, Task> playerTaskMap = new HashMap<>();
        System.out.println("Start Session");

        if (config.getBoolean("quests")) {
            List<Task> tasks = new TaskManager().getSessionTasks(SessionManager.getSession());
            System.out.println(tasks);
            ArrayList<String> alivePlayers = Players.getAlivePlayers();

            if (tasks.size() < alivePlayers.size()) {
                sender.sendMessage(ChatColor.RED + "Not enough tasks for all players. Ensure there are enough tasks in the session.");
                return false;
            }

            // Shuffle players and tasks for random distribution
            Collections.shuffle(alivePlayers);
            Collections.shuffle(tasks);

            System.out.println("Shuffled stuff");

            // Map to keep track of which player has which task

            System.out.println(alivePlayers.size());
            System.out.println("Assign players tasks");

            if (alivePlayers.size() < 0) {
                sender.sendMessage(ChatColor.RED + "No Alive Players to play.");
                return false;
            }

            for (int i = 0; i < alivePlayers.size(); i++) {
                try {
                    String currentPlayer = alivePlayers.get(i);
                    Task assignedTask = tasks.get(i);

                    System.out.println(currentPlayer);

                    // Assign the task to the current player and persist it
                    playerTaskMap.put(currentPlayer, assignedTask);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Failed to assign player a task: " + e.getMessage());
                }
            }
        }

        for (Player player: Bukkit.getOnlinePlayers()) {
            if (config.getBoolean("assassins")) {
                startCountdown(player);
            }
            if (config.getBoolean("quests")) {
                givePlayerTask(player, playerTaskMap);
            }
        }

        for (String player: Players.getAlivePlayers()) {
            Players.get().set(player + ".gift", true);
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "SESSION STARTED");
        //for (int i = 0; i < 3; i++) {
        //    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        //    String message = String.valueOf(i);
        //    executorService.schedule(Firebush::broadcastMessage(message), 1, TimeUnit.SECONDS);
        //}

        return true;
    }

}
