package fr.WarzouMc.hikabrain.gameLoop;

import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StartLoop extends BukkitRunnable {

    private final Main main;
    private int second = 0;
    private int timer = 10;
    private int all = 10 * 20;

    public StartLoop(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        main.setTimer(all);
        if(second == 0) {
            second = 20;
            for (String playerName : main.getPlayerInGame()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player == null) continue;

                switch (timer) {
                    case 10:
                        player.resetTitle();
                        player.sendTitle("", "§b" + timer + "s");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, 1, 1); // Changé ici
                        break;
                    case 5:
                        player.resetTitle();
                        player.sendTitle("", "§b" + timer + "s");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, 1, 1); // Changé ici
                        break;
                    case 3:
                        player.resetTitle();
                        player.sendTitle("", "§b" + timer + "s");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, 1, 1); // Changé ici
                        break;
                    case 2:
                        player.resetTitle();
                        player.sendTitle("", "§b" + timer + "s");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, 1, 1); // Changé ici
                        break;
                    case 1:
                        player.resetTitle();
                        player.sendTitle("", "§b" + timer + "s");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_SNARE, 1, 1); // Changé ici
                        break;
                    case 0:
                        main.initPlayer(player);
                        break;
                }
            }
            timer--;
        }

        if(main.getGameState() == GameState.WAITING) {
            cancel();
        }
        if(timer == -1) {
            if(main.getGameState() == GameState.STARTING) {
                main.setGameState(GameState.PLAYING);
                cancel();
            }
        }
        second--;
        all--;
    }

    public int getTimer() {
        return timer;
    }
}