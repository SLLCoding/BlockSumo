package emortal.bs;

import emortal.bs.games.GameManager;
import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

import static emortal.bs.Main.particles;
import static emortal.bs.Util.ColorUtil.color;
import static emortal.bs.Util.TitleAPI.actionbar;

public class PlayerStats {
    public byte lives = 5;
    public byte kills = 0;
    private Player lastHitBy = null;
    private long lastHitTime = 0;
    public final TeamColor teamColor;
    public final Player player;
    public final BPlayerBoard board;
    public BukkitTask spawnProtectionTask = null;
    public BukkitTask respawnTask = null;

    public PlayerStats(Player p, TeamColor teamColor) {
        this.player = p;
        this.teamColor = teamColor;
        this.board = Netherboard.instance().createBoard(p, color("&6&lBLOCK SUMO"));
    }

    public void setLastHitBy(Player p) {
        lastHitBy = p;
        lastHitTime = System.currentTimeMillis();
    }
    public Player getLastHitBy() {
        if (lastHitBy != null && System.currentTimeMillis() - lastHitTime > 8000) {
            lastHitBy = null;
            return null;
        }
        return lastHitBy;
    }

    public String coloredLives() {
        final ChatColor liveColor = (lives > 5 ? ChatColor.AQUA : lives > 3 ? ChatColor.GREEN : lives > 2 ? ChatColor.GOLD : ChatColor.RED);
        return liveColor + "" + ChatColor.BOLD + lives + liveColor;
    }

    public void removeSpawnProtection(Random r) {
        spawnProtectionTask.cancel();
        spawnProtectionTask = null;

        actionbar(player, "&8Your spawn protection has worn off");
        player.playSound(player.getLocation(), Sound.FIZZ, 0.25f, 1);

        for (byte a = 0; a < 40; a++) {
            final Object particlePacket = particles.SMOKE_LARGE().packetMotion(false, player.getLocation().clone().add(r.nextDouble()*0.2, 0.5+(r.nextDouble()*0.2), r.nextDouble()*0.2), Vector.getRandom().multiply(0.3));
            particles.sendPacket(GameManager.getGame(player).getPlayers(), particlePacket);
        }
    }
}
