package me.home4.firebush.firebush.commands.session;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RefreshSeasonCommand implements CommandExecutor {

    private final Firebush plugin;

    public RefreshSeasonCommand(Firebush plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        Players.clear();
        for (Player player: Bukkit.getOnlinePlayers()) {
            System.out.println(player.getName());
            String uuid = player.getUniqueId().toString();
            if (!Players.get().getBoolean(uuid + ".excluded")) {
                Players.definePlayer(player);
                player.setGameMode(GameMode.SURVIVAL);
            }

            if (plugin.getConfig().getBoolean("uhc")) {
                int maxHeart = plugin.getConfig().getInt("uhcMaxHearts");
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHeart);
                player.setHealth(maxHeart);
            }
        }

        return true;
    }
}
