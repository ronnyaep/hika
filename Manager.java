package fr.WarzouMc.hikabrain.manager;

import fr.WarzouMc.hikabrain.gameLoop.NewPoint;
import fr.WarzouMc.hikabrain.gameLoop.StartLoop;
import fr.WarzouMc.hikabrain.graphic.scoreboard.Creater;
import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.graphic.scoreboard.ObjectiveSign;
import fr.WarzouMc.hikabrain.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Map;

public class Manager implements Listener {

    private Main main;
    public Manager(Main main) {this.main = main;}

    private Location spawn;

    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    Team team = null;

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String playerName = player.getName();

        spawn = new Location(player.getLocation().getWorld(), 0.5, 70.0, 0.5);

        player.teleport(spawn);
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setHealth(20);
        player.setBedSpawnLocation(spawn);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        if(main.getPlayerInGame().size() > 1){
            for (int i = 0; i < main.getPlayerInGame().size(); i++) {
                Player pls = Bukkit.getPlayer(main.getPlayerInGame().get(i));
                pls.hidePlayer(player);
            }

            player.sendTitle("§cThe game is already", "§eStart");
            player.setGameMode(GameMode.SPECTATOR);
        }else{
            addPlayer(playerName);
            player.setGameMode(GameMode.ADVENTURE);
        }

        if(main.getPlayerInGame().size() == 2 && main.getGameState() == GameState.WAITING){
            main.setGameState(GameState.STARTING);
            StartLoop startLoop = new StartLoop(main);
            startLoop.runTaskTimer(main, 0, 1);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (main.getPlayerInGame().contains(playerName)) {
            rvmPlayer(playerName); // Retirer le joueur de la liste des joueurs en jeu

            // Déterminer le gagnant
            String winnerName = main.getPlayerInGame().get(0).equals(playerName) ? 
                                main.getPlayerInGame().get(1) : 
                                main.getPlayerInGame().get(0);
            
            // Annonce de la victoire
            main.setWinner(winnerName);
            Bukkit.broadcastMessage("§6" + winnerName + " §fa gagné par §cabandon §fde §e" + playerName + "§f!");

            // Gérer la victoire
            handleWin(Bukkit.getPlayer(winnerName), winnerName, isPlayerBlue(winnerName), getGamePlayers());

            // Réinitialiser le jeu
            resetGame();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Maintien des besoins vitaux
        maintainVitals(player);

        // Gestion des états spéciaux
        if (main.getGameState() == GameState.NEWPOINT) {
            main.initPlayer(player);
            return;
        }

        // Récupération des joueurs et scores
        GamePlayers gamePlayers = getGamePlayers();
        if (gamePlayers == null) return;

        // Gestion de la mort par chute
        if (isDeathByFall(player)) {
            handleFallDeath(player);
            return;
        }

        // Gestion des points
        handleScoring(player, playerName, gamePlayers);
    }

    private void handleFallDeath(Player victim) {
        if (victim.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent lastDamage = (EntityDamageByEntityEvent) victim.getLastDamageCause();
            if (lastDamage.getDamager() instanceof Player) {
                Player killer = (Player) lastDamage.getDamager();
                handleKill(killer, victim, true); // Avec message "poussé dans le vide"
            }
        } else {
            // Suicide - pas de killer
            main.getDeaths().put(victim.getName(), main.getDeaths().getOrDefault(victim.getName(), 0) + 1);
        }
        main.initPlayer(victim);
    }

    private boolean isPlayerBlue(String playerName) {
        return main.getPlayerInGame().get(0).equals(playerName);
    }
    
    private void maintainVitals(Player player) {
        player.setSaturation(20);
        player.setFoodLevel(20);
    }

    private GamePlayers getGamePlayers() {
        if (main.getGameState() == GameState.WAITING || main.getGameState() == GameState.STARTING) {
            return null;
        }
        
        return new GamePlayers(
            Bukkit.getPlayer(main.getPlayerInGame().get(0)),
            Bukkit.getPlayer(main.getPlayerInGame().get(1)),
            main.getPoint()
        );
    }

    private boolean isDeathByFall(Player player) {
        return main.getGameState() == GameState.PLAYING && player.getLocation().getBlockY() < 50;
    }

    private void handleScoring(Player player, String playerName, GamePlayers gamePlayers) {
        if (main.getGameState() != GameState.PLAYING) return;
        
        Block block = player.getLocation().subtract(0, 1, 0).getBlock();
        if (block.getType() != Material.WOOL) return;

        boolean isBlueTeam = main.getPlayerInGame().get(0).equalsIgnoreCase(playerName);
        byte woolData = block.getData();
        
        if ((isBlueTeam && woolData == 14) || (!isBlueTeam && woolData == 11)) {
            addPoint(player, playerName, isBlueTeam, gamePlayers);
            
            // Envoyer un message lorsque le point est marqué
            String teamColor = isBlueTeam ? "§3Bleu" : "§cRouge";
            Bukkit.broadcastMessage("⚔ "+ teamColor + " " + playerName + " §7a marqué un point !");
        }
    }

    private void addPoint(Player player, String playerName, boolean isBlueTeam, GamePlayers gamePlayers) {
        Map<String, Integer> points = main.getPoint();
        points.replace(playerName, points.get(playerName) + 1);
        main.setPoint(points);

        if (points.get(playerName) == 5) {
            handleWin(player, playerName, isBlueTeam, gamePlayers);
        } else {
            goal();
        }
    }

    private void handleWin(Player player, String playerName, boolean isBlueTeam, GamePlayers gamePlayers) {
        main.setGameState(GameState.WINNING);
        Player loser = getOpponent(player);

        if (loser != null) {
            main.getPointsManager().addGameStats(
                playerName, 
                loser.getName(),
                main.getKills(),
                main.getDeaths()
            );
            loser.setGameMode(GameMode.SPECTATOR);
            announceGameStats(player, loser);
        }

        player.setAllowFlight(true);
        player.setFlying(true);
        main.setWinner(playerName);
        win();

        // Ne pas changer le scoreboard ici
        // Le scoreboard reste le même que pendant le jeu

        // Ne pas réinitialiser le scoreboard
        // Aucune action supplémentaire pour changer le scoreboard

        // Reset après 5 secondes
        Bukkit.getScheduler().runTaskLater(main, () -> {
            resetGame();
        }, 100 * 5);
    }
    
    private void resetGame() {
        // Réinitialiser le scoreboard de la partie
        resetScoreboard();

        // Kick tous les joueurs avec message
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§aPartie terminée! Merci d'avoir joué!");
        }

        // Reset des variables
        main.getPlayerInGame().clear();
        main.getPoint().clear();
        main.getKills().clear(); 
        main.getDeaths().clear();
        main.setWinner(null);
        main.setGameState(GameState.WAITING);
        main.initMap();

        // Sauvegarder les stats
        main.getPointsManager().saveStats();
    }
    
