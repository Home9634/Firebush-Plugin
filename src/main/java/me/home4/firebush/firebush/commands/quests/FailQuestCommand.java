package me.home4.firebush.firebush.commands.quests;

import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class FailQuestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage("You're not allowed to use this command manually!");
            return true;
        }

        String playerNick = sender.getName();
        String playerStringUUID = Players.getUUIDfromNick(playerNick);
        Player player = Bukkit.getPlayer(UUID.fromString(playerStringUUID));

        if (playerStringUUID.equals("")) {
            player.sendMessage("You're not participating in the game!");
            return true;
        }

        if (Players.get().getString(playerStringUUID + ".task").equals("")) {
            player.sendMessage("You don't have a quest to fail!");
            return true;
        }

        PlayerInventory inventory = player.getInventory();
        TaskManager.removeTask(playerStringUUID);

        Boolean hardtask = Players.get().getBoolean(playerStringUUID + ".hardtask");
        int penalty = 0;
        int maxHealth = (int) player.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (Players.get().getBoolean(playerStringUUID + ".hardtask")) {
            penalty = 20;
        }
        

        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Math.max(maxHealth - penalty, 0));
        player.setHealth(Math.max(0, maxHealth - penalty));

        Players.setTask(playerStringUUID, "");

        // Play animation signifying that the player failed.

        // Temporary sanity check
        player.sendMessage("You have failed your quest. The Firebush is not pleased.");



        return true;
    }
}
