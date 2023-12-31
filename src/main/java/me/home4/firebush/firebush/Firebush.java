package me.home4.firebush.firebush;

import me.home4.firebush.firebush.commands.*;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.gui.ActionBarTask;
import me.home4.firebush.firebush.listeners.AllListeners;
import org.bukkit.Bukkit;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class Firebush extends JavaPlugin {

    private static Firebush plugin;
    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Firebush started.");

        plugin = this;

        getServer().getPluginManager().registerEvents(new AllListeners(this), this);
        getCommand("resetlives").setExecutor(new ResetLivesCommand());
        getCommand("setlives").setExecutor(new SetLivesCommand());
        getCommand("die").setExecutor(new DieCommand());
        getCommand("startsession").setExecutor(new StartSessionCommand(this));
        getCommand("refreshseason").setExecutor(new RefreshSeasonCommand(this));

        getConfig().options().copyDefaults();
        plugin.saveDefaultConfig();

        Players.setup();
        Players.get().options().copyDefaults(true);
        Players.save();

        BukkitTask actionBarTask = new ActionBarTask(this).runTaskTimer(this, 0L, 20L);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Wow plugin stopped that's so sad hit that like button.");
    }

    public static Firebush getPlugin() {
        return plugin;
    }

}
