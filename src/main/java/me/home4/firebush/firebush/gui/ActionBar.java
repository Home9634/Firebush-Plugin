package me.home4.firebush.firebush.gui;

import me.home4.firebush.firebush.files.Players;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBar {

    static Map<UUID, String> actionBarMessages = new HashMap<>();

    public static void sendActionBar(Player player, String message) {

        BaseComponent[] text = TextComponent.fromLegacyText(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, text);
    }

    public static void setActionBarMessage(UUID playerUUID, String message) {

        actionBarMessages.put(playerUUID, message);
    }



}
