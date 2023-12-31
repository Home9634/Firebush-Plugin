package me.home4.firebush.firebush.listeners;

import me.home4.firebush.firebush.Firebush;
import me.home4.firebush.firebush.files.Players;
import me.home4.firebush.firebush.gui.ActionBar;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.w3c.dom.events.Event;

import java.util.Objects;
import java.util.UUID;

public class AllListeners implements Listener {

    private final Firebush plugin;

    public AllListeners(Firebush plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // event.setJoinMessage("Welcome to Element 79~");
        Player player = event.getPlayer();

        String playerUUID = player.getUniqueId().toString();
        if (!Players.get().contains(playerUUID)) {
            Players.definePlayer(player);
        } else {
            Players.get().set(playerUUID + ".nick", player.getName());
        }

        Players.save();

        Players.setLifeColor(playerUUID);
        //ActionBar.setActionBarMessage(player.getUniqueId(), "Tree");

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        String playerUUID = player.getUniqueId().toString();

        Players.modifyLives(playerUUID, -1);
        Players.setLifeColor(playerUUID);

        Player attacker = (Player) player.getKiller();

        System.out.println(attacker);
        
        if (attacker != null) {
            String attackerUUID = attacker.getUniqueId().toString();
            System.out.println(attackerUUID);
            System.out.println(playerUUID);
            if (Objects.equals(Players.get().getString(attackerUUID + ".target"), playerUUID)) {
                System.out.println("Successfully killed target.");
                attacker.sendMessage("Successfully killed your target.");
                Players.get().set(attackerUUID + ".extraLives", Players.get().getInt(attackerUUID + ".extraLives") + 1);
                Players.get().set(attackerUUID + ".target", "");
                ActionBar.setActionBarMessage(UUID.fromString(attackerUUID), "");
                Players.save();
            }
        }

        if (Players.getLives(playerUUID) == 0) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }



}
