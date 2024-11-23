package fr.WarzouMc.hikabrain.manager;

import org.bukkit.configuration.file.FileConfiguration;
import fr.WarzouMc.hikabrain.manager.*;
import fr.WarzouMc.hikabrain.main.*;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.*;

public class PointsManager {
    private final Main plugin;
    private File statsFile;
    private FileConfiguration statsConfig;
    private Map<String, PlayerStats> playerStats;

    public PointsManager(Main plugin) {
        this.plugin = plugin;
        this.playerStats = new HashMap<>();
        loadStats();
    }

    private void loadStats() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            plugin.saveResource("stats.yml", false);
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        // Charger les stats
        if (statsConfig.contains("players")) {
            for (String playerName : statsConfig.getConfigurationSection("players").getKeys(false)) {
                String path = "players." + playerName;
                PlayerStats stats = new PlayerStats(
                    statsConfig.getInt(path + ".wins", 0),
                    statsConfig.getInt(path + ".losses", 0),
                    statsConfig.getInt(path + ".kills", 0),
                    statsConfig.getInt(path + ".deaths", 0),
                    statsConfig.getInt(path + ".points", 0)
                );
                playerStats.put(playerName, stats);
            }
        }
    }

    public void saveStats() {
        for (Map.Entry<String, PlayerStats> entry : playerStats.entrySet()) {
            String path = "players." + entry.getKey();
            PlayerStats stats = entry.getValue();
            
            statsConfig.set(path + ".wins", stats.getWins());
            statsConfig.set(path + ".losses", stats.getLosses());
            statsConfig.set(path + ".kills", stats.getKills());
            statsConfig.set(path + ".deaths", stats.getDeaths());
            statsConfig.set(path + ".points", stats.getPoints());
        }

        try {
            statsConfig.save(statsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addGameStats(String winner, String loser, Map<String, Integer> gameKills, Map<String, Integer> gameDeaths) {
        // Points par action
        final int WIN_POINTS = 60; // Points pour la victoire
        final int LOSS_POINTS = 15; // Points pour la défaite
        final int KILL_POINTS = 5; // Points par kill

        // Winner stats
        PlayerStats winnerStats = getOrCreateStats(winner);
        winnerStats.addWin();
        winnerStats.addPoints(WIN_POINTS);
        winnerStats.addKills(gameKills.getOrDefault(winner, 0));
        winnerStats.addDeaths(gameDeaths.getOrDefault(winner, 0));
        winnerStats.addPoints(gameKills.getOrDefault(winner, 0) * KILL_POINTS);

        // Loser stats
        PlayerStats loserStats = getOrCreateStats(loser);
        loserStats.addLoss();
        loserStats.addPoints(LOSS_POINTS); // Ajouter des points pour la défaite
        loserStats.addKills(gameKills.getOrDefault(loser, 0));
        loserStats.addDeaths(gameDeaths.getOrDefault(loser, 0));
        loserStats.addPoints(gameKills.getOrDefault(loser, 0) * KILL_POINTS);

        saveStats();
    }

    private PlayerStats getOrCreateStats(String playerName) {
        return playerStats.computeIfAbsent(playerName, k -> new PlayerStats());
    }

    public List<Map.Entry<String, PlayerStats>> getLeaderboard() {
        List<Map.Entry<String, PlayerStats>> leaderboard = new ArrayList<>(playerStats.entrySet());
        leaderboard.sort((a, b) -> b.getValue().getPoints() - a.getValue().getPoints());
        return leaderboard;
    }

    public PlayerStats getPlayerStats(String playerName) {
        return playerStats.getOrDefault(playerName, new PlayerStats());
    }
}