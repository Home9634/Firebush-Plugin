package me.home4.firebush.firebush.files;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.classes.Task;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.home4.firebush.firebush.Firebush.cooldownManager;

public class TaskManager {

    private class TaskWrapper {
        private List<Task> tasks;

        private List<Task> hardtasks;

        private List<Task> redtasks;

        public List<Task> getTasks() {
            return tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }

        public List<Task> getHardtasks() {return hardtasks;}

        public void setHardtasks(List<Task> tasks) {
            this.hardtasks = tasks;
        }

        public List<Task> getRedTasks() {return redtasks;}

        public void setRedTasks(List<Task> tasks) {
            this.redtasks = tasks;
        }

    }

    private final List<Task> tasks;

    private final List<Task> hardtasks;
    private final List<Task> redtasks;

    private final HashMap<String, Task> sessionTasks;

    public TaskManager() {

        this.tasks = loadTasks("tasks");
        this.hardtasks = loadTasks("hardtasks");
        this.redtasks = loadTasks("redtasks");
        this.sessionTasks = new HashMap<>();
    }

    private List<Task> loadTasks(String property) {
        try (Reader reader = new FileReader(Bukkit.getServer().getPluginManager().getPlugin("Firebush").getDataFolder() + "/tasks.json")) { // Ensure this path points to your tasks.json file
            Gson gson = new Gson();
            TaskWrapper taskWrapper = gson.fromJson(reader, TaskWrapper.class);

            if (property == "tasks") {
                if (taskWrapper != null && taskWrapper.getTasks() != null) {
                    return taskWrapper.getTasks();
                } else {
                    Bukkit.getLogger().severe("No tasks found in tasks.json or tasks array is null.");
                    return new java.util.ArrayList<>(); // Return an empty list if the tasks array is empty or null
                }
            } else if (property == "hardtasks") {
                if (taskWrapper != null && taskWrapper.getHardtasks() != null) {
                    return taskWrapper.getHardtasks();
                } else {
                    Bukkit.getLogger().severe("No tasks found in tasks.json or tasks array is null.");
                    return new java.util.ArrayList<>(); // Return an empty list if the tasks array is empty or null
                }
            } else if (property == "redtasks") {
                if (taskWrapper != null && taskWrapper.getRedTasks() != null) {
                    return taskWrapper.getRedTasks();
                } else {
                    Bukkit.getLogger().severe("No tasks found in tasks.json or tasks array is null.");
                    return new java.util.ArrayList<>(); // Return an empty list if the tasks array is empty or null
                }
            } else {
                return new java.util.ArrayList<>(); // Return an empty list if the tasks array is empty or null
            }


        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load tasks.json: " + e.getMessage());
            return new java.util.ArrayList<>(); // Return an empty list in case of an error
        }
    }

