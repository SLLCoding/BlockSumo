package emortal.bs.Util;

import emortal.bs.Main;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.WorldBorder;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldBorderUtil {

    private static final WorldBorder shown = new WorldBorder();
    private static final WorldBorder hidden = new WorldBorder();
    private static final List<UUID> shownPlayers = new ArrayList<>();

    public static void init() {
        shown.setSize(30_000_000);
        shown.setWarningDistance(50_000_000);

        hidden.setSize(30_000_000);
        hidden.setCenter(0, 0);
    }

    public static void show(Player player) {
        if (shownPlayers.contains(player.getUniqueId())) return;
        shownPlayers.add(player.getUniqueId());

        shown.setCenter(player.getLocation().getX() + 10000, player.getLocation().getZ() + 10000);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(shown, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));

    }

    public static void hide(Player player) {
        if (!shownPlayers.contains(player.getUniqueId())) return;
        shownPlayers.remove(player.getUniqueId());

        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(hidden, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

}
