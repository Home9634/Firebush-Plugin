package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class StartSessionCommand implements CommandExecutor {

    private final Firebush plugin;

    public StartSessionCommand(Firebush plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("assassins")) {
            ArrayList<String> alivePlayers = Players.getAlivePlayers();
            Collections.shuffle(alivePlayers);
            System.out.println(alivePlayers);
            for (int i = 0; i < alivePlayers.size() - 1; i++) {
                String uuid = alivePlayers.get(i);
                String target = alivePlayers.get(i + 1);
                Players.get().set(uuid + ".target", target);
                ActionBar.setActionBarMessage(UUID.fromString(uuid), ChatColor.LIGHT_PURPLE + Players.get().getString(target + ".nick"));
            }

            String lastPlayerUUID = alivePlayers.get(alivePlayers.size() - 1);
            String lastTargetUUID = alivePlayers.get(0);

            Players.get().set(lastPlayerUUID + ".target", lastTargetUUID);
            ActionBar.setActionBarMessage(UUID.fromString(lastPlayerUUID), ChatColor.LIGHT_PURPLE + Players.get().getString(lastTargetUUID + ".nick"));
            Players.save();
        }


        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "SESSION STARTED");

        return true;
    }
}
