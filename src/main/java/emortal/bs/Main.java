package emortal.bs;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import emortal.bs.Util.GamePosition;
import emortal.bs.Util.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static emortal.bs.Util.ColorUtil.color;
import static emortal.bs.Util.TitleAPI.title;

public class Main extends JavaPlugin implements Listener {

    public static final String[] winMessages = new String[] {
            "gg", "ez", "gg wp", "well played", "free win?", "easy win", "Should have used Minestom"
    };

    public static final Random r = new Random();
    public static final List<GamePosition> gamePositions = new ArrayList<>();
    public static final HashMap<Player, Game> gameMap = new HashMap<>();
    public static final HashMap<Entity, Player> entityMap = new HashMap<>();

    public static final List<Schematic> maps = new ArrayList<>();

    public static ParticleNativeAPI particleAPI;
    public static Particles_1_8 particles;
    public static Game nextGame;
    public static Main instance;

    public void log(String s) {
        getServer().getConsoleSender().sendMessage(color("&dEmortalBS &7| " + s));
    }

    @Override
    public void onEnable() {
        instance = this;
        particleAPI = ParticleNativeCore.loadAPI(this);
        particles = particleAPI.getParticles_1_8();
        getServer().getPluginManager().registerEvents(this,this);
        Items.init();

        final File schemFolder = new File("./maps/");
        for (File file : schemFolder.listFiles()) {
            if (!file.getName().endsWith(".schematic")) continue;
            log("&aFound schematic " + file.getName());

            try {
                maps.add(FaweAPI.load(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        refreshGame();
    }

    @Override
    public void onDisable() {
        gamePositions.clear();
        gameMap.clear();
    }

    public static void refreshGame() {
        for (int x = -10; x < 10; x++)
            for (int y = -10; y < 10; y++) {
                final GamePosition pos = new GamePosition(x,y);
                if (gamePositions.contains(pos)) continue;
                final World w = Bukkit.getWorld("sumo");
                nextGame = new Game(w, pos);
                return;
            }
    }

    @EventHandler
    public void playerJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(nextGame.midLoc);
        nextGame.addPlayer(p);

        for (Player player : getServer().getOnlinePlayers()) {
            if (nextGame.getPlayers().contains(player)) continue;
            player.hidePlayer(p);
            p.hidePlayer(player);
        }
        for (Player player : nextGame.getPlayers()) {
            player.showPlayer(p);
            p.showPlayer(player);
        }

        e.setJoinMessage(color("&8(&a" + nextGame.getPlayers().size() + "&8/&a" + Game.maxPlayers + "&8) " + p.getDisplayName() + "&7 joined"));
    }

    @EventHandler
    public void playerQuit(final PlayerQuitEvent e) {
        e.setQuitMessage(color(e.getPlayer().getDisplayName() + " &7 left"));

        final Game g = gameMap.get(e.getPlayer());
        if (g == null) return;
        g.removePlayer(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void blockPlace(final BlockPlaceEvent e) {

        // BLOCK FIX FOR VIAVERSION
        // Prevents placing blocks inside yourself
        Location location = e.getPlayer().getLocation();
        Location diff = location.clone().subtract(e.getBlock().getLocation().add(0.5D, 0, 0.5D));
        Material block = e.getBlockPlaced().getType();
        if (!isPlacable(block)) {
            if (location.getBlock().equals(e.getBlock())) {
                e.setCancelled(true);
            } else {
                if (location.getBlock().getRelative(BlockFace.UP).equals(e.getBlock())) {
                    e.setCancelled(true);
                } else {
                    if (Math.abs(diff.getX()) <= 0.8 && Math.abs(diff.getZ()) <= 0.8D) {
                        if (diff.getY() <= 0.1D && diff.getY() >= -0.1D) {
                            e.setCancelled(true);
                            return;
                        }
                        BlockFace relative = e.getBlockAgainst().getFace(e.getBlock());
                        if (relative == BlockFace.UP) {
                            if (diff.getY() < 1D && diff.getY() >= 0D) {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
        // END OF VIAVERISON FIX


        final Game g = gameMap.get(e.getPlayer());
        final PlayerStats stats = g.statMap.get(e.getPlayer());
        final Location loc = e.getBlockPlaced().getLocation().subtract(g.midLoc.getBlockX(), 0, g.midLoc.getBlockZ());

        if (e.getBlockAgainst().getType() == Material.BARRIER) {
            e.setCancelled(true);
            if (Math.abs(e.getPlayer().getLocation().getX()) > Game.gameWidth + 3 || Math.abs(e.getPlayer().getLocation().getZ()) > Game.gameWidth + 3) {
                e.getPlayer().sendMessage(ChatColor.RED + "Stop that");
                e.getPlayer().teleport(e.getPlayer().getLocation().subtract(0, 3, 0));
                e.getPlayer().setVelocity(new Vector(0, -4, 0));
            }
            return;
        }

        e.getBlockPlaced().setData((byte) stats.teamColor.woolColor);
        e.getItemInHand().setAmount(64);
        if (Math.abs(loc.getX()) > Game.gameWidth + 2 || Math.abs(loc.getZ()) > Game.gameWidth + 2) {
            e.setCancelled(true);
            return;
        }
        if (Math.abs(loc.getX()) < 3 && loc.getY() > 229 && loc.getY() < 234 && Math.abs(loc.getZ()) < 3)
            getServer().getScheduler().runTaskLater(this, () -> e.getBlockPlaced().setType(Material.AIR), 6*20);
    }
    @EventHandler
    public void blockBreak(final BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.WOOL) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void blockExplode(final BlockExplodeEvent e) {
        e.blockList().removeIf(b -> b.getType() != Material.WOOL);

        final List<FallingBlock> fallingBlockList = new ArrayList<>();

        for (Block b : e.blockList()) {
            if (r.nextInt(2) == 0) continue;
            FallingBlock fallingBlock = e.getBlock().getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData());
            fallingBlock.setVelocity(e.getBlock().getLocation().subtract(b.getLocation()).toVector().multiply(0.35).setY(r.nextDouble()));

            fallingBlockList.add(fallingBlock);
        }

        getServer().getScheduler().runTaskLater(this, () -> {
            if (fallingBlockList.size() == 0) return;

            for (FallingBlock f : fallingBlockList) {
                f.remove();
            }
        }, 20);
    }

    @EventHandler
    public void fallingBlockLand(final EntityChangeBlockEvent e) {
        if (e.getEntityType() != EntityType.FALLING_BLOCK) return;
        e.getEntity().remove();
        e.setCancelled(true);
    }
    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        final Player p = e.getPlayer();

        if (p.getGameMode() != GameMode.SURVIVAL) return;

        final Game g = gameMap.get(p);
        if (p.getLocation().getBlockY() < 215) {
            g.playerDied(p);
            return;
        }

        final Location loc = p.getLocation().clone().subtract(g.midLoc.getBlockX(), 0, g.midLoc.getBlockZ());

        if (Math.abs(loc.getX()) > Game.gameWidth + 5 || Math.abs(loc.getZ()) > Game.gameWidth + 5) {
            p.sendMessage(color("&cYou went out of bounds"));
            g.playerDied(p);
            return;
        }

        final Material fromBlock =      p.getWorld().getBlockAt(e.getFrom().clone().subtract(0, 1, 0)).getType();
        final Material fromBlockUnder = p.getWorld().getBlockAt(e.getFrom().clone().subtract(0, 2, 0)).getType();
        final Material toBlock =        p.getWorld().getBlockAt(e.getTo().clone().subtract(0, 1, 0)).getType();
        final Material toBlockUnder =   p.getWorld().getBlockAt(e.getTo().clone().subtract(0, 2, 0)).getType();

        if (toBlock == Material.DIAMOND_BLOCK || toBlockUnder == Material.DIAMOND_BLOCK) {
            if (g.diamondBlockTask != null && g.diamondBlockPlayer == p) return;

            if (g.diamondBlockPlayer != null && g.diamondBlockPlayer != p) {
                g.diamondBlockTask.cancel();
                g.diamondBlockPlayer.setLevel(0);
                g.diamondBlockTask = null;
                g.diamondBlockPlayer = null;
                return;
            }

            g.diamondBlockPlayer = p;
            g.diamondBlockTask = new BukkitRunnable() {
                @Override
                public void run() {
                    final int i = p.getLevel() + 1;

                    if (i == 20) {
                        g.victory(p);
                        cancel();
                        return;
                    }

                    if (i % 5 == 0 || i > 15) {
                        for (Player player : g.getPlayers()) {
                            if (i > 14) {
                                title(player, ChatColor.RED + "" + ChatColor.BOLD + (20 - i), p.getDisplayName() + color(" &7is on the diamond block!"), 0, 20, 8);
                            }
                            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                            player.sendMessage(color("&c&l>> &r" + p.getDisplayName() + " &7has been on the diamond block for &6" + i + " &7seconds!\n&c&l>> &7They win in &6" + (20 - i) + " &7seconds!"));
                        }
                    }

                    p.setLevel(i);
                }
            }.runTaskTimer(this, 0, 20);

            return;
        }

        if (fromBlock == Material.DIAMOND_BLOCK || fromBlockUnder == Material.DIAMOND_BLOCK) {
            if (g.diamondBlockPlayer == null) return;
            g.diamondBlockPlayer.setLevel(0);
            g.diamondBlockTask.cancel();
            g.diamondBlockTask = null;
            g.diamondBlockPlayer = null;
        }
    }

    @EventHandler
    public void playerDamage(final EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;
        if (((Player)e.getEntity()).getGameMode() != GameMode.SURVIVAL) {
            e.setCancelled(true);
            return;
        }

        e.setDamage(0);

        final Game g = gameMap.get(e.getEntity());
        final PlayerStats stats = g.statMap.get(e.getEntity());


        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (stats.spawnProtectionTask != null) {
                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.DIG_WOOD, 1, 1);
            }
        }
    }

    @EventHandler
    public void playerDamageByPlayer(final EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() != EntityType.PLAYER) return;
        if (e.getDamager().getType() != EntityType.PLAYER) {
            e.setCancelled(true);
            return;
        }
        final Game g = gameMap.get(e.getEntity());
        final PlayerStats stats = g.statMap.get(e.getEntity());

        if (e.getDamager() instanceof Projectile) {
            stats.setLastHitBy((Player) ((Projectile) e.getDamager()).getShooter());

            if (g.diamondBlockPlayer == e.getEntity()) {
                g.diamondBlockPlayer.setLevel(0);
            }
            return;
        }
        if (e.getEntityType() != EntityType.PLAYER) return;

        stats.setLastHitBy((Player) e.getDamager());

        if (g.diamondBlockPlayer == e.getEntity()) {
            g.diamondBlockPlayer.setLevel(0);
        }
    }

    @EventHandler
    public void playerConsume(final PlayerItemConsumeEvent e) {
        e.setCancelled(true);
        e.getPlayer().setItemInHand(null);
        e.getPlayer().addPotionEffect(((PotionMeta)e.getItem().getItemMeta()).getCustomEffects().get(0));
    }

    @EventHandler
    public void fish(final PlayerFishEvent e) {
        e.setExpToDrop(0);
        if (e.getState() == PlayerFishEvent.State.FISHING) return;

        e.getPlayer().setVelocity(e.getHook().getLocation().subtract(e.getPlayer().getLocation()).toVector().normalize().multiply(2));
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ITEM_BREAK, 1, 1);
        e.getPlayer().getInventory().setItemInHand(null);
        e.getHook().remove();
    }

    @EventHandler
    public void projectileHit(final ProjectileHitEvent e) {
        if (e.getEntityType() != EntityType.FIREBALL) return;

        final Location loc = e.getEntity().getLocation();



        for (Entity nearbyEntity : e.getEntity().getNearbyEntities(10, 10, 10)) {
            if (nearbyEntity.getType() != EntityType.PLAYER) continue;
            if (nearbyEntity.getLocation().distance(e.getEntity().getLocation()) > 5) continue;

            final Game g = gameMap.get(nearbyEntity);
            final PlayerStats stats = g.statMap.get(nearbyEntity);

            final double xPos = nearbyEntity.getLocation().getX() - loc.getX();
            final double yPos = nearbyEntity.getLocation().getY() - loc.getY() + 0.3;
            final double zPos = nearbyEntity.getLocation().getZ() - loc.getZ();

            stats.setLastHitBy((Player) e.getEntity().getShooter());
            ((Player) nearbyEntity).damage(0);
            nearbyEntity.setVelocity(new Vector(xPos, yPos, zPos).normalize().multiply(1.5));
        }
    }

    @EventHandler
    public void entityExplode(final EntityExplodeEvent e) {
        e.setCancelled(true);
        final Location loc = e.getEntity().getLocation();
        e.getEntity().getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 2, false, true);

        for (Entity nearbyEntity : e.getEntity().getNearbyEntities(10, 10, 10)) {
            if (nearbyEntity.getType() != EntityType.PLAYER) continue;
            if (nearbyEntity.getLocation().distance(e.getEntity().getLocation()) > 5) continue;

            final Game g = gameMap.get(nearbyEntity);
            final PlayerStats stats = g.statMap.get(nearbyEntity);

            final double xPos = nearbyEntity.getLocation().getX() - loc.getX();
            final double yPos = nearbyEntity.getLocation().getY() - loc.getY() + 0.3;
            final double zPos = nearbyEntity.getLocation().getZ() - loc.getZ();

            stats.setLastHitBy(entityMap.get(e.getEntity()));
            ((Player) nearbyEntity).damage(0);
            nearbyEntity.setVelocity(new Vector(xPos, yPos, zPos).normalize().multiply(1.5));
        }

        entityMap.remove(e.getEntity());
    }

    @EventHandler
    public void playerItemUse(final PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player p = e.getPlayer();
        final Game g = gameMap.get(p);
        if (g == null) return;
        final PlayerStats stats = g.statMap.get(p);

        switch (e.getItem().getType()) {
            case FIREBALL:
                p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
                p.launchProjectile(Fireball.class, p.getEyeLocation().getDirection().multiply(0.5));

                e.setCancelled(true);
                if (e.getItem().getAmount() == 1) p.setItemInHand(null);
                else e.getItem().setAmount(e.getItem().getAmount() - 1);

                return;

            case NETHER_STAR:
                stats.lives++;

                g.updateLives();
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);

                e.setCancelled(true);
                if (e.getItem().getAmount() == 1) p.setItemInHand(null);
                else e.getItem().setAmount(e.getItem().getAmount() - 1);

                return;

            case TNT:
                e.setCancelled(true);

                if (e.getClickedBlock() == null) return;

                final Location locToSpawn = e.getClickedBlock().getLocation().add(0.5 + e.getBlockFace().getModX(), e.getBlockFace().getModY(), 0.5 + e.getBlockFace().getModZ());

                if (e.getItem().getAmount() == 1) p.setItemInHand(null);
                else e.getItem().setAmount(e.getItem().getAmount() - 1);

                final TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(locToSpawn, EntityType.PRIMED_TNT);
                tnt.setVelocity(new Vector(0, 0.2, 0));
                tnt.setFuseTicks(3*20);
                tnt.setCustomNameVisible(true);

                entityMap.put(tnt, p);

                p.getWorld().playSound(tnt.getLocation(), Sound.FUSE, 1, 1);
                p.getWorld().playSound(tnt.getLocation(), Sound.DIG_GRASS, 1, 1);

                new BukkitRunnable() {
                    float i = 3;
                    @Override
                    public void run() {
                        i -= 0.2;
                        if (i <= 0) {
                            cancel();
                            return;
                        }
                        tnt.setCustomName((i > 2 ? ChatColor.GREEN : i > 1 ? ChatColor.GOLD : ChatColor.RED) + "" + (double)Math.round(i * 10D) / 10D);
                    }
                }.runTaskTimerAsynchronously(this, 0, 4);

                return;
        }
    }

    @EventHandler
    public void inventoryMove(final InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() == InventoryType.CRAFTING) e.setCancelled(true);
    }
    @EventHandler
    public void inventory3(final InventoryDragEvent e) {
        if (e.getInventory().getType() == InventoryType.CRAFTING) e.setCancelled(true);
    }
    @EventHandler
    public void playerDropItem(final PlayerDropItemEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void itemDropped(final ItemSpawnEvent e) {
        if (!Items.midLoot.contains(e.getEntity().getItemStack())) e.setCancelled(true);
    }
    @EventHandler
    public void weatherChange(final WeatherChangeEvent e) {
        e.setCancelled(true);
    }


    // VIAVERSION FIX
    private boolean isPlacable(Material material) {
        if (!material.isSolid()) return true;
        // signs and banners
        switch (material.getId()) {
            case 63:
            case 68:
            case 176:
            case 177:
                return true;
            default:
                return false;
        }
    }

}
