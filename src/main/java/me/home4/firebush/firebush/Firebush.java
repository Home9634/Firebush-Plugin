package me.home4.firebush.firebush;

import me.home4.firebush.firebush.commands.*;
import me.home4.firebush.firebush.commands.quests.ClaimQuestCommand;
import me.home4.firebush.firebush.commands.quests.FailQuestCommand;
import me.home4.firebush.firebush.commands.quests.RerollQuestCommand;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.SessionManager;
import me.home4.firebush.firebush.gui.ActionBarTask;
import me.home4.firebush.firebush.listeners.AllListeners;
import org.bukkit.Bukkit;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public final class Firebush extends JavaPlugin {

    public static Firebush plugin;


    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Firebush started.");
        plugin = this;

        // Ensure the data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Copy the tasks.json file if it doesn't exist
        File tasksJson = new File(getDataFolder(), "tasks.json");
        if (!tasksJson.exists()) {
            saveResource("tasks.json", false);
        }

        getServer().getPluginManager().registerEvents(new AllListeners(this), this);
        getCommand("resetlives").setExecutor(new ResetLivesCommand());
        getCommand("setlives").setExecutor(new SetLivesCommand());
        getCommand("die").setExecutor(new DieCommand());
        getCommand("startsession").setExecutor(new StartSessionCommand(this));
        getCommand("refreshseason").setExecutor(new RefreshSeasonCommand(this));
        getCommand("claimtarget").setExecutor(new ClaimTargetCommand());
        getCommand("replenish").setExecutor(new ReplenishCommand());
        getCommand("endsession").setExecutor(new EndSessionCommand());
        getCommand("exclude").setExecutor(new ExcludeCommand());
        getCommand("gift").setExecutor(new GiftHeartCommand());
        getCommand("setsession").setExecutor(new SetSessionCommand(this));

        getCommand("claimquest").setExecutor(new ClaimQuestCommand(this));
        getCommand("failquest").setExecutor(new FailQuestCommand());
        getCommand("rerollquest").setExecutor(new RerollQuestCommand());
        getCommand("modifyhearts").setExecutor(new ModifyHeartsCommand());



        // Load the config file, adding new defaults if necessary
        getConfig().options().copyDefaults(true);
//        addConfigDefaults();  // Add new config defaults here
        saveConfig(); // Save the config with any new defaults added

        Players.setup();
        Players.get().options().copyDefaults(true);
        Players.save();

        SessionManager.setup();
        SessionManager.get().options().copyDefaults(true);
        SessionManager.save();

        BukkitTask actionBarTask = new ActionBarTask(this).runTaskTimer(this, 0L, 20L);

        NamespacedKey key = new NamespacedKey(this, "tnt_cheap_recipe");
        ShapedRecipe customTNTRecipe = new ShapedRecipe(key, new ItemStack(Material.TNT));
        customTNTRecipe.shape("PSP", "SGS", "PSP");

        customTNTRecipe.setIngredient('P', Material.PAPER);
        customTNTRecipe.setIngredient('S', Material.GRAVEL);
        customTNTRecipe.setIngredient('G', Material.GUNPOWDER);

        getServer().addRecipe(customTNTRecipe);

    }

    private void addConfigDefaults() {
        // Add new configuration defaults here
        getConfig().addDefault("ghoul", false);
        getConfig().addDefault("assassins", false);
        getConfig().addDefault("blueLifeDecay", true);
        getConfig().addDefault("quests", false);
        getConfig().addDefault("uhc", false);
        getConfig().addDefault("uhcMinHearts", 0);
        getConfig().addDefault("uhcMaxHearts", 60);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Wow plugin stopped that's so sad hit that like button.");
    }

    public static void broadcastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

    public static Firebush getPlugin() {
        return plugin;
    }

}
