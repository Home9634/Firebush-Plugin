package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.files.FileManager;
import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ClaimTargetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerUUID = player.getUniqueId().toString();
            String targetUUID = Players.get().getString(playerUUID + ".target");

            if (targetUUID.length() == 0) {
                player.sendMessage("You didn't even have a target to kill silly. Rolling eyes emoji");
                return true;
            }

            Player target = Bukkit.getPlayer(UUID.fromString(targetUUID));

            Players.get().set(playerUUID + ".target", "");
            Players.get().set(playerUUID + ".extraLives", Players.get().getInt(playerUUID + ".extraLives") + 1);
            Players.save();

            player.sendMessage("Target claimed! The Firebush is pleased.");
            Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.LIGHT_PURPLE + "" + ChatColor.LIGHT_PURPLE + " has successfully killed " + target.getDisplayName());

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            FileManager.updateHistoryFile(timestamp + " " + player.getDisplayName() + " has claimed their target");

        }

        return true;
    }
}
