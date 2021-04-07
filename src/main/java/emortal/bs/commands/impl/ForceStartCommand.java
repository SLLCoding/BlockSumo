package emortal.bs.commands.impl;

import emortal.bs.Game;
import emortal.bs.Main;
import emortal.bs.Util.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ForceStartCommand extends Command {

    public ForceStartCommand() {
        super("forcestart");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.getName().equalsIgnoreCase("SuperLegoLuis") || commandSender.getName().equalsIgnoreCase("emortl")) {
            Game game = Main.gameMap.get(commandSender);
            if (game == null || !game.getState().equals(GameState.STARTING)) return true;
            if (game.gameStartTask != null) game.gameStartTask.cancel();
            game.start();
        }
        return true;
    }

}
