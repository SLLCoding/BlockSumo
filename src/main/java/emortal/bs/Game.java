package emortal.bs;

import com.boydti.fawe.object.schematic.Schematic;
import com.github.fierioziy.particlenativeapi.api.types.ParticleTypeMotion;
import com.google.common.base.Strings;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import emortal.bs.Util.GamePosition;
import emortal.bs.Util.Items;
import emortal.bs.Util.TaskUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static emortal.bs.Main.*;
import static emortal.bs.Util.ColorUtil.color;
import static emortal.bs.Util.TitleAPI.actionbar;
import static emortal.bs.Util.TitleAPI.title;

public class Game {

    public static final int playersNeededToStart = 2;
    public static final int gameWidth = 15;
    public static final int maxPlayers = 12;

    public static final int tntRainSecs = 8;
    public static final int respawnSecs = 5;
    public static final int gameStartSecs = 10;
    public static final int midSpawnSecs = 40;
    public static final int everywhereSpawnSecs = midSpawnSecs / 2;

    private final List<Player> players = new ArrayList<>();
    private final List<Player> dead = new ArrayList<>();

    public final HashMap<Player, PlayerStats> statMap = new HashMap<>();
    public final List<BukkitTask> tasks = new ArrayList<>();

    private final Random r = new Random();
    private final World w;
    public final Location midLoc;

    public boolean started = false;
    private boolean starting = false;
    private boolean hasRainedTNT = false;
    private boolean victorying = false;

    private long startTime;

    private final GamePosition pos;
    public BukkitTask gameStartTask = null;
    public BukkitTask diamondBlockTask = null;
    public Player diamondBlockPlayer = null;

    public Game(World w, GamePosition pos) {
        this.pos = pos;
        this.w = w;
        this.midLoc = new Location(w, pos.x + 0.5, 231, pos.y + 0.5);

        final Schematic map = maps.get(r.nextInt(maps.size()));
        final com.sk89q.worldedit.world.World worldeditWorld = BukkitUtil.getLocalWorld(w);
        final com.sk89q.worldedit.Vector worldeditVector = BukkitUtil.toVector(midLoc.clone().subtract(0, 1, 0));
        map.paste(worldeditWorld, worldeditVector);
    }

    public List<Player> getPlayers() {
        final List<Player> list = new ArrayList<>();
        list.addAll(players);
        list.addAll(dead);
        return list;
    }

    public void start() {
        refreshGame();
        started = true;


        updateLives();

        tasks.add(TaskUtil.timer(midSpawnSecs*20, midSpawnSecs*20, () -> {
            final ItemStack itemToGive = Items.midLoot.get(r.nextInt(Items.midLoot.size()));
            final Firework fw = (Firework) w.spawnEntity(midLoc, EntityType.FIREWORK);
            final FireworkMeta fwm = fw.getFireworkMeta();

            final TeamColor randomColor = TeamColor.values()[r.nextInt(TeamColor.values().length)];
            final FireworkEffect effect = FireworkEffect.builder().withColor(randomColor.color).build();
            fwm.addEffect(effect);
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);

            TaskUtil.laterAsync(2, fw::detonate);

            final Item i = w.dropItem(midLoc, itemToGive);
            i.setVelocity(new Vector(0, 0, 0));
            i.setCustomName(itemToGive.getItemMeta().getDisplayName());
            i.setCustomNameVisible(true);


            for (Player p1 : getPlayers()) {
                p1.sendMessage(color(itemToGive.getItemMeta().getDisplayName() + " &7has spawned in middle!"));
            }

            if (!hasRainedTNT && r.nextInt(100) < 5) {
                hasRainedTNT = true;

                for (Player p1 : getPlayers()) {
                    p1.playSound(p1.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 0.25f);
                    p1.sendMessage(color("&7(&c&l!&7) &6TNT is raining from the sky!"));
                    title(p1, "", color("&6TNT is raining from the sky!"), 0, 40, 20);
                }

                tasks.add(new BukkitRunnable() {
                    int i = Game.tntRainSecs;
                    @Override
                    public void run() {
                        i--;
                        if (i <= 0) {
                            cancel();
                            return;
                        }
                        for (Player p : players) {
                            final TNTPrimed tnt = (TNTPrimed) w.spawnEntity(p.getLocation(), EntityType.PRIMED_TNT);
                            p.getWorld().playSound(p.getLocation(), Sound.FUSE, 1, 1);
                            tnt.setFuseTicks(3*20);
                            tnt.setVelocity(new Vector(0, 0, 0));
                        }
                    }
                }.runTaskTimer(instance, 0, 20));
            }
        }));

