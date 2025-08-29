package me.home4.firebush.firebush.commands.session;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.classes.Task;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.files.SessionManager;
import me.home4.firebush.firebush.files.TaskManager;
import me.home4.firebush.firebush.files.amongus.KillCooldownManager;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.home4.firebush.firebush.files.TaskManager.givePlayerTask;
import static me.home4.firebush.firebush.Firebush.cooldownManager;

public class StartSessionCommand implements CommandExecutor {

    private final Firebush plugin;
    private final FileConfiguration config;

    public StartSessionCommand(Firebush plugin) {

        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    private Random random = new Random();


    public void startCountdown(Player player) {
        new CountdownTask(player).runTaskTimer(plugin, 0, 20); // Run every second (20 ticks)
    }

    private void chooseTargets() {
        ArrayList<String> alivePlayers = Players.getAlivePlayers();
        Collections.shuffle(alivePlayers);
        System.out.println(alivePlayers);
        for (int i = 0; i < alivePlayers.size() - 1; i++) {
            String uuid = alivePlayers.get(i);
            String target = alivePlayers.get(i + 1);
            Players.get().set(uuid + ".target", target);
            //ActionBar.setActionBarMessage(UUID.fromString(uuid), ChatColor.LIGHT_PURPLE + Players.get().getString(target + ".nick"));
        }

        String lastPlayerUUID = alivePlayers.get(alivePlayers.size() - 1);
        String lastTargetUUID = alivePlayers.get(0);

        Players.get().set(lastPlayerUUID + ".target", lastTargetUUID);
        //ActionBar.setActionBarMessage(UUID.fromString(lastPlayerUUID), ChatColor.LIGHT_PURPLE + Players.get().getString(lastTargetUUID + ".nick"));
        Players.save();
    }

    private class CountdownTask extends BukkitRunnable {
        private final Player player;
        private int countdown = 3; // Initial countdown value

        public CountdownTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            FileConfiguration config = plugin.getConfig();
            if (countdown > 0) {
                // Display countdown with color based on the value
                ChatColor color = ChatColor.GREEN;

                if (countdown == 2) {
                    color = ChatColor.YELLOW;
                } else if (countdown == 1) {
                    color = ChatColor.RED;
                }

                player.sendTitle("", color + Integer.toString(countdown), 10, 20, 10);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 1.0f);

                countdown--;
            } else {
                // Countdown complete
                player.sendTitle("", ChatColor.LIGHT_PURPLE + "Target Selected", 10, 20, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);

                if (config.getBoolean("assassins")) {
                    chooseTargets();
                }

                cancel(); // Stop the task
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] arg) {

        HashMap<String, Task> playerTaskMap = new HashMap<>();
        System.out.println("Start Session");

        if (config.getBoolean("quests")) {
            List<Task> tasks = new TaskManager().getSessionTasks(SessionManager.getSession());
            System.out.println(tasks);
            ArrayList<String> nonRedPlayers = Players.getNonRedPlayers();
            ArrayList<String> alivePlayers = Players.getAlivePlayers();

            if (tasks.size() < nonRedPlayers.size()) {
                sender.sendMessage(ChatColor.RED + "Not enough tasks for all players. Ensure there are enough tasks in the session.");
                return false;
            }

            for (int i = 0; i < alivePlayers.size(); i++) {
                Task redTask = new TaskManager().getRandomRedTask();
                String playerUUID = alivePlayers.get(i);
                Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));

                if (player == null) {
                    continue;
                }
                if (Players.getLives(playerUUID) == 1) {
                    playerTaskMap.put(playerUUID, redTask);
                    TaskManager.givePlayerTask(player, playerTaskMap);
                }
            }

            // Shuffle players and tasks for random distribution
            Collections.shuffle(nonRedPlayers);
            Collections.shuffle(tasks);

            System.out.println("Shuffled stuff");

            // Map to keep track of which player has which task

            System.out.println(nonRedPlayers.size());
            System.out.println("Assign players tasks");

            if (nonRedPlayers.size() < 0) {
                sender.sendMessage(ChatColor.RED + "No Alive Players to play.");
                return false;
            }

            for (int i = 0; i < nonRedPlayers.size(); i++) {
                try {
                    String currentPlayer = nonRedPlayers.get(i);
                    Task assignedTask = tasks.get(i);

                    Players.setTask(currentPlayer, assignedTask);

                    System.out.println(currentPlayer);

                    // Assign the task to the current player and persist it
                    playerTaskMap.put(currentPlayer, assignedTask);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Failed to assign player a task: " + e.getMessage());
                }
            }

            cooldownManager.handleCooldowns();

        }

        for (Player player: Bukkit.getOnlinePlayers()) {
            if (!Players.getAlivePlayers().contains(player.getUniqueId().toString())) {
                continue;
            }
            if (config.getBoolean("assassins")) {
                startCountdown(player);
            }
            if (config.getBoolean("quests")) {
                givePlayerTask(player, playerTaskMap);
            }
        }

        for (String player: Players.getAlivePlayers()) {
            Players.get().set(player + ".gift", true);
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "SESSION STARTED");
        //for (int i = 0; i < 3; i++) {
        //    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        //    String message = String.valueOf(i);
        //    executorService.schedule(Firebush::broadcastMessage(message), 1, TimeUnit.SECONDS);
        //}

        return true;
    }

}
