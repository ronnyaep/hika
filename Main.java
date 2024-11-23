package fr.WarzouMc.hikabrain.main;

import fr.WarzouMc.hikabrain.graphic.scoreboard.ObjectiveSign;
import fr.WarzouMc.hikabrain.graphic.scoreboard.Creater;
import fr.WarzouMc.hikabrain.graphic.scoreboard.ScoreBoardUpdater;
import fr.WarzouMc.hikabrain.graphic.scoreboard.ScoreboardSign;
import fr.WarzouMc.hikabrain.manager.Manager;
import fr.WarzouMc.hikabrain.manager.*;
import fr.WarzouMc.hikabrain.state.GameState;
import org.bukkit.*;
import fr.WarzouMc.hikabrain.commands.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import fr.WarzouMc.hikabrain.state.GameState;
import java.util.*;
import java.util.Map;

public class Main extends JavaPlugin {
    private PointsManager pointsManager;
    private GameState gameState;
    private int gameTime = 0;
    private Map<String, PlayerStats> playerStats;
    private BukkitTask gameTimer;
    private Map<String, Integer> kills = new HashMap<>();
    private Map<String, Integer> deaths = new HashMap<>();
    private List<String> playerInGame = new ArrayList<>();
    
    public void startGame() {
        gameTime = 0;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        gameTimer = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                gameTime++;
            }
        }, 0L, 20L);
    }

    public void stopGame() {
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }

    public String getFormattedGameTime() {
        int minutes = gameTime / 60;
        int seconds = gameTime % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getGameTime() {
        return gameTime;
    }
    
    public void addKill(String player) {
        kills.put(player, kills.getOrDefault(player, 0) + 1);
    }
    
    public void addDeath(String player) {
        deaths.put(player, deaths.getOrDefault(player, 0) + 1);
    }
    
    
    // Getters
    public Map<String, Integer> getKills() { return kills; }
    public Map<String, Integer> getDeaths() { return deaths; }

    private List<String> playerOutGame = new ArrayList<>();
    private Map<String, Integer> point = new HashMap<>();
    private ScoreBoardUpdater scoreBoardUpdater;
    private Map<Player, ObjectiveSign> board = new HashMap<>(); 

    private int timer = 10;

    public String winner = null;

    @Override
    public void onEnable() {
        // Sauvegarder la config par défaut
        saveDefaultConfig();
        pointsManager = new PointsManager(this);
        getServer().getPluginManager().registerEvents(new ChatManager(this), this);
        // Reste de votre code...
        Bukkit.getConsoleSender().sendMessage("§2§m--------------------------\n" +
                "                 §6Hikabrain by ronnyaep\n" +
                "                 §2§m--------------------------");

        Bukkit.getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
        Bukkit.getWorlds().get(0).setGameRuleValue("doMobSpawning", "false");
        Bukkit.getWorlds().get(0).setGameRuleValue("keepInventory", "true");
        getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
        getCommand("hikabrain").setExecutor(new HikaBrainCommand(this));
        setGameState(GameState.WAITING);

        getServer().getPluginManager().registerEvents(new Manager(this), this);

        ScoreBoardUpdater scoreBoardUpdater = new ScoreBoardUpdater(this);
        scoreBoardUpdater.runTaskTimer(this, 0, 1);
        initMap();
    }

    public PointsManager getPointsManager() {
        return pointsManager;
    }
    
    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§2§m--------------------------\n" +
                "                 §6Hikabrain by ronnyaep\n" +
                "                 §2§m--------------------------");


        for (Player pls : Bukkit.getOnlinePlayers()) {
            Creater creater = new Creater(this);
            creater.destroy(pls);
            pls.kickPlayer("§cPartie terminée !");
        }
    }

    public void initPlayer(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = null;
        float x = 20.5f;
        float yaw = 0;

        if(getPlayerInGame().get(0).equalsIgnoreCase(player.getName())){
            x = 20.5f;
            yaw = 90.0f;
            player.setPlayerListName("§3Bleu " + player.getName());
            player.setDisplayName("§3Bleu " + player.getName() + "§r");

            if(scoreboard.getTeam(player.getName()) == null){
                team = scoreboard.registerNewTeam(player.getName());
            }else{
                team = scoreboard.getTeam(player.getName());
            }

            team.setPrefix("§3Bleu ");
            team.addPlayer(Bukkit.getPlayer(player.getName()));
        }else {
            x = -20.5f;
            yaw = 270.0f;
            player.setPlayerListName("§cRouge " + player.getName());
            player.setDisplayName("§cRouge " + player.getName() + "§r");

            if(scoreboard.getTeam(player.getName()) == null){
                team = scoreboard.registerNewTeam(player.getName());
            }else{
                team = scoreboard.getTeam(player.getName());
            }

            team.setPrefix("§cRouge ");
            team.addPlayer(Bukkit.getPlayer(player.getName()));
        }

        Location tp = new Location(player.getLocation().getWorld(), x, 64, 0.5f, yaw, 0.0f);
        player.teleport(tp);
        player.setHealth(20);
        player.setSaturation(20);
        player.setFoodLevel(20);
        player.getInventory().clear();

        // Équipement
        givePlayerEquipment(player);

        player.setGameMode(GameMode.SURVIVAL);
        player.setNoDamageTicks(20*2);
    }

    private void givePlayerEquipment(Player player) {
        // Armes et outils
        ItemStack sword = createSword();
        ItemStack pickAxe = createPickaxe();
        ItemStack gApple = new ItemStack(Material.GOLDEN_APPLE, 64);
        ItemStack sandStonne = new ItemStack(Material.SANDSTONE, 64);

        // Armure
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        ItemStack leggins = new ItemStack(Material.IRON_LEGGINGS);
        ItemStack boots = new ItemStack(Material.IRON_BOOTS);

        // Équiper le joueur
        player.getInventory().setItemInOffHand(sandStonne);
        player.getInventory().addItem(sword);
        player.getInventory().addItem(pickAxe);
        player.getInventory().addItem(gApple);

        // Ajouter les blocs
        for (int i = 0; i < 40; i++) {
            player.getInventory().addItem(sandStonne);
        }

        // Équiper l'armure
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggins);
        player.getInventory().setBoots(boots);
    }

    private ItemStack createSword() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta swordM = sword.getItemMeta();
        swordM.addEnchant(Enchantment.DURABILITY, 1000, true);
        swordM.addEnchant(Enchantment.KNOCKBACK, 1, true);
        sword.setItemMeta(swordM);
        return sword;
    }

    private ItemStack createPickaxe() {
        ItemStack pickAxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickAxeM = pickAxe.getItemMeta();
        pickAxeM.addEnchant(Enchantment.DURABILITY, 1000, true);
        pickAxe.setItemMeta(pickAxeM);
        return pickAxe;
    }
    
    public List<Map.Entry<String, PlayerStats>> getLeaderboard() {
        List<Map.Entry<String, PlayerStats>> leaderboard = new ArrayList<>(playerStats.entrySet());
        leaderboard.sort((a, b) -> b.getValue().getPoints() - a.getValue().getPoints());
        return leaderboard;
    }

    public void initMap(){
        World world = Bukkit.getWorlds().get(0);

        for (int x = -21; x < 22; x++) {
            for (int y = 50; y < 73; y++) {
                for (int z = -7; z < 8; z++){
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                    world.getBlockAt(x, 69, z).setType(Material.BARRIER);
                }
            }
        }

        for (int y = 50; y < 73; y++){
            for (int x = -22; x < 23; x++){
                for (int z = -8; z < 9; z++){
                    if(x == -22 || x == 22 || z == -8 || z == 8){
                        if (y < 70){
                            world.getBlockAt(x, y, z).setType(Material.OBSIDIAN);
                        }else if (y > 70){
                            world.getBlockAt(x, y, z).setType(Material.BARRIER);
                        }else {
                            world.getBlockAt(x, y, z).setType(Material.AIR);
                        }
                    }
                }
            }
        }

        for (int x = -21; x < 22; x++) {
            for (int y = 50; y < 60; y++) {
                if(x == 0 && y == 59){
                    world.getBlockAt(x, y, 0).setType(Material.REDSTONE_BLOCK);
                }else if((x == 21 || x == 20) && y == 59){
                    world.getBlockAt(x, y, 0).setType(Material.WOOL);
                    world.getBlockAt(x, y, 0).setData(( byte ) 11);
                }else if((x == -21 || x == -20) && y == 59){
                    world.getBlockAt(x, y, 0).setType(Material.WOOL);
                    world.getBlockAt(x, y, 0).setData(( byte ) 14);
                }else{
                    world.getBlockAt(x, y, 0).setType(Material.SANDSTONE);
                }
            }
        }

        for (int x = 19; x < 22; x++) {
            for (int z = -1; z < 2; z++) {
                world.getBlockAt(x, 63, z).setType(Material.STAINED_GLASS);
                world.getBlockAt(x, 63, z).setData(( byte ) 11);
            }
        }

        for (int x = -21; x < -18; x++) {
            for (int z = -1; z < 2; z++) {
                world.getBlockAt(x, 63, z).setType(Material.STAINED_GLASS);
                world.getBlockAt(x, 63, z).setData(( byte ) 14);
            }
        }
    }

    public void reloadScoreboardConfig() {
        reloadConfig();
        if (scoreBoardUpdater != null) {
            if (getServer().getScheduler().isCurrentlyRunning(scoreBoardUpdater.getTaskId())) {
                scoreBoardUpdater.cancel();
            }
            scoreBoardUpdater = new ScoreBoardUpdater(this);
            scoreBoardUpdater.runTaskTimer(this, 0, 1);
        }
    }
    
    public int getTimer(){return timer;}

    public void setTimer(int timer){this.timer = timer;}

    public Map<String, Integer> getPoint() {
        return point;
    }

    public void setPoint(Map<String, Integer> point) {
        this.point = point;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        
        if (gameState == GameState.PLAYING) {
            if (this.gameTimer == null) { // Ne reset le chrono que si c'est un nouveau jeu
                gameTime = 0;
                gameTimer = Bukkit.getScheduler().runTaskTimer(this, () -> {
                    gameTime++;
                }, 0L, 20L);
                
                // Reset K/D seulement au début d'une nouvelle partie
                for (String player : playerInGame) {
                    kills.put(player, 0);
                    deaths.put(player, 0);
                }
            }
        } else if (gameState == GameState.WINNING || gameState == GameState.WAITING) {
            if (gameTimer != null) {
                gameTimer.cancel();
                gameTimer = null;
            }
        }
    }

    public List<String> getPlayerInGame() {
        return playerInGame;
    }

    public void setPlayerInGame(List<String> playerInGame) {
        this.playerInGame = playerInGame;
    }

    public List<String> getPlayerOutGame() {
        return playerOutGame;
    }

    public void setPlayerOutGame(List<String> playerOutGame) {
        this.playerOutGame = playerOutGame;
    }

    public Map<Player, ObjectiveSign> getBoard() {
        return board;
    }

    public void setBoard(Map<Player, ObjectiveSign> board) {
        this.board = board;
    }
    
    public String getWinner(){return winner;}

    public void setWinner(String winner){this.winner = winner;}


}
