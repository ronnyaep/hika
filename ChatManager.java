package fr.WarzouMc.hikabrain.manager;

import org.bukkit.event.EventHandler;
import fr.WarzouMc.hikabrain.manager.*;
import fr.WarzouMc.hikabrain.main.*;
import fr.WarzouMc.hikabrain.state.*;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager implements Listener {
    private final Main plugin;

    public ChatManager(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        String message = event.getMessage();
        
        // Récupérer les statistiques du joueur
        PlayerStats playerStats = plugin.getPointsManager().getPlayerStats(playerName);
        int playerPoints = playerStats.getPoints(); // Récupérer les points totaux

        // Format du message
        String format;
        if (plugin.getGameState() == GameState.PLAYING || 
            plugin.getGameState() == GameState.STARTING || 
            plugin.getGameState() == GameState.NEWPOINT) {
            
            // Joueur en partie
            if (plugin.getPlayerInGame().contains(playerName)) {
                boolean isBlueTeam = plugin.getPlayerInGame().get(0).equals(playerName);
                String teamColor = isBlueTeam ? "§3Bleu " : "§cRouge "; 
                format = String.format("§7[%d] %s%s§f: §b@§f%s", playerPoints, teamColor, playerName, message);
            } else {
                // Spectateur
                format = String.format("§7[Spectateur] §7%s§f: §7%s", playerName, message);
            }
        } else {
            // Hors partie
            format = String.format("§7[%d] §7%s§f: §f%s", playerPoints, playerName, message);
        }

        event.setFormat(format);
    }

}