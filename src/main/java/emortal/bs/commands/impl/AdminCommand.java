package emortal.bs.commands.impl;

import emortal.bs.games.Game;
import emortal.bs.games.GameManager;
import emortal.bs.games.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand extends Command {

    public AdminCommand() {
        super("admin");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender.getName().equalsIgnoreCase("SuperLegoLuis") || sender.getName().equalsIgnoreCase("emortl"))) return true;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("end")) {
                // TODO: Allow user to specify player or game ID.
                Game game = GameManager.getGame((Player) sender);
                if (game != null) game.stop();
            } else if (args[0].equalsIgnoreCase("start")) {
                // TODO: Allow user to specify player or game ID.
                Game game = GameManager.getGame((Player) sender);
                if (game == null || !(game.getState().equals(GameState.WAITING) || game.getState().equals(GameState.STARTING))) return true;
                if (game.gameStartTask != null) game.gameStartTask.cancel();
                game.start();
            } else if (args[0].equalsIgnoreCase("lives")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("set")) {
                        Player player;
                        int lives;
                        if (args.length > 3) {
                            player = Bukkit.getPlayer(args[2]);
                            if (player == null) return true;
                            try {
                                lives = Integer.parseInt(args[3]);
                            } catch (Exception e) {
                                return true;
                            }
                        } else {
                            player = (Player) sender;
                            try {
                                lives = Integer.parseInt(args[2]);
                            } catch (Exception e) {
                                return true;
                            }
                        }
                        Game game = GameManager.getGame(player);
                        if (game == null) return true;
                        game.statMap.get(player).lives = (byte) lives;
                        game.updateLives();
                    } else if (args[1].equalsIgnoreCase("reset")) {
                        Player player;
                        if (args.length > 2) {
                            player = Bukkit.getPlayer(args[0]);
                            if (player == null) return true;
                        } else {
                            player = (Player) sender;
                        }
                        Game game = GameManager.getGame(player);
                        if (game == null) return true;
                        game.statMap.get(player).lives = (byte) game.getOptions().getStartingLives();
                        game.updateLives();
                    }
                }
            }
        }
        return true;
    }

}
