package fr.WarzouMc.hikabrain.graphic.scoreboard;

import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

public class ScoreboardManager {
    private final Main plugin;
    private FileConfiguration config;
    private File configFile;
    private boolean separatorsEnabled;
    private String topSeparator;
    private String bottomSeparator;
    private List<String> titleAnimation;
    private int animationSpeed;
    private String separator;
    private boolean animationEnabled;

    public ScoreboardManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void addGameStats(Map<String, String> placeholders, Player player) {
        int kills = plugin.getKills().getOrDefault(player.getName(), 0);
        int deaths = plugin.getDeaths().getOrDefault(player.getName(), 0);
        double kd = deaths == 0 ? kills : (double) kills / deaths;
        
        placeholders.put("kills", String.valueOf(kills));
        placeholders.put("deaths", String.valueOf(deaths));
        placeholders.put("kd", String.format("%.2f", kd));
        placeholders.put("timer", plugin.getFormattedGameTime());
    }

    
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadSettings();
    }

    private void loadSettings() {
        ConfigurationSection scoreboard = config.getConfigurationSection("scoreboard");
        if (scoreboard == null) return;

        // Chargement des séparateurs
        ConfigurationSection separators = scoreboard.getConfigurationSection("separators");
        if (separators != null) {
            this.separatorsEnabled = separators.getBoolean("enabled", true);
            this.topSeparator = separators.getString("top", "");
            this.bottomSeparator = separators.getString("bottom", "");
        }

        separator = scoreboard.getString("separator", "");
    }

    public List<String> getScoreboardLines(GameState state, Player player, Map<String, String> placeholders) {
        List<String> lines = new ArrayList<>();
        if (separatorsEnabled) {
            lines.add(colorize(topSeparator));
        }
        if(state == GameState.PLAYING) {
            addGameStats(placeholders, player);
        }

        // Chargement des lignes selon l'état
        String path = "scoreboard." + state.name().toLowerCase() + ".lines";
        List<String> configLines = config.getStringList(path);

        // Ajout des placeholders spéciaux pour les points
        if (plugin.getPlayerInGame().size() == 2) {
            Player bluePlayer = Bukkit.getPlayer(plugin.getPlayerInGame().get(0));
            Player redPlayer = Bukkit.getPlayer(plugin.getPlayerInGame().get(1));

            if (bluePlayer != null && redPlayer != null) {
                placeholders.put("blue_points", String.valueOf(plugin.getPoint().get(bluePlayer.getName())));
                placeholders.put("red_points", String.valueOf(plugin.getPoint().get(redPlayer.getName())));
            }
        }

        for (String line : configLines) {
            // Remplacement des placeholders
            String processedLine = line.replace("\\n", "\n");
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                processedLine = processedLine.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            lines.addAll(Arrays.asList(colorize(processedLine).split("\n")));
        }

        // Ajout du séparateur inférieur si activé
        if (separatorsEnabled) {
            lines.add(colorize(bottomSeparator));
        }

        return lines;
    }


    public String getNextAnimationFrame(int currentFrame) {
        if (!animationEnabled || titleAnimation == null || titleAnimation.isEmpty()) {
            return colorize(config.getString("scoreboard.title", "&6&lHikaBrain"));
        }
        return colorize(titleAnimation.get(currentFrame % titleAnimation.size()));
    }

    public int getAnimationSpeed() {
        return animationSpeed;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    private String colorize(String text) {
        if (text == null) return "§f";
        if (text.trim().isEmpty()) return "§f";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}