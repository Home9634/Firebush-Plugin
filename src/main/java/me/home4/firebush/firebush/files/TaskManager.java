package me.home4.firebush.firebush.files;

import me.home4.firebush.firebush.classes.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

public class TaskManager {

    private class TaskWrapper {
        private List<Task> tasks;

        private List<Task> hardtasks;

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

    }

    private final List<Task> tasks;

    private final List<Task> hardtasks;

    public TaskManager() {

        this.tasks = loadTasks("tasks");
        this.hardtasks = loadTasks("hardtasks");
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
            List<ItemStack> itemList = (List<ItemStack>) Players.get().getList(uuid + ".inventory");
            if (itemList != null) {

                System.out.println(itemList.size());
                for (int i = 0; i < itemList.size() - 1; i++) {
                    ItemStack item = itemList.get(i);
                    System.out.println(item);

                    // Check if the item is a written book
                    if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                        BookMeta bookMeta = (BookMeta) item.getItemMeta();
                        System.out.println(bookMeta);
                        System.out.println(bookMeta.getTitle());

                        // Match the title of the book
                        if (bookMeta != null && bookMeta.getTitle().contains("Secret Task")) {
                            System.out.println("Delete book");
                            itemList.set(i, null); // Remove the book from the inventory
                        }
                    }
                }

                Players.get().set(uuid + ".inventory", itemList);
                Players.save();
            }
        }


    }
}
