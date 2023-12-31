package me.home4.firebush.firebush.files;

import me.home4.firebush.firebush.gui.LifeScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
            return ChatColor.LIGHT_PURPLE;
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
            System.out.println(key);
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
            customFile.set(key, null);
        }

        Players.save();
    }

    public static void definePlayer(Player player) {
        String playerUUID = player.getUniqueId().toString();

        Players.get().set(playerUUID + ".lives", 3);
        Players.setNick(playerUUID, player.getName());
        Players.get().set(playerUUID + ".target", "");
        Players.get().set(playerUUID + ".extraLives", 0);
        Players.get().set(playerUUID + ".isPink", false);

        Players.setLifeColor(playerUUID);
    }
}
