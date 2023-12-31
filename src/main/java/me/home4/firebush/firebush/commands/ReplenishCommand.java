package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplenishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0) {
            for (Player player: Bukkit.getOnlinePlayers()) {
                player.setFoodLevel(20);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            }
        }

        if (args.length == 1) {
            Player player = (Player) Bukkit.getPlayer(Players.getUUIDfromNick(args[0]));
            player.setFoodLevel(20);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("Successfully replenished player(s)");
        }

        return true;
    }
}
