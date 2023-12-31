package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimTargetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();

            if (Players.get().getString(playerUUID + ".target").length() == 0) {
                player.sendMessage("You didn't even have a target to kill silly. Rolling eyes emoji");
                return true;
            }

            Players.get().set(playerUUID + ".target", "");
            Players.get().set(playerUUID + ".extraLives", Players.get().getInt(playerUUID + ".extraLives") + 1);
            Players.save();

            player.sendMessage("Target claimed! The Firebush is pleased.");

        }

        return true;
    }
}
