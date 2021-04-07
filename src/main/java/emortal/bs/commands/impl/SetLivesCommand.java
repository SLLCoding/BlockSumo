package emortal.bs.commands.impl;

import emortal.bs.Game;
import emortal.bs.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLivesCommand extends Command {

    public SetLivesCommand() {
        super("setlives");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender.getName().equalsIgnoreCase("SuperLegoLuis") || sender.getName().equalsIgnoreCase("emortl")) {
            Player player;
            int lives;
            if (args.length > 1) {
                player = Bukkit.getPlayer(args[0]);
                if (player == null) return true;
                try {
                    lives = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    return true;
                }
            } else {
                player = (Player) sender;
                try {
                    lives = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    return true;
                }
            }
            Game game = Main.gameMap.get(player);
            if (game == null) return true;
            game.statMap.get(player).lives = (byte) lives;
            game.updateLives();
        }
        return true;
    }

}
