package emortal.bs.commands.impl;

import emortal.bs.Game;
import emortal.bs.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class EndGameCommand extends Command {

    public EndGameCommand() {
        super("endgame");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.getName().equalsIgnoreCase("SuperLegoLuis") || commandSender.getName().equalsIgnoreCase("emortl")) {
            Game game = Main.gameMap.get(commandSender);
            if (game != null) game.stop();
        }
        return true;
    }

}
