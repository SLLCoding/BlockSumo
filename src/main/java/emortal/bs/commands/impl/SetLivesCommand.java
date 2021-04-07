package emortal.bs.commands.impl;

import emortal.bs.Game;
import emortal.bs.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetLivesCommand extends Command {

    public SetLivesCommand() {
        super("setlives");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.getName().equalsIgnoreCase("SuperLegoLuis") || commandSender.getName().equalsIgnoreCase("emortl")) {
            Game game = Main.gameMap.get(commandSender);
            game.statMap.get(commandSender).lives = (byte) Integer.parseInt(strings[0]);
            game.updateLives();
        }
        return true;
    }

}
