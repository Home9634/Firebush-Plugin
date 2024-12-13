package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ExcludeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 1) {
            String uuid = Players.getUUIDfromNick(args[0]);
            if (uuid == null) {
                return true;
            }

            //Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            Players.get().set(uuid + ".lives", 0);
            Players.get().set(uuid + ".excluded", Boolean.parseBoolean(Players.get().getString("excluded")));
        }

        Players.save();

        System.out.println("Player " + args[0] + " has been excluded from Firebush.");

        return true;
    }
}
