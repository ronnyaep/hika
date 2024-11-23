package fr.WarzouMc.hikabrain.commands;

import fr.WarzouMc.hikabrain.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HikaBrainCommand implements CommandExecutor {
    private final Main plugin;

    public HikaBrainCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("hikabrain.reload")) {
                sender.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande !");
                return true;
            }
            
            plugin.reloadScoreboardConfig();
            sender.sendMessage("§aConfiguration rechargée avec succès !");
            return true;
        }
        
        return false;
    }
}