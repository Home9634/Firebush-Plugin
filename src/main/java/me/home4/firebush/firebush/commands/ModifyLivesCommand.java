package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModifyLivesCommand implements CommandExecutor {

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            return true;
        }

        Player player;
        String playerUUID;
        int livesMod = 0;

        if (args.length < 2) {
            return true;
        }

        if (!isNumeric(args[1])) {
            return true;
        } else {
            livesMod = Integer.parseInt(args[1]);
        }

        String playerName = args[0];
        playerUUID = Players.getUUIDfromNick(playerName);

        if (playerUUID == null) {
            if (sender instanceof Player) {
                sender.sendMessage("Invalid player name chosen");
            }
            return true;
        }

        int currentLives = Players.getLives(playerUUID);
        int newLives = currentLives + livesMod;

        //player = Bukkit.getPlayer(playerUUID);

        Players.setLives(playerUUID, newLives);
        Players.setLifeColor(playerUUID);

        return true;
    }
}
