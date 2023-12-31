package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RefreshSeasonCommand implements CommandExecutor {

    private final Firebush plugin;

    public RefreshSeasonCommand(Firebush plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        Players.clear();
        for (Player player: Bukkit.getOnlinePlayers()) {
            System.out.println(player.getName());
            Players.definePlayer(player);

        }

        return true;
    }
}
