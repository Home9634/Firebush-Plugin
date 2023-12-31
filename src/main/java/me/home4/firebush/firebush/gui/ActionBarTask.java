package me.home4.firebush.firebush.gui;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarTask extends BukkitRunnable {

    Firebush plugin;

    public ActionBarTask(Firebush plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        //Map<UUID, String> actionBarMessages = ActionBar.actionBarMessages;

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            String playerStringUUID = playerUUID.toString();
            String targetUUID = Players.get().getString(playerUUID + ".target");
            String targetName = Players.getNick(targetUUID);

            if (targetName == null) {
                targetName = "";
            }


            // Get the action bar message for the player
            String actionBarMessage = ChatColor.LIGHT_PURPLE + targetName;
            //System.out.println(actionBarMessage);

            // Send the action bar message to the player
            ActionBar.sendActionBar(player, actionBarMessage);
        }
    }
}
