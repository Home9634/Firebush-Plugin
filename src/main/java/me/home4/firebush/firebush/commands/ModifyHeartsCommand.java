package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ModifyHeartsCommand implements CommandExecutor {
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
        int heartsMod = 0;

        if (args.length < 2) {
            return true;
        }

        if (!isNumeric(args[1])) {
            return true;
        } else {
            heartsMod = Integer.parseInt(args[1]);
        }

        String playerName = args[0];
        playerUUID = Players.getUUIDfromNick(playerName);

        if (playerUUID == null) {
            if (sender instanceof Player) {
                sender.sendMessage("Invalid player name chosen");
            }
            return true;
        }

        player = Bukkit.getPlayer(UUID.fromString(playerUUID));
        int currentHealth = (int) player.getAttribute(Attribute.MAX_HEALTH).getValue();
        int newHealth = currentHealth + heartsMod;

        System.out.println(newHealth);

        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newHealth);
        player.setHealth(newHealth);

        return true;
    }
}
