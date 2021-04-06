package emortal.bs;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum TeamColor {

    AQUA(ChatColor.AQUA, Color.AQUA, 3),
    GRAY(ChatColor.GRAY, Color.GRAY, 8),
    DARK_GRAY(ChatColor.DARK_GRAY, Color.GRAY, 7),
    GREEN(ChatColor.GREEN, Color.LIME, 5),
    ORANGE(ChatColor.GOLD, Color.ORANGE, 1),
    PURPLE(ChatColor.LIGHT_PURPLE, Color.FUCHSIA, 6),
    YELLOW(ChatColor.YELLOW, Color.YELLOW, 4),
    RED(ChatColor.RED, Color.RED, 14),
    WHITE(ChatColor.WHITE, Color.WHITE, 0);

    public final ChatColor chatColor;
    public final Color color;
    public final int woolColor;
    TeamColor(ChatColor chatColor, Color color, int woolColor) {
        this.chatColor = chatColor;
        this.color = color;
        this.woolColor = woolColor;
    }
}
