package me.home4.firebush.firebush.gui;

import me.home4.firebush.firebush.files.Players;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class LifeScoreboard {

    private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();;
    public static void updatePlayerTeam(String stringUUID) {

        UUID uuid = UUID.fromString(stringUUID);
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        // Remove player from existing teams
        for (Team team : scoreboard.getTeams()) {
            team.removeEntry(player.getName());
        }

        // Assign player to the appropriate team based on lives
        Team team = getOrCreateTeam(scoreboard, Players.getColor(stringUUID));
        team.addEntry(player.getName());

        // Update player's display name color
        player.setDisplayName(team.getColor() + player.getName());
    }

    public static Team getOrCreateTeam(Scoreboard scoreboard, ChatColor color) {
        Team team = scoreboard.getTeam(color.name());
        if (team == null) {
            team = scoreboard.registerNewTeam(color.name());
            team.setColor(color);
        }
        return team;
    }


}