    public Task getRandomTask() {
        if (tasks.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return tasks.get(random.nextInt(tasks.size()));
    }

    public List<Task> getSessionTasks(int session) {
        try {
            if (tasks.isEmpty()) {
                return null;
            }

            List<Task> sessionTasks = new ArrayList<Task>();
            for (Task task : tasks) {
                if (task.getSession() == session) {
                    sessionTasks.add(task);
                }
            }

            System.out.println(sessionTasks);

            Collections.shuffle(sessionTasks);

            System.out.println("Finished getting session tasks");
            return sessionTasks;
        } catch( Exception e  ) {
            Bukkit.getLogger().severe("Failed to get session tasks: " + e.getMessage());
            return new java.util.ArrayList<>(); // Return an empty list in case of an error
        }
    }

    public List<Task> getHardTasks(int session) {
        try {
            if (hardtasks.isEmpty()) {
                return null;
            }

            List<Task> sessionTasks = new ArrayList<Task>();
            for (Task task : hardtasks) {
                if (session >= task.getSession()) {
                    sessionTasks.add(task);
                }

            }

            Collections.shuffle(sessionTasks);

            System.out.println("Finished getting session tasks");
            return sessionTasks;
        } catch( Exception e  ) {
            Bukkit.getLogger().severe("Failed to get session tasks: " + e.getMessage());
            return new java.util.ArrayList<>(); // Return an empty list in case of an error
        }
    }

    public List<Task> getRedTasks() {
        try {
            if (redtasks.isEmpty()) {
                return null;
            }

            List<Task> tempRedTasks = new ArrayList<Task>();
            for (Task task: redtasks) {
                tempRedTasks.add(task);
            }

            Collections.shuffle(tempRedTasks);
            return tempRedTasks;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to get red tasks: " + e.getMessage());
            return new java.util.ArrayList<>(); // Return an empty list in case of an error
        }
    }

    public Task getRandomRedTask() {
        if (redtasks.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return redtasks.get(random.nextInt(redtasks.size()));
    }

    public static void givePlayerTask(Player player, HashMap<String, Task> playerTasks) {

        ArrayList<String> alivePlayers = Players.getAlivePlayers();
        ArrayList<String> otherPlayers = Players.getAlivePlayers();
        otherPlayers.remove(alivePlayers.indexOf(player.getUniqueId().toString()));
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

                if (Objects.equals(tempTask.getName(), task.getName()) && tempTask.getId() == Integer.parseInt(playerId)) {
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
        if (taskDescription.contains("{random}")) {
            Collections.shuffle(otherPlayers);
            taskDescription = taskDescription.replaceAll("\\{random}", Players.getNick(otherPlayers.get(0)));
        }


        Players.setTask(player.getUniqueId().toString(), task);

        // Step 1: Get the player's eye location and the direction they are facing
        Location eyeLocation = player.getEyeLocation(); // This gives us the eye-level location
        org.bukkit.util.Vector direction = eyeLocation.getDirection();  // This gives the direction the player is looking

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

            // Define page limits for a Minecraft book (255 characters per page in Minecraft 1.21.3)
            final int PAGE_LIMIT = 255;

            // Add the task name to the first page
            String taskHeader = ChatColor.BOLD + task.getName() + "\n\n" + ChatColor.RESET;

            // Combine the header with the task description
            String fullText = taskHeader + taskDescription;

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

        System.out.println("Task Name: " + task.getName());
        if (Objects.equals(task.getName(), "Imposter")) {
            if (player.isOnline()) {
                cooldownManager.giveInitialWeapon(player);
            }
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
                    droppedWrittenBook.setVelocity(new org.bukkit.util.Vector());
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
        }.runTaskTimer(Firebush.getPlugin(), 0L, 2L); // Runs every 2 ticks (0.1 second intervals)

    }


    public static void removeTask(String uuid) {

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

        if (offlinePlayer.isOnline()) {
            // Online player - directly access inventory
            Player onlinePlayer = offlinePlayer.getPlayer(); // Guaranteed to be non-null for online players
            if (onlinePlayer != null) {

                PlayerInventory inventory = onlinePlayer.getInventory();
                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack item = inventory.getItem(i);
                    System.out.println(item);

                    // Check if the item is a written book
                    if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                        BookMeta bookMeta = (BookMeta) item.getItemMeta();
                        System.out.println(bookMeta);
                        System.out.println(bookMeta.getTitle());

                        // Match the title of the book
                        if (bookMeta != null && bookMeta.getTitle().contains("Secret Task")) {
                            System.out.println("Delete book");
                            inventory.setItem(i, null); // Remove the book from the inventory
                        }
                    }
                }
            }
        } else {
            // Load the inventory from Players.yml
//            List<ItemStack> itemList = (List<ItemStack>) Players.get().getList(uuid + ".inventory");
//            if (itemList != null) {
//
//                System.out.println(itemList.size());
//                for (int i = 0; i < itemList.size() - 1; i++) {
//                    ItemStack item = itemList.get(i);
//                    System.out.println(item);
//
//                    // Check if the item is a written book
//                    if (item != null && item.getType() == Material.WRITTEN_BOOK) {
//                        BookMeta bookMeta = (BookMeta) item.getItemMeta();
//                        System.out.println(bookMeta);
//                        System.out.println(bookMeta.getTitle());
//
//                        // Match the title of the book
//                        if (bookMeta != null && bookMeta.getTitle().contains("Secret Task")) {
//                            System.out.println("Delete book");
//                            itemList.set(i, null); // Remove the book from the inventory
//                        }
//                    }
//                }
//
//                Players.get().set(uuid + ".inventory", itemList);
//                Players.save();
//            }
        }


    }
}