    private Player getOpponent(Player player) {
        if (main.getPlayerInGame().size() != 2) return null;
        String opponentName = main.getPlayerInGame().get(0).equals(player.getName()) 
            ? main.getPlayerInGame().get(1) 
            : main.getPlayerInGame().get(0);
        return Bukkit.getPlayer(opponentName);
    }

    private class GamePlayers {
        final Player bluePlayer;
        final Player redPlayer;
        private final Map<String, Integer> points;

        GamePlayers(Player bluePlayer, Player redPlayer, Map<String, Integer> points) {
            this.bluePlayer = bluePlayer;
            this.redPlayer = redPlayer;
            this.points = points;
        }

        int getBluePoints() {
            return points.get(bluePlayer.getName());
        }

        int getRedPoints() {
            return points.get(redPlayer.getName());
        }
    }

    private void goal() {
        main.setGameState(GameState.NEWPOINT);
        
        // Créer une nouvelle instance de NewPoint
        NewPoint newPoint = new NewPoint(main);
        newPoint.runTaskTimer(main, 0, 20); // Exécuter toutes les secondes (20 ticks)
        
        // Initialiser les joueurs
        main.initPlayer(Bukkit.getPlayer(main.getPlayerInGame().get(0)));
        main.initPlayer(Bukkit.getPlayer(main.getPlayerInGame().get(1)));
        
        // Initialiser la carte
        main.initMap();
    }

    private void win(){
        for (int x = -21; x < 22; x++) {
            for (int z = -7; z < 8; z++){
                Bukkit.getWorlds().get(0).getBlockAt(x, 69, z).setType(Material.AIR);
            }
        }
    }
    
    private void handleKill(Player killer, Player victim, boolean isVoidKill) {
        if (killer != null) {
            // Stats update
            main.getKills().put(killer.getName(), main.getKills().getOrDefault(killer.getName(), 0) + 1);
            main.getDeaths().put(victim.getName(), main.getDeaths().getOrDefault(victim.getName(), 0) + 1);
            
            // Kill sound
            killer.playSound(killer.getLocation(), "entity.experience_orb.pickup", 1f, 1f); // Son de kill

            // Kill message
            String killerColor = isPlayerBlue(killer.getName()) ? "§3" : "§c";
            String victimColor = isPlayerBlue(victim.getName()) ? "§3" : "§c";
            
            String message = isVoidKill ?
                String.format("⚔ %s%s §7a poussé %s%s §7dans le vide", killerColor, killer.getName(), victimColor, victim.getName()) :
                String.format("⚔ %s%s §7a tué %s%s", killerColor, killer.getName(), victimColor, victim.getName());
                
            Bukkit.broadcastMessage(message); // Envoi du message à tous les joueurs
        }
    }

