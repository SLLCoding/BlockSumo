package emortal.bs.commands.impl;

import emortal.bs.games.Game;
import emortal.bs.games.GameManager;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static emortal.bs.Main.instance;

public class NukeCommand extends Command {

    public NukeCommand() {
        super("nuke");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.getName().equalsIgnoreCase("SuperLegoLuis") || commandSender.getName().equalsIgnoreCase("emortl")) {
            Game game = GameManager.getGame((Player) commandSender);
            if (game != null) {
                game.tasks.add(new BukkitRunnable() {
                    int i = Game.tntRainSecs;
                    @Override
                    public void run() {
                        i--;
                        if (i <= 0) {
                            cancel();
                            return;
                        }
                        for (Player p : game.getPlayers()) {
                            final TNTPrimed tnt = (TNTPrimed) game.midLoc.getWorld().spawnEntity(p.getLocation(), EntityType.PRIMED_TNT);
                            p.getWorld().playSound(p.getLocation(), Sound.FUSE, 1, 1);
                            tnt.setFuseTicks(3*20);
                            tnt.setVelocity(new Vector(0, 0, 0));
                        }
                    }
                }.runTaskTimer(instance, 0, 20));
            }
        }
        return true;
    }

}
