package fr.WarzouMc.hikabrain.commands;

import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.manager.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class LeaderboardCommand implements CommandExecutor {
    private final Main plugin;

    public LeaderboardCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            List<Map.Entry<String, PlayerStats>> leaderboard = plugin.getPointsManager().getLeaderboard();
            sender.sendMessage("§6Classement des joueurs :");
            for (int i = 0; i < leaderboard.size(); i++) {
                Map.Entry<String, PlayerStats> entry = leaderboard.get(i);
                sender.sendMessage(String.format("§e%d. %s - %d points", i + 1, entry.getKey(), entry.getValue().getPoints()));
            }
            return true;
        }
        return false;
    }
}