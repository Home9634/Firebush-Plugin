package me.home4.firebush.firebush.files;

import me.home4.firebush.firebush.Firebush;
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

public class SessionManager {

    private static File file;
    private static FileConfiguration customFile;

    //Finds or generates the custom Config file
    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("Firebush").getDataFolder(), "session.yml");

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

    int session = 1;

    public static int getSession() {
        return customFile.getInt("session");
    }

    public static void setSession(int session) {
        customFile.set(".session", session);
        SessionManager.save();
    }


}
