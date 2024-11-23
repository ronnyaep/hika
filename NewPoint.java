package fr.WarzouMc.hikabrain.gameLoop;

import fr.WarzouMc.hikabrain.main.Main;
import fr.WarzouMc.hikabrain.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class NewPoint extends BukkitRunnable {

    private Main main;
    private int countdown = 2; // Compte à rebours de 3 secondes

    public NewPoint(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        Player bluePlayer = Bukkit.getPlayer(main.getPlayerInGame().get(0));
        Player redPlayer = Bukkit.getPlayer(main.getPlayerInGame().get(1));

        // Vérifier que les joueurs sont en ligne
        if (bluePlayer == null || redPlayer == null) {
            cancel(); // Arrêter si les joueurs ne sont pas trouvés
            return;
        }

        if (countdown >= 0) {
            // Afficher le compte à rebours
            bluePlayer.resetTitle();
            redPlayer.resetTitle();
            bluePlayer.sendTitle("§bPrêt ?", "§7" + countdown + "§7s");
            redPlayer.sendTitle("§bPrêt ?", "§7" + countdown + "§7s");
            if (countdown >= 1) {
                // Afficher le compte à rebours
                bluePlayer.resetTitle();
                redPlayer.resetTitle();
                bluePlayer.sendTitle("§eÀ vos marques...", "§7" + countdown + "§7s");
                redPlayer.sendTitle("§eÀ vos marques...", "§7" + countdown + "§7s");
            }
            if (countdown >= 2) {
                // Afficher le compte à rebours
                bluePlayer.resetTitle();
                redPlayer.resetTitle();
                bluePlayer.sendTitle("§6Attention...", "§7" + countdown + "§7s");
                redPlayer.sendTitle("§6Attention...", "§7" + countdown + "§7s");
            }
            // Jouer le son avec des variations de hauteur
            switch (countdown) {
                case 2:
                    bluePlayer.playSound(bluePlayer.getLocation(), "entity.experience_orb.pickup", 1f, 0.5f); // Son aigu
                    redPlayer.playSound(redPlayer.getLocation(), "entity.experience_orb.pickup", 1f, 0.5f); // Son aigu
                    break;
                case 1:
                    bluePlayer.playSound(bluePlayer.getLocation(), "entity.experience_orb.pickup", 1f, 1f); // Son moins aigu
                    redPlayer.playSound(redPlayer.getLocation(), "entity.experience_orb.pickup", 1f, 1f); // Son moins aigu
                    break;
                case 0:
                    bluePlayer.playSound(bluePlayer.getLocation(), "entity.experience_orb.pickup", 1f, 1.5f); // Son grave
                    redPlayer.playSound(redPlayer.getLocation(), "entity.experience_orb.pickup", 1f, 1.5f); // Son grave
                    break;
            }
            countdown--;
        } else {
            // Afficher "Yeah !" à la fin du décompte
            bluePlayer.resetTitle();
            redPlayer.resetTitle();
            bluePlayer.sendTitle("", "§bYeah !");
            redPlayer.sendTitle("", "§bYeah !");
            
            // Jouer le son aigu pour "Yeah !"
            bluePlayer.playSound(bluePlayer.getLocation(), "entity.experience_orb.pickup", 1f, 2f); // Son aigu
            redPlayer.playSound(redPlayer.getLocation(), "entity.experience_orb.pickup", 1f, 2f); // Son aigu
            
            // Passer à l'état de jeu
            main.setGameState(GameState.PLAYING);
            cancel(); // Arrêter le décompte
        }
    }
}