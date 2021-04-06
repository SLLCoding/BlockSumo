package emortal.bs;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import static emortal.bs.Util.ColorUtil.color;

public class PlayerStats {
    public byte lives = 5;
    public byte kills = 0;
    private Player lastHitBy = null;
    private long lastHitTime = 0;
    public final TeamColor teamColor;
    public final Player player;
    public final BPlayerBoard board;
    public BukkitTask spawnProtectionTask = null;

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
}
