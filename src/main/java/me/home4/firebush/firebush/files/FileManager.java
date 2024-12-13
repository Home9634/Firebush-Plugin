package me.home4.firebush.firebush.files;

import org.bukkit.Bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager {
    private static final File historyLogFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Firebush").getDataFolder(), "history_log.txt");

    public static void updateHistoryFile(String message) {

        message = message + "\n";

        if (!historyLogFile.exists()) {
            try {
                historyLogFile.createNewFile();
            } catch(IOException e) {
                System.out.println("Couldn't create file.");
            }
        }
        // Format the log entry

        // Write the log entry to the death log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyLogFile, true))) {
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