        tasks.add(TaskUtil.timerAsync(everywhereSpawnSecs * 20, everywhereSpawnSecs * 20, () -> {
            final ItemStack itemToGive = Items.everywhereLoot.get(r.nextInt(Items.everywhereLoot.size()));
            for (Player p1 : getPlayers()) {
                p1.sendMessage(color("&7Everyone was given a " + itemToGive.getItemMeta().getDisplayName() + "&7!"));
                if (p1.getGameMode() == GameMode.SURVIVAL) p1.getInventory().addItem(itemToGive);
            }
        }));

        startTime = System.currentTimeMillis();
        ParticleTypeMotion particle = particles.FLAME();
        tasks.add(new BukkitRunnable() {
            double skyBorderTarget = 250;
            double skyBorderHeight = 260;

            @Override
            public void run() {
                if (skyBorderHeight == 260) for (Player p1 : getPlayers()) {
                    p1.playSound(p1.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 0.25f);
                    p1.sendMessage(color("&7(&c&l!&7) &6The sky is falling in!"));
                    title(p1, "", color("&6The sky is falling in!"), 0, 40, 20);
                }

                if (skyBorderTarget == 250 && System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(5)) {
                    skyBorderTarget = 240;

                    for (Player p1 : getPlayers()) {
                        p1.playSound(p1.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 0.25f);
                        p1.sendMessage(color("&7(&c&l!&7) &6The sky is falling in!"));
                        title(p1, "", color("&6The sky is falling in!"), 0, 40, 20);
                    }
                }

                for (Player player : getPlayers()) {
                    for (double x = player.getLocation().getX() - 3; x <= player.getLocation().getX() + 3; x++) {
                        for (double z = player.getLocation().getZ() - 3; z <= player.getLocation().getZ() + 3; z++) {
                            particles.sendPacket(getPlayers(), particle.packet(true, x, skyBorderHeight, z));
                        }
                    }
                    if (player.getEyeLocation().getY() >= skyBorderHeight && player.getGameMode() == GameMode.SURVIVAL) {
                        playerDied(player);
                    }
                }
                if (Math.round(skyBorderHeight) > Math.round(skyBorderTarget)) {
                    skyBorderHeight = skyBorderHeight - 0.05;
                } else if (Math.round(skyBorderHeight) < Math.round(skyBorderTarget)) {
                    skyBorderHeight = skyBorderHeight + 0.05;
                }
            }
        }.runTaskTimer(instance, 3*60*20, 5));

