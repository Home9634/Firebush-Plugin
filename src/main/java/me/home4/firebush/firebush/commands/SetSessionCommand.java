package me.home4.firebush.firebush.commands;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.SessionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetSessionCommand  implements CommandExecutor {

    private final Firebush plugin;
    private final FileConfiguration config;

    public SetSessionCommand(Firebush plugin) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 1) {
            Integer newSessionCount = Integer.parseInt(args[0]);
            System.out.println(newSessionCount);
            if (newSessionCount == 0) {
                return true;
            }

            SessionManager.setSession(newSessionCount);

            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage("Session set to " + args[0]);
            }

            System.out.println("Session set to " + args[0]);
        }

        return true;
    }
}
