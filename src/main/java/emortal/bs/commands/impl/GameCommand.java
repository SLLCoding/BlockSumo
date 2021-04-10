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

public class GameCommand extends Command {

    public GameCommand() {
        super("game");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // TODO: Replace with permission check or ownership over the game.
        if (!(sender.getName().equalsIgnoreCase("superlegoluis") || sender.getName().equalsIgnoreCase("emortl"))) return true;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("nuke")) {
                Game game = GameManager.getGame((Player) sender);
                if (game != null) {
                    game.tasks.add(new BukkitRunnable() {
                        int i = game.getOptions().getTntRainTimer();
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
                                tnt.setVelocity(new Vector(0, 0.2, 0));
                            }
                        }
                    }.runTaskTimer(instance, 0, 20));
                }
            }/* else if (args[0].equalsIgnoreCase("private")) {
                Player player = (Player) sender;
                GameManager.removePlayer(player);
                player.teleport(new Location(player.getWorld(), 0, 64, 0));

                player.sendMessage("You would be configuring a custom game right now but I haven't made that yet so you can just rejoin to get back into a public game.");
            }*/
        }
        return true;
    }

}