        for (Player player : getPlayers()) {
            respawn(player);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
        }
    }

    public void addPlayer(Player p) {
        p.getInventory().clear();
        p.teleport(midLoc);
        p.setGameMode(GameMode.SPECTATOR);

        players.add(p);
        gameMap.put(p, this);

        TeamColor teamColor = TeamColor.values()[r.nextInt(TeamColor.values().length)];

        if (p.getName().equalsIgnoreCase("emortl")) teamColor = TeamColor.PURPLE;
        else if (p.getName().equalsIgnoreCase("iternalplayer")) teamColor = TeamColor.RED;

        statMap.put(p, new PlayerStats(p, teamColor));
        p.setDisplayName(teamColor.chatColor + "" + p.getName());

        final ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        final LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestMeta.spigot().setUnbreakable(true);
        chestMeta.setColor(teamColor.color);
        chestplate.setItemMeta(chestMeta);
        p.getInventory().setChestplate(chestplate);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (players.contains(player)) continue;
            player.hidePlayer(p);
            p.hidePlayer(player);
        }
        for (Player player : getPlayers()) {
            player.showPlayer(p);
            p.showPlayer(player);
        }

        if (!starting && getPlayers().size() >= playersNeededToStart) {
            starting = true;
            tasks.add(gameStartTask = new BukkitRunnable() {
                int i = gameStartSecs;
                @Override
                public void run() {
                    if (i < 1) {
                        start();
                        cancel();
                        return;
                    }

                    if (i % 5 == 0 || i < 5) {
                        for (Player player : getPlayers()) {
                            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                            player.sendMessage(color("&6Game starting in &a&l" + i + " &6seconds!"));
                            title(player, color("&a" + i), "", 0, 30, 10);
                        }
                    }

                    i--;
                }
            }.runTaskTimer(instance, 0, 20));
        }
        if (getPlayers().size() == maxPlayers) {
            refreshGame();
            gameStartTask.cancel();
            start();
        }
    }
    public void removePlayer(Player p) {
        players.remove(p);
        dead.remove(p);
        statMap.remove(p);
        gameMap.remove(p);

        if (getPlayers().size() > 0) updateLives();
        if (getPlayers().size() <= 1) {
            if (!started && starting) {
                gameStartTask.cancel();
                starting = false;
                players.get(0).sendMessage(color("&cStart cancelled! Not enough players."));
                players.get(0).playSound(players.get(0).getLocation(), Sound.CLICK, 1f, 1f);
                return;
            }
            victory(players.get(0));
            return;
        }
    }

    public Location getRandomSpawnLocation() {
        final double angle = r.nextDouble() * 360;
        final int x = (int) (Math.cos(angle) * (gameWidth - 2));
        final int z = (int) (Math.sin(angle) * (gameWidth - 2));

        final Location loc = midLoc.clone().add(x, -2, z);
        final Vector angle1 = midLoc.clone().subtract(loc).toVector();

        loc.setDirection(angle1);
        loc.setPitch(90);

        w.getBlockAt(loc.clone().add(0, 1, 0)).setType(Material.AIR);
        w.getBlockAt(loc.clone().add(0, 2, 0)).setType(Material.AIR);

        final Block b = w.getBlockAt(loc);
        b.setType(Material.BEDROCK);
        TaskUtil.later(20*5, () -> { b.setType(Material.WOOL); });

        return loc.clone().add(0, 1, 0);
    }

    public void playerDied(Player playerWhoDied) {
        if (victorying) return;

        playerWhoDied.closeInventory();
        playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.VILLAGER_DEATH, 0.5f, 1.5f);
        playerWhoDied.setGameMode(GameMode.SPECTATOR);
        playerWhoDied.getInventory().clear();

        final PlayerStats stats = statMap.get(playerWhoDied);
        if (stats.spawnProtectionTask != null) {
            stats.spawnProtectionTask.cancel();
            stats.spawnProtectionTask = null;
        }
        stats.lives--;

        final boolean wasFinal = stats.lives <= 0;

        final Player lastHitBy = stats.getLastHitBy();
        if (lastHitBy != null) {
            if (playerWhoDied == lastHitBy) {
                for (Player p : getPlayers()) {
                    p.sendMessage(color(playerWhoDied.getDisplayName() + " &7killed themselves. " + (wasFinal ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));
                }
            } else {
                lastHitBy.playSound(lastHitBy.getLocation(), Sound.NOTE_PLING, 1, 1);
                lastHitBy.sendMessage(color("&7You killed " + playerWhoDied.getDisplayName() + "&7! " + (wasFinal ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));

                statMap.get(lastHitBy).kills++;

                for (Player p : getPlayers()) {
                    if (p == lastHitBy) continue;
                    p.sendMessage(color(playerWhoDied.getDisplayName() + " &7was killed by " + lastHitBy.getDisplayName() + "&7. " + (stats.lives == 0 ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));
                }
            }
        } else {
            for (Player p : getPlayers()) {
                p.sendMessage(color(playerWhoDied.getDisplayName() + " &7died. " + (wasFinal ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));
            }
        }

        updateLives();
        title(playerWhoDied, color("&c&lYOU DIED!"), color(lastHitBy == null ? "" : lastHitBy == playerWhoDied ? "&7You killed yourself" : "&7Killed by " + lastHitBy.getDisplayName()), 0, 20, 10);

        if (wasFinal) {
            dead.add(playerWhoDied);
            players.remove(playerWhoDied);

            final Firework fw = (Firework) w.spawnEntity(playerWhoDied.getLocation(), EntityType.FIREWORK);
            final FireworkMeta fwm = fw.getFireworkMeta();

            final FireworkEffect effect = FireworkEffect.builder().withColor(stats.teamColor.color).build();
            fwm.addEffect(effect);
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);

            TaskUtil.laterAsync(2, fw::detonate);

            if (players.size() <= 1) {
                victory(players.get(0));
            }
            return;
        }

        tasks.add(new BukkitRunnable() {
            int i = respawnSecs - 1;

            @Override
            public void run() {
                if (i == respawnSecs - 1 && lastHitBy != null && lastHitBy != playerWhoDied) {
                    playerWhoDied.setSpectatorTarget(lastHitBy);
                }
                i--;
                if (i < 1) {
                    respawn(playerWhoDied);
                    cancel();
                    return;
                }

                playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.CLICK, 1, 1);
                title(playerWhoDied, color((i == 3 ? ChatColor.RED : i == 2 ? ChatColor.GOLD : ChatColor.GREEN) + "" + i), color(lastHitBy == null ? "" : "&7Spectating " + lastHitBy.getDisplayName()), 0, 20, 10);
            }
        }.runTaskTimer(instance, 20, 20));
    }

    public void respawn(Player playerWhoDied) {
        if (victorying) return;
        playerWhoDied.setLevel(0);

        final PlayerStats stats = statMap.get(playerWhoDied);

        for (PotionEffect potion : playerWhoDied.getActivePotionEffects()) {
            playerWhoDied.removePotionEffect(potion.getType());
        }
        playerWhoDied.getInventory().clear();
        playerWhoDied.getInventory().setItem(1, new ItemStack(Material.WOOL, (byte)64, (short) stats.teamColor.woolColor));
        playerWhoDied.getInventory().setItem(2, Items.shears);
        playerWhoDied.getInventory().setHeldItemSlot(1);

        playerWhoDied.teleport(getRandomSpawnLocation());
        playerWhoDied.setGameMode(GameMode.SURVIVAL);

        stats.spawnProtectionTask = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;

                if (i >= (respawnSecs * 4)) {
                    cancel();
                    stats.spawnProtectionTask = null;

                    actionbar(playerWhoDied, "&8Your spawn protection has worn off");
                    playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.FIZZ, 0.25f, 1);

                    for (byte a = 0; a < 40; a++) {
                        final Object particlePacket = particles.SMOKE_LARGE().packetMotion(false, playerWhoDied.getLocation().clone().add(r.nextDouble()*0.2, 0.5+(r.nextDouble()*0.2), r.nextDouble()*0.2), Vector.getRandom().multiply(0.3));
                        particles.sendPacket(getPlayers(), particlePacket);
                    }

                    return;
                }
                if (i % 4 == 0) {
                    actionbar(playerWhoDied, "&8Your spawn protection wears off in &l" + ((respawnSecs * 4) - i) / 4 + "&8 seconds");
                    playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.LAVA_POP, 0.25f, 2);
                }

                for (double a = 0; a < Math.PI * 2; a+= Math.PI / 5) {
                    final Object particlePacket = particles.VILLAGER_HAPPY().packet(false, playerWhoDied.getLocation().clone().add(Math.cos(a), 1, Math.sin(a)));
                    particles.sendPacket(getPlayers(), particlePacket);
                }

            }
        }.runTaskTimer(instance, 0, 5);

        tasks.add(stats.spawnProtectionTask);
    }

    public void victory(Player winner) {
        if (victorying) return;
        victorying = true;

        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        if (diamondBlockTask != null) diamondBlockTask.cancel();

        final List<PlayerStats> killList = new ArrayList<>(statMap.values());

        killList.sort(Comparator.comparingInt(a -> a.kills));
        Collections.reverse(killList);
        statMap.clear();

        final StringBuilder sb = new StringBuilder();
        sb.append("&8&m" + Strings.repeat(" ", 50) + "&r\n");
        sb.append(Strings.repeat(" ", 20) + "%WINMSG%");
        sb.append("\n&r");
        sb.append(Strings.repeat(" ", 15) + "&7Winner: " + winner.getDisplayName());
        sb.append(" &r\n ");
        for (byte i = 0; i < Math.min(killList.size(), 3); i++) {
            sb.append("\n    " + (i == 0 ? ChatColor.GREEN + "1st" : i == 1 ? ChatColor.GOLD + "2nd" : ChatColor.RED + "3rd") + " &7- " + killList.get(i).player.getDisplayName() + " &7- " + killList.get(i).kills);
        }
        sb.append("\n \n&8&m" + Strings.repeat(" ", 50));

        final String s = sb.toString();

        for (Player player : getPlayers()) {
            final String winMsg = player == winner ? "&6&lVICTORY" : "&c&lDEFEAT";
            title(player, color(winMsg), ChatColor.GRAY + "" + winMessages[r.nextInt(winMessages.length)], 0, 100, 0);

            gameMap.remove(player);
            player.sendMessage(color(s.replace("%WINMSG%", winMsg)));
            player.setGameMode(GameMode.SPECTATOR);
        }

        TaskUtil.later(5 * 20, () -> {
            gamePositions.remove(pos);
            for (Player player : getPlayers()) {
                nextGame.addPlayer(player);
            }
        });
    }

    public void updateLives() {
        final List<PlayerStats> sortedStats = new ArrayList<>(statMap.values());
        final List<String> lines = new ArrayList<>();

        sortedStats.sort(Comparator.comparingInt(a -> a.lives));
        Collections.reverse(sortedStats);
        lines.add(" ");
        for (PlayerStats value : sortedStats) {
            if (value.lives < 1) continue;
            lines.add(color(value.player.getDisplayName() + " &8| " + value.coloredLives() + " &8(" + value.kills + ")"));
        }

        lines.add("");
        lines.add(color("&8emortal.live &m        "));

        for (PlayerStats value : sortedStats) {
            value.board.setAll(lines.toArray(new String[0]));

            for (PlayerStats value1 : sortedStats) {
                Team t = value.player.getScoreboard().getTeam(value1.player.getName());
                if (t == null) t = value.player.getScoreboard().registerNewTeam(value1.player.getName());

                t.setPrefix(value1.teamColor.chatColor + "");
                t.setSuffix(color(" &8| " + value1.coloredLives()));
                t.addEntry(value1.player.getName());
            }
        }
    }

}
