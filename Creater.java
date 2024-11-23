package fr.WarzouMc.hikabrain.graphic.scoreboard;

import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.graphic.scoreboard.ObjectiveSign;
import org.bukkit.entity.Player;

import java.util.Map;

public class Creater {

    private final Main main;

    public Creater(Main main) {
        this.main = main;
    }

    public void create(Player player) {
        ObjectiveSign objective = new ObjectiveSign("hikabrain", "§6§lHikaBrain");
        
        Map<Player, ObjectiveSign> board = main.getBoard();
        
        objective.addReceiver(player);
        
        // Initialisation du scoreboard
        for (int i = 0; i < 15; i++) {
            objective.setLine(i, "");
        }
        objective.setLine(0, "§8§m----------------------");
        
        board.put(player, objective);
        main.setBoard(board);
    }

    public void destroy(Player player) {
        Map<Player, ObjectiveSign> board = main.getBoard();
        if (!board.containsKey(player)) return;
        
        ObjectiveSign objective = board.get(player);
        objective.removeReceiver(player);
        board.remove(player);
        main.setBoard(board);
    }
}