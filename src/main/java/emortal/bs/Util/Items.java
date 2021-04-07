package emortal.bs.Util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static emortal.bs.Util.ColorUtil.color;
import static emortal.bs.Util.ColorUtil.colorList;

public class Items {
    public static final ItemStack shears = new ItemStack(Material.SHEARS);
    public static final ItemStack tnt = new ItemStack(Material.TNT);
    public static final ItemStack fireball = new ItemStack(Material.FIREBALL);
    public static final ItemStack snowballs = new ItemStack(Material.SNOW_BALL, 8);
    public static final ItemStack bat = new ItemStack(Material.WOOD_SWORD);

    public static final ItemStack megaBat = new ItemStack(Material.IRON_SWORD);
    public static final ItemStack jumpboost = new ItemStack(Material.POTION, 1, (byte)11);
    public static final ItemStack extraLife = new ItemStack(Material.NETHER_STAR);
    public static final ItemStack grappleHook = new ItemStack(Material.FISHING_ROD);

    public static final List<ItemStack> midLoot = new ArrayList<>();
    public static final List<ItemStack> everywhereLoot = new ArrayList<>();

    public static void init() {
        final ItemMeta shearMeta = shears.getItemMeta();
        shearMeta.spigot().setUnbreakable(true);
        shearMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        shears.setItemMeta(shearMeta);

        final ItemMeta tntMeta = tnt.getItemMeta();
        tntMeta.setDisplayName(color("&c&lTNT"));
        tnt.setItemMeta(tntMeta);

        final ItemMeta fireballMeta = fireball.getItemMeta();
        fireballMeta.setDisplayName(color("&6&lFireball"));
        fireball.setItemMeta(fireballMeta);

        final ItemMeta snowMeta = snowballs.getItemMeta();
        snowMeta.setDisplayName(color("&f&lSnowball"));
        snowballs.setItemMeta(snowMeta);

        final ItemMeta batMeta = bat.getItemMeta();
        batMeta.setDisplayName(color("&6&lBat"));
        batMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        bat.setItemMeta(batMeta);
        bat.setDurability(Material.WOOD_SWORD.getMaxDurability());

        final ItemMeta megaBatMeta = bat.getItemMeta();
        megaBatMeta.setDisplayName(color("&f&lMega Bat"));
        megaBatMeta.addEnchant(Enchantment.KNOCKBACK, 3, true);
        megaBat.setItemMeta(megaBatMeta);
        megaBat.setDurability(Material.IRON_SWORD.getMaxDurability());

        final PotionMeta jumpMeta = (PotionMeta)jumpboost.getItemMeta();
        jumpMeta.setDisplayName(color("&a&lJump Boost Potion"));
        jumpMeta.setLore(colorList("&7Lasts for 30 seconds"));
        jumpMeta.setMainEffect(PotionEffectType.JUMP);
        jumpMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        jumpMeta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, 30*20, 4, true, true), true);
        jumpboost.setItemMeta(jumpMeta);

        final ItemMeta extraMeta = extraLife.getItemMeta();
        extraMeta.setDisplayName(color("&b&lExtra Life"));
        extraMeta.setLore(colorList("&7Right click to gain an extra life!"));
        extraMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        extraMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        extraLife.setItemMeta(extraMeta);

        final ItemMeta grappleMeta = grappleHook.getItemMeta();
        grappleMeta.setDisplayName(color("&6&lGrapple Hook"));
        grappleHook.setItemMeta(grappleMeta);
        grappleHook.setDurability(Material.FISHING_ROD.getMaxDurability());


        midLoot.add(extraLife);
        midLoot.add(jumpboost);
        midLoot.add(megaBat);
        midLoot.add(grappleHook);

        everywhereLoot.add(tnt);
        everywhereLoot.add(fireball);
        everywhereLoot.add(snowballs);
        everywhereLoot.add(bat);
    }
}
