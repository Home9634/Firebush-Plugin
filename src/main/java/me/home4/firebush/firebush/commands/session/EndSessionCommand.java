package me.home4.firebush.firebush.commands.session;

import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EndSessionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        for (String uuid: Players.getAlivePlayers()) {
            Players.get().set(uuid + ".target", "");
            Players.get().set(uuid + ".task", "");
            TaskManager.removeTask(uuid);

        }

        Players.save();
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "SESSION ENDED");

        return true;
    }
}
