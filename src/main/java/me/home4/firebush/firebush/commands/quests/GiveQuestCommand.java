package me.home4.firebush.firebush.commands.quests;

import me.home4.firebush.firebush.classes.Task;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class GiveQuestCommand  implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 1) {
            String uuid = Players.getUUIDfromNick(args[0]);

            System.out.println(uuid);
            System.out.println(args[0]);

            if (uuid == null) {
                sender.sendMessage("That is not a valid player!");
                return true;
            }

            if (Players.getTask(uuid) == null) {
                sender.sendMessage("That player does not have a quest to give!");
                return true;
            }




            Player player = Bukkit.getPlayer(UUID.fromString(uuid));

            if (!player.isOnline()) {
                sender.sendMessage("That player is not online!");
                return true;
            }

            HashMap<String, Task> playerTasks = Players.getPlayerTasks();
            TaskManager.givePlayerTask(player, playerTasks);

            sender.sendMessage("Successfully given quest to player!");
        }

        return true;
    }
}