    private void announceGameStats(Player winner, Player loser) {
        PlayerStats winnerStats = main.getPointsManager().getPlayerStats(winner.getName());
        PlayerStats loserStats = main.getPointsManager().getPlayerStats(loser.getName());

        Bukkit.broadcastMessage("§8§m--------------------------------");
        Bukkit.broadcastMessage("§6§l          Fin de partie!");
        Bukkit.broadcastMessage("§8§m--------------------------------");
        
        String winnerColor = isPlayerBlue(winner.getName()) ? "§3" : "§c";
        String loserColor = isPlayerBlue(loser.getName()) ? "§3" : "§c";

        Bukkit.broadcastMessage(winnerColor + winner.getName());
        Bukkit.broadcastMessage("§7Kills: §f" + main.getKills().get(winner.getName()));
        Bukkit.broadcastMessage("§7Points totaux: §6" + winnerStats.getPoints());
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(loserColor + loser.getName());
        Bukkit.broadcastMessage("§7Kills: §f" + main.getKills().get(loser.getName()));
        Bukkit.broadcastMessage("§7Points totaux: §6" + loserStats.getPoints());
        Bukkit.broadcastMessage("§8§m--------------------------------");
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (main.getGameState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        Player victim = (Player) event.getEntity();
        if (victim.getHealth() - event.getDamage() <= 0) {
            event.setCancelled(true);
            
            // Trouver le dernier joueur qui a frappé la victime
            Player killer = null;
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    killer = (Player) damager;
                }
            }
            
            // Mettre à jour les stats
            if (killer != null) {
                handleKill(killer, victim, false); // Appel à handleKill pour gérer le kill
            }
            main.initPlayer(victim);
        }
    }

    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {
        if (main.getGameState() != GameState.PLAYING) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player victim = (Player) event.getEntity();
        
        if (victim.getHealth() - event.getDamage() <= 0) {
            event.setCancelled(true);
            
            // Trouver le dernier joueur qui a frappé la victime
            Player killer = null;
            if (event instanceof EntityDamageByEntityEvent) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    killer = (Player) damager;
                }
            }
            
            // Mettre à jour les stats
            if (killer != null) {
                main.getKills().put(killer.getName(), main.getKills().getOrDefault(killer.getName(), 0) + 1);
            }
            main.getDeaths().put(victim.getName(), main.getDeaths().getOrDefault(victim.getName(), 0) + 1);
            
            main.initPlayer(victim);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        if(block.getType() != Material.SANDSTONE || main.getGameState() != GameState.PLAYING){
            event.setCancelled(true);
            return;
        }
        if(blockLocation.getZ() >= -1 && blockLocation.getZ() <= 1 && blockLocation.getY() >= 59){
            event.setCancelled(blockLocation.getX() >= 19 || blockLocation.getX() <= -19);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        if(block.getType() != Material.SANDSTONE || main.getGameState() != GameState.PLAYING){
            event.setCancelled(true);
            return;
        }
        if(blockLocation.getZ() >= -1 && blockLocation.getZ() <= 1 && blockLocation.getY() >= 59){
            event.setCancelled(blockLocation.getX() >= 19 || blockLocation.getX() <= -19);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    private void rvmPlayer(String playerName) {
        List<String> playerInGame = main.getPlayerInGame();
        Map<String, Integer> point = main.getPoint();

        Creater creater = new Creater(main);
        creater.destroy(Bukkit.getPlayer(playerName));

        playerInGame.remove(playerName);
        point.remove(playerName);
        main.setPlayerInGame(playerInGame);


    }
    
    private void resetScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ObjectiveSign objective = main.getBoard().get(player);
            if (objective != null) {
                // Effacer toutes les lignes du scoreboard
                for (int i = 0; i < 15; i++) {
                    objective.setLine(i, "");
                }
                objective.updateLines(); // Mettre à jour le scoreboard pour le joueur
            }
        }
    }

    private void handlePlayerDeath(Player victim, Player killer) {
        if (killer != null) {
            main.getKills().put(killer.getName(), main.getKills().getOrDefault(killer.getName(), 0) + 1);
            killer.playSound(killer.getLocation(), "entity.experience_orb.pickup", 1f, 1f);
            
            String killerColor = isPlayerBlue(killer.getName()) ? "§3" : "§c";
            String victimColor = isPlayerBlue(victim.getName()) ? "§3" : "§c";
            
            // Message unifié pour tous les types de kills
            String killMessage = String.format("⚔ %s%s §7a tué %s%s", 
                killerColor, killer.getName(), 
                victimColor, victim.getName());
            Bukkit.broadcastMessage(killMessage);
        }
        
        main.getDeaths().put(victim.getName(), main.getDeaths().getOrDefault(victim.getName(), 0) + 1);
        main.initPlayer(victim);
    }
    
    private void addPlayer(String playerName) {
        List<String> playerInGame = main.getPlayerInGame();
        Map<String, Integer> point = main.getPoint();

        Creater creater = new Creater(main);
        creater.create(Bukkit.getPlayer(playerName));

        playerInGame.add(playerName);
        point.put(playerName, 0);
        main.setPlayerInGame(playerInGame);
        Bukkit.getPlayer(playerName).setPlayerListName("§7" + playerName);
        Bukkit.getPlayer(playerName).setDisplayName("§7" + playerName + "§r");

        if(scoreboard.getTeam(playerName) == null){
            team = scoreboard.registerNewTeam(playerName);
        }else{
            team = scoreboard.getTeam(playerName);
        }

        team.setPrefix("§7");
        team.addPlayer(Bukkit.getPlayer(playerName));
    }


}
