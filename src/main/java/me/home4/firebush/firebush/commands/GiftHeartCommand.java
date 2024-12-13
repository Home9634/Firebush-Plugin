package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.home4.firebush.firebush.Firebush.plugin;

public class GiftHeartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }

        if (args.length == 1) {

            Player gifter = (Player) sender;

            if (plugin.getConfig().getBoolean("giftHearts")) {
                gifter.sendMessage("Gifting hearts is disabled!");
                return true;
            }

            if (!Players.get().getBoolean(gifter.getUniqueId() + ".gift")) {
                gifter.sendMessage("You have already used your gift!");
                return true;
            }

            String uuid = Players.getUUIDfromNick(args[0]);
            if (uuid == null) {
                gifter.sendMessage("That is not a valid player!");
                return true;
            }

            if (uuid.equals(gifter.getUniqueId().toString())) {
                gifter.sendMessage("You cannot gift yourself!");
                return true;
            }

            Player player = Bukkit.getPlayer(UUID.fromString(uuid));
            if (!player.isOnline()) {
                gifter.sendMessage("That player is not online!");
                return true;
            }
            int maxHearts = plugin.getConfig().getInt("uhcMaxHearts");
            int currentHearts = (int) player.getAttribute(Attribute.MAX_HEALTH).getValue();
            if (currentHearts + 2 > maxHearts) {
                gifter.sendMessage("That player has too many hearts!");
                return true;
            }

            player.sendTitle("", ChatColor.GREEN + "You recieved a heart from " + Players.getNick(gifter.getUniqueId().toString()), 10, 50, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 1.0f);

            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentHearts + 2);
            Players.get().set(uuid + ".gift", false);

        }

        Players.save();

        //System.out.println("Player " + args[0] + " has been excluded from Firebush.");

        return true;
    }
}
