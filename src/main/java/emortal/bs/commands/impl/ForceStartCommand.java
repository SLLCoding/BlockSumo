package emortal.bs.commands.impl;

import emortal.bs.games.Game;
import emortal.bs.games.GameManager;
import emortal.bs.games.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceStartCommand extends Command {

    public ForceStartCommand() {
        super("forcestart");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.getName().equalsIgnoreCase("SuperLegoLuis") || commandSender.getName().equalsIgnoreCase("emortl")) {
            Game game = GameManager.getGame((Player) commandSender);
            if (game == null || !(game.getState().equals(GameState.WAITING) || game.getState().equals(GameState.STARTING))) return true;
            if (game.gameStartTask != null) game.gameStartTask.cancel();
            game.start();
        }
        return true;
    }

}
