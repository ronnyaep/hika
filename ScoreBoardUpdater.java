package fr.WarzouMc.hikabrain.graphic.scoreboard;

import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreBoardUpdater extends BukkitRunnable {
    private final Main main;
    private final ScoreboardManager scoreboardManager;
    private int animationTick = 0;
    private int animationFrame = 0;
    private FileConfiguration config;
    private File configFile;
    private boolean separatorsEnabled;
    private String topSeparator;
    private String bottomSeparator;
    private List<String> titleAnimation;
    private int animationSpeed;
    private String separator;
    private boolean animationEnabled;

    public ScoreBoardUpdater(Main main) {
        this.main = main;
        this.scoreboardManager = new ScoreboardManager(main);
    }

    @Override
    public void run() {
        updateAnimation();
        updateScoreboards();
    }

    private void updateAnimation() {
        if (scoreboardManager.isAnimationEnabled()) {
            animationTick++;
            if (animationTick >= scoreboardManager.getAnimationSpeed()) {
                animationTick = 0;
                animationFrame++;
            }
        }
    }

    
    private void updateScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<String, String> timerPlaceholders = new HashMap<>();
            timerPlaceholders.put("timer", String.valueOf((main.getTimer() / 20)));
            
            if (main.getGameState() == GameState.STARTING) {
                timerPlaceholders.put("timer", String.valueOf((main.getTimer() / 20) + 1));
            }
            
            ObjectiveSign objective = main.getBoard().get(player);
            if (objective == null) continue;
            
            objective.setDisplayName(scoreboardManager.getNextAnimationFrame(animationFrame));
            
            Map<String, String> placeholders = createPlaceholders(player);
            placeholders.putAll(timerPlaceholders);  // Fusionner les placeholders
            
            List<String> lines = scoreboardManager.getScoreboardLines(main.getGameState(), player, placeholders);
            
            for (int i = 0; i < lines.size(); i++) {
            	objective.setLine(0, "");
            	objective.setLine(i + 1, lines.get(i)); // Décalage de 1 pour éviter le conflit
            }
            
            objective.updateLines();
        }
    }

    private Map<String, String> createPlaceholders(Player player) {
        Map<String, String> placeholders = new HashMap<>();
        
        // Placeholders communs
        placeholders.put("players", String.valueOf(main.getPlayerInGame().size()));
        
        // Placeholders spécifiques selon l'état
        Player opponent = getOpponent(player);
        if (opponent != null) {
            placeholders.put("opponent", getColoredName(opponent));
            placeholders.put("opponent_score", String.valueOf(main.getPoint().get(opponent.getName())));
            placeholders.put("player_score", String.valueOf(main.getPoint().get(player.getName())));
        }

        // Placeholders pour le timer
        placeholders.put("timer", String.valueOf(main.getTimer() / 20));

        // Placeholders pour la victoire
        if (main.getGameState() == GameState.WINNING) {
            int kills = main.getKills().getOrDefault(player.getName(), 0);
            int deaths = main.getDeaths().getOrDefault(player.getName(), 0);
            double kd = deaths == 0 ? kills : (double) kills / deaths;
            
            placeholders.put("kills", String.valueOf(kills));
            placeholders.put("deaths", String.valueOf(deaths));
            placeholders.put("kd", String.format("%.2f", kd));
            placeholders.put("timer", main.getFormattedGameTime());
            boolean isWinner = player.getName().equals(main.getWinner());
            placeholders.put("win_message", isWinner ? "§aVictoire !" : "§cDéfaite...");
            placeholders.put("winner", getColoredName(Bukkit.getPlayer(main.getWinner())));
        }

        return placeholders;
    }

    private Player getOpponent(Player player) {
        if (main.getPlayerInGame().size() != 2) return null;
        String opponentName = main.getPlayerInGame().get(0).equals(player.getName()) 
            ? main.getPlayerInGame().get(1) 
            : main.getPlayerInGame().get(0);
        return Bukkit.getPlayer(opponentName);
    }

    private String getColoredName(Player player) {
        if (player == null) return "§7Aucun";
        boolean isBlueTeam = main.getPlayerInGame().get(0).equals(player.getName());
        return (isBlueTeam ? "§3" : "§c") + player.getName();
    }
}