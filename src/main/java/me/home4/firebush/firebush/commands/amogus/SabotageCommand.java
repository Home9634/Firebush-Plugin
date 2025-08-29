package me.home4.firebush.firebush.commands.amogus;

import me.home4.firebush.firebush.files.Players;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SabotageCommand implements CommandExecutor {

    private final Map<UUID, Map<Integer, Long>> sabotageCooldowns = new HashMap<>();
    private final long sabotageCooldownTime = 15 * 60 * 1000; // 15 minutes in milliseconds

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        String playerStringUUID = player.getUniqueId().toString();

        // Check if the player is an imposter
        if (!Objects.equals(Players.getTask(playerStringUUID).getName(), "Imposter")) {
            player.sendMessage(ChatColor.RED + "Only imposters can use this command.");
            return true;
        }

        if (args.length > 0) {
            int option = Integer.parseInt(args[1]);
            return executeSabotage(player, option);
        } else {
            player.sendMessage(ChatColor.GOLD + "--- Sabotage Options ---");

            sendClickableOption(player, 1, "Blindness", "Makes all players blind for 10 seconds.");
            sendClickableOption(player, 2, "Glowing", "Makes all players glow for 10 seconds.");
            sendClickableOption(player, 3, "Teleport", "Teleport all players with each other randomly.");

            return true;
        }

    }

    private void sendClickableOption(Player player, int option, String name, String description) {
        long remainingTime = getRemainingCooldown(player, option);
        ChatColor color = remainingTime > 0 ? ChatColor.RED : ChatColor.GREEN;
        String cooldownMessage = remainingTime > 0 ? " (Cooldown: " + formatCooldown(remainingTime) + ")" : "";

        TextComponent message = new TextComponent(color + name + ": " + ChatColor.GRAY + description + cooldownMessage);
        if (remainingTime <= 0) {
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sabotage execute " + option));
        }
        player.spigot().sendMessage(message);
    }

    public boolean executeSabotage(Player player, int option) {
        String playerStringUUID = player.getUniqueId().toString();

        // Check if the player is an imposter
        if (!Objects.equals(Players.getTask(playerStringUUID).getName(), "Imposter")) {
            player.sendMessage(ChatColor.RED + "Only imposters can use this command.");
            return false;
        }

        // Check cooldown
        if (isOnCooldown(player, option)) {
            long remainingTime = getRemainingCooldown(player, option);
            player.sendMessage(ChatColor.RED + "You cannot use this sabotage yet. Cooldown: " + formatCooldown(remainingTime));
            return false;
        }

        switch (option) {
            case 1: // Blindness
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!Objects.equals(Players.getTask(target.getUniqueId().toString()).getName(), "Imposter")) {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1)); // 10 seconds
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Blindness sabotage activated!");
                break;

            case 2: // Glowing
                for (Player target : Bukkit.getOnlinePlayers()) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1)); // 10 seconds
                }
                player.sendMessage(ChatColor.GREEN + "Glowing sabotage activated!");
                break;

            case 3:
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (players.size() < 2) {
                    player.sendMessage(ChatColor.RED + "Not enough players to shuffle teleport.");
                    break;
                }

                // Shuffle the list
                Collections.shuffle(players);

                // Save the original locations
                Map<Player, Location> originalLocations = new HashMap<>();
                for (Player target : players) {
                    originalLocations.put(target, target.getLocation());
                }

                // Teleport each player to the location of the next player in the shuffled list
                for (int i = 0; i < players.size(); i++) {
                    Player currentPlayer = players.get(i);
                    Player nextPlayer = players.get((i + 1) % players.size());
                    Location newLocation = originalLocations.get(nextPlayer);

                    currentPlayer.teleport(newLocation);
//                    currentPlayer.sendMessage(ChatColor.GOLD + "You have been shuffled!");
                }

                player.sendMessage(ChatColor.GREEN + "Shuffle Teleport sabotage activated!");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Invalid sabotage option. Use /sabotage to see available options.");
                return false;
        }

        // Start cooldown for this sabotage option
        startCooldown(player, option);
        return true;
    }

    private boolean isOnCooldown(Player player, int option) {
        UUID uuid = player.getUniqueId();
        sabotageCooldowns.putIfAbsent(uuid, new HashMap<>());
        return System.currentTimeMillis() < sabotageCooldowns.get(uuid).getOrDefault(option, 0L);
    }

    private void startCooldown(Player player, int option) {
        UUID uuid = player.getUniqueId();
        sabotageCooldowns.putIfAbsent(uuid, new HashMap<>());
        sabotageCooldowns.get(uuid).put(option, System.currentTimeMillis() + sabotageCooldownTime);
    }

    private long getRemainingCooldown(Player player, int option) {
        UUID uuid = player.getUniqueId();
        sabotageCooldowns.putIfAbsent(uuid, new HashMap<>());
        return Math.max(0, sabotageCooldowns.get(uuid).getOrDefault(option, 0L) - System.currentTimeMillis());
    }

    private String formatCooldown(long timeMillis) {
        long seconds = (timeMillis / 1000) % 60;
        long minutes = (timeMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
