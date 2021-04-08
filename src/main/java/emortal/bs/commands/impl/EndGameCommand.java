package emortal.bs.commands.impl;

import emortal.bs.games.Game;
import emortal.bs.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndGameCommand extends Command {

    public EndGameCommand() {
        super("endgame");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.getName().equalsIgnoreCase("SuperLegoLuis") || commandSender.getName().equalsIgnoreCase("emortl")) {
            Game game = GameManager.getGame((Player) commandSender);
            if (game != null) game.stop();
        }
        return true;
    }

}
