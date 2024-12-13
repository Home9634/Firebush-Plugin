package me.home4.firebush.firebush.files;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.gui.LifeScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Players {

    private static File file;
    private static FileConfiguration customFile;

    //Finds or generates the custom Config file
    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Firebush").getDataFolder(), "players.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                System.out.println("Couldn't create file.");
            }
        }

        customFile = YamlConfiguration.loadConfiguration(file);

    }

    public static FileConfiguration get() {
        return customFile;
    }

    public static void save() {
        try {
            customFile.save(file);
        } catch(IOException e) {
            System.out.println("Couldn't save file.");
        }
    }

    public static void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    int defaultLives = 3;

    public static int getLives(String uuid) {
        return customFile.getInt(uuid + ".lives");
    }

    public static void setLives(String uuid, int lives) {
        customFile.set(uuid + ".lives", lives);
        Players.save();
    }

    public static ChatColor getColor(String uuid) {
        int lives = customFile.getInt(uuid + ".lives");
        if (lives > 3) {
            return ChatColor.BLUE;
        } else if (lives == 3) {
            return ChatColor.GREEN;
        } else if (lives == 2) {
            return ChatColor.YELLOW;
        } else if (lives == 1 && customFile.getBoolean(uuid + ".isPink")) {
            return ChatColor.RED;
        } else if (lives == 1) {
            return ChatColor.RED;
        } else if (lives == 0) {
            return ChatColor.GRAY;
        }
        return ChatColor.GRAY;
    }

    public static void modifyLives(String uuid, int livesModify) {
        int lives = customFile.getInt(uuid + ".lives");
        lives += livesModify;

        if (lives < 1 && customFile.getInt(uuid + ".extraLives") > 0) {
            lives = 1;
            customFile.set(uuid + ".extraLives", customFile.getInt(uuid + ".extraLives") - 1);
            customFile.set(uuid + ".isPink", true);
        }

        if (lives < 0) {
            lives = 0;
        }

        customFile.set(uuid + ".lives", lives);
        Players.save();

    }

    public static String getNick(String uuid) {
        return Players.get().getString(uuid + ".nick");
    }

    public static void resetLives() {

        for (String key: customFile.getKeys(false)) {
            Players.setLives(key,3);
            Players.setLifeColor(key);
        }

        Players.save();

    }

    public static void setNick(String uuid, String nick) {
        customFile.set(uuid + ".nick", nick);
        Players.save();
    }

    public static String getUUIDfromNick(String nick) {
        for (String key: customFile.getKeys(false)) {
            System.out.println("Key: " + key);
            if (customFile.getString(key + ".nick").equalsIgnoreCase(nick)) {
                return key;
            }
        }
        return null;
    }

    public static void setLifeColor(String stringUUID) {
        ChatColor color = ChatColor.BLACK;
        UUID uuid = UUID.fromString(stringUUID);
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        color = Players.getColor(stringUUID);
        player.setPlayerListName(color + player.getName());
        player.setDisplayName(color + player.getName());

        LifeScoreboard.updatePlayerTeam(stringUUID);
    }

    public static ArrayList<String> getAlivePlayers() {
        ArrayList<String> alivePlayers = new ArrayList<>();

        for (String key: customFile.getKeys(false)) {
            if (Players.getLives(key) > 0) {
                alivePlayers.add(key);
            }
        }

        return alivePlayers;
    }

    public static void clear() {
        for (String key: customFile.getKeys(false)) {
            if (!customFile.getBoolean(key + ".excluded")) {
                customFile.set(key, null);
            }
        }

        Players.save();
    }

    public static void setTask(String uuid, String taskName) {

        Players.get().set(uuid + ".task", taskName);
        Players.get().set(uuid + ".hardtask", false);
        Players.save();
    }
    public static void setHard(String uuid, Boolean isHard) {
        Players.get().set(uuid + ".hardtask", isHard);
        Players.save();
    }

    public static void saveInventory(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerInventory inventory = player.getInventory();
        List<ItemStack> items = new ArrayList<>();

        // Loop through inventory and save non-armor slots
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= 36 && i <= 39) {
                continue; // Skip armor slots
            }
            items.add(inventory.getItem(i));
        }

        // Save the inventory to the YAML file
        get().set(uuid + ".inventory", items);
        save();
    }

    public static void loadInventory(Player player) {
        String uuid = player.getUniqueId().toString();

        // Retrieve inventory from the YAML file
        List<?> itemList = get().getList(uuid + ".inventory");
        if (itemList != null) {
            PlayerInventory inventory = player.getInventory();
            inventory.clear(); // Clear current inventory

            int index = 0;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (i >= 36 && i <= 39) {
                    continue; // Skip armor slots
                }

                if (index < itemList.size()) {
                    ItemStack item = (ItemStack) itemList.get(index);
                    inventory.setItem(i, item);
                    index++;
                }
            }
        }
    }

    public static void clearInventory(String uuid) {
        get().set(uuid + ".inventory", null);
        save();
    }
    public static void definePlayer(Player player) {
        String playerUUID = player.getUniqueId().toString();

        Players.get().set(playerUUID + ".lives", Firebush.getPlugin().getConfig().get("defaultLives"));
        Players.setNick(playerUUID, player.getName());
        Players.get().set(playerUUID + ".target", "");
        Players.get().set(playerUUID + ".extraLives", 0);
        Players.get().set(playerUUID + ".isPink", false);
        Players.get().set(playerUUID + ".maxHealth", Firebush.getPlugin().getConfig().get("uhcMaxHeart"));
        Players.get().set(playerUUID + ".hasTask", false);
        Players.get().set(playerUUID + ".task", "");
        Players.get().set(playerUUID + ".hardtask", false);
        Players.get().set(playerUUID + ".gifted", false);
        Players.get().set(playerUUID + ".excluded", false);
        Players.get().set(playerUUID + ".inventory", null);

        Players.setLifeColor(playerUUID);
    }
}
