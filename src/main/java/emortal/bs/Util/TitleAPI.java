package emortal.bs.Util;

import com.google.common.base.Strings;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import static emortal.bs.Util.ColorUtil.color;

public class TitleAPI {
    public static void actionbar(Player p, String message) {
        final PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + color(message) + "\"}"), (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void title(Player p, String title, String subtitle, int in, int stay, int out) {
        final IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + color(title) + "\"}");
        final IChatBaseComponent subtitleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + color(subtitle) + "\"}");
        final PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleComponent);
        final PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleComponent);
        final PacketPlayOutTitle length = new PacketPlayOutTitle(in, stay, out);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(titlePacket);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(subtitlePacket);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);
    }
}
