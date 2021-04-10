package emortal.bs.games;

import com.boydti.fawe.object.schematic.Schematic;
import com.github.fierioziy.particlenativeapi.api.types.ParticleType;
import com.google.common.base.Strings;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import emortal.bs.PlayerStats;
import emortal.bs.TeamColor;
import emortal.bs.Util.Items;
import emortal.bs.Util.TaskUtil;
import emortal.bs.Util.WorldBorderUtil;
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

    private final List<Player> players = new ArrayList<>();
    private final List<Player> dead = new ArrayList<>();
    private final List<Player> gamers = new ArrayList<>();

    public final HashMap<UUID, PlayerStats> statMap = new HashMap<>();
    public final List<BukkitTask> tasks = new ArrayList<>();

    private final Random r = new Random();
    private final World w;
    public final Location midLoc;

    private boolean hasRainedTNT = false;

    private GameState state = GameState.WAITING;

    private long startTime;
    public double skyBorderTarget = 250;
    public double skyBorderHeight = 260;

    private final GamePosition pos;
    public BukkitTask gameStartTask = null;
    public BukkitTask diamondBlockTask = null;
    public Player diamondBlockPlayer = null;

    private final GameOptions options;
    private final int id;

    public Game(World w, GamePosition pos, GameOptions options) {
        int supposedId = r.nextInt(1000);
        boolean exists = GameManager.getGames().size() != 0;
        while (exists) {
            for (Game game : GameManager.getGames()) {
                if (game.equals(this)) continue;
                if (game.getId() == supposedId) supposedId = r.nextInt(1000);
                else {
                    exists = false;
                    break;
                }
            }
        }
        id = supposedId;
        this.pos = pos;
        this.w = w;
        this.midLoc = new Location(w, pos.x + 0.5, 231, pos.y + 0.5);
        if (options != null) this.options = options;
        else this.options = new GameOptions();

        final Schematic map = maps.get(r.nextInt(maps.size()));
        final com.sk89q.worldedit.world.World worldeditWorld = BukkitUtil.getLocalWorld(w);
        final com.sk89q.worldedit.Vector worldeditVector = BukkitUtil.toVector(midLoc.clone().subtract(0, 1, 0));
        map.paste(worldeditWorld, worldeditVector);
    }

    public void start() {
        state = GameState.PLAYING;

        updateLives();

        tasks.add(TaskUtil.timer(options.getMidSpawnTimer() * 20L, options.getMidSpawnTimer() * 20L, () -> {
            final ItemStack itemToGive = Items.midLoot.get(r.nextInt(Items.midLoot.size()));
            final Firework fw = (Firework) w.spawnEntity(midLoc, EntityType.FIREWORK);
            final FireworkMeta fwm = fw.getFireworkMeta();

            final TeamColor randomColor = TeamColor.values()[r.nextInt(TeamColor.values().length)];
            final FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(randomColor.color).build();
            fwm.addEffect(effect);
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);

            TaskUtil.laterAsync(2, fw::detonate);

            final Item i = w.dropItem(midLoc, itemToGive);
            i.setVelocity(new Vector(0, 0.2, 0));
            i.setPickupDelay(0);
            i.setCustomName(itemToGive.getItemMeta().getDisplayName());
            i.setCustomNameVisible(true);


            for (Player p1 : gamers) {
                p1.sendMessage(color(itemToGive.getItemMeta().getDisplayName() + " &7has spawned in middle!"));
            }

            if (!hasRainedTNT && r.nextInt(100) < 5) {
                hasRainedTNT = true;

                for (Player p1 : gamers) {
                    p1.playSound(p1.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                    p1.sendMessage(color("&c(&l!&c) &6TNT is raining from the sky!"));
                    title(p1, "", color("&6TNT is raining from the sky!"), 0, 40, 20);
                }

                tasks.add(new BukkitRunnable() {
                    int i = options.getTntRainTimer();
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

        tasks.add(TaskUtil.timerAsync(options.getEverywhereSpawnTimer() * 20L, options.getEverywhereSpawnTimer() * 20L, () -> {
            final ItemStack itemToGive = Items.everywhereLoot.get(r.nextInt(Items.everywhereLoot.size()));
            for (Player p1 : gamers) {
                p1.sendMessage(color("&7Everyone was given a " + itemToGive.getItemMeta().getDisplayName() + "&7!"));
                if (p1.getGameMode() == GameMode.SURVIVAL) p1.getInventory().addItem(itemToGive);
            }
        }));

        startTime = System.currentTimeMillis();
        if (options.hasSkyBorder()) {
            ParticleType particle = particles.BARRIER();
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    if (skyBorderHeight == 260) for (Player p1 : gamers) {
                        p1.playSound(p1.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                        p1.sendMessage(color("&c(&l!&c) &6The sky is falling in!"));
                        title(p1, "", color("&6The sky is falling in!"), 0, 40, 20);
                    }

                    if (skyBorderTarget == 250 && System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(4)) {
                        skyBorderTarget = 240;

                        for (Player p1 : gamers) {
                            p1.playSound(p1.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                            p1.sendMessage(color("&c(&l!&c) &6The sky is falling in!"));
                            title(p1, "", color("&6The sky is falling in!"), 0, 40, 20);
                        }
                    }

                    for (Player player : players) {
                        for (double x = player.getLocation().getX() - 3; x <= player.getLocation().getX() + 3; x++) {
                            for (double z = player.getLocation().getZ() - 3; z <= player.getLocation().getZ() + 3; z++) {
                                particles.sendPacket(gamers, particle.packet(true, x, skyBorderHeight, z));
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
            }.runTaskTimer(instance, 2*60*20, 5));
        }

        for (Player player : gamers) {
            respawn(player);
            title(player, "&a&lGO", "", 0, 20, 20);
            player.playSound(player.getLocation(), Sound.EXPLODE, 1, 1);
        }
    }

    public void stop() {
        if (state.equals(GameState.ENDING)) return;
        state = GameState.ENDING;

        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        if (diamondBlockTask != null) diamondBlockTask.cancel();

        statMap.clear();

        for (Player player : gamers) {
            title(player, color("&6&lDRAW"), ChatColor.GRAY + "Mysterious things happened", 0, 100, 0);

            GameManager.getPlayerToGame().remove(player.getUniqueId(), this);
            player.setGameMode(GameMode.SPECTATOR);
        }

        TaskUtil.later(5 * 20, () -> {
            GameManager.getGames().remove(this);
            GameManager.getGamePositions().remove(pos);
            for (Player player : gamers) {
                GameManager.addPlayer(player);
            }
        });
    }

    public void addPlayer(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(midLoc);
        p.getInventory().clear();

        if (state.equals(GameState.PLAYING) || state.equals(GameState.ENDING)) dead.add(p);
        else players.add(p);
        gamers.add(p);
        GameManager.getPlayerToGame().put(p.getUniqueId(), this);

        TeamColor teamColor = TeamColor.values()[r.nextInt(TeamColor.values().length)];

        if (p.getName().equalsIgnoreCase("emortl")) teamColor = TeamColor.PURPLE;
        else if (p.getName().equalsIgnoreCase("iternalplayer")) teamColor = TeamColor.RED;
        else if (p.getName().equalsIgnoreCase("superlegoluis")) teamColor = TeamColor.ORANGE;

        PlayerStats playerStats = new PlayerStats(p, teamColor);
        if (dead.contains(p)) playerStats.lives = (byte) 0;
        else if (players.contains(p)) playerStats.lives = (byte) options.getStartingLives();
        statMap.put(p.getUniqueId(), playerStats);
        p.setDisplayName(teamColor.chatColor + "" + p.getName());

        final ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        final LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestMeta.spigot().setUnbreakable(true);
        chestMeta.setColor(teamColor.color);
        chestplate.setItemMeta(chestMeta);
        p.getInventory().setChestplate(chestplate);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (players.contains(player)) continue;
            if (player.canSee(p)) player.hidePlayer(p);
            if (p.canSee(player)) p.hidePlayer(player);
        }
        for (Player player : gamers) {
            if (!player.canSee(p)) player.showPlayer(p);
            if (!p.canSee(player)) p.showPlayer(player);
        }

        for (Player player : gamers) {
            player.sendMessage(color("&8(&a" + gamers.size() + "&8/&a" + options.getMaxPlayers() + "&8) " + p.getDisplayName() + "&7 joined"));
        }

        if (state.equals(GameState.WAITING) && gamers.size() >= playersNeededToStart) {
            state = GameState.STARTING;
            tasks.add(gameStartTask = new BukkitRunnable() {
                int i = options.getGameStartTimer();
                @Override
                public void run() {
                    if (i < 1) {
                        start();
                        cancel();
                        return;
                    }

                    if (i % 5 == 0 || i < 5) {
                        for (Player player : gamers) {
                            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                            player.sendMessage(color("&6Game starting in &a&l" + i + " &6seconds!"));
                            if (i == 3) player.playSound(player.getLocation(), Sound.FUSE, 1, 1);
                            title(player, color("&a" + i), "", 0, 0, 20);
                        }
                    }

                    i--;
                }
            }.runTaskTimer(instance, 0, 20));
        }
        if (gamers.size() == options.getMaxPlayers()) {
            gameStartTask.cancel();
            start();
        }
    }
    public void removePlayer(Player p) {
        players.remove(p);
        dead.remove(p);
        gamers.remove(p);

        PlayerStats stats = statMap.get(p.getUniqueId());
        if (stats.spawnProtectionTask != null) stats.spawnProtectionTask.cancel();
        if (stats.respawnTask != null) stats.respawnTask.cancel();

        statMap.remove(p.getUniqueId());
        GameManager.getPlayerToGame().remove(p.getUniqueId(), this);

        if (gamers.size() > 0) updateLives();
        if (gamers.size() == 1) {
            if (state.equals(GameState.STARTING)) {
                gameStartTask.cancel();
                state = GameState.WAITING;
                players.get(0).sendMessage(color("&cStart cancelled! Not enough players."));
                players.get(0).playSound(players.get(0).getLocation(), Sound.CLICK, 1f, 1f);
                return;
            }
            victory(players.get(0));
        } else if (gamers.size() < 1) {
            if (state.equals(GameState.STARTING)) {
                gameStartTask.cancel();
                state = GameState.WAITING;
            }
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
        if (state.equals(GameState.ENDING)) return;

        playerWhoDied.closeInventory();
        playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.VILLAGER_DEATH, 0.5f, 1.5f);
        playerWhoDied.setGameMode(GameMode.SPECTATOR);
        playerWhoDied.getInventory().clear();

        final PlayerStats stats = statMap.get(playerWhoDied.getUniqueId());
        if (stats.spawnProtectionTask != null) {
            stats.spawnProtectionTask.cancel();
            stats.spawnProtectionTask = null;
        }
        stats.lives--;

        if (stats.lives == 1) {
            WorldBorderUtil.show(playerWhoDied);
        } else {
            WorldBorderUtil.hide(playerWhoDied);
        }

        final boolean wasFinal = stats.lives <= 0;

        final Player lastHitBy = stats.getLastHitBy();
        if (lastHitBy != null) {
            if (playerWhoDied == lastHitBy) {
                for (Player p : gamers) {
                    p.sendMessage(color(playerWhoDied.getDisplayName() + " &7killed themselves. " + (wasFinal ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));
                }
            } else {
                lastHitBy.playSound(lastHitBy.getLocation(), Sound.NOTE_PLING, 1, 1);
                lastHitBy.sendMessage(color("&7You killed " + playerWhoDied.getDisplayName() + "&7! " + (wasFinal ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));

                statMap.get(lastHitBy.getUniqueId()).kills++;

                for (Player p : gamers) {
                    if (p == lastHitBy) continue;
                    p.sendMessage(color(playerWhoDied.getDisplayName() + " &7was killed by " + lastHitBy.getDisplayName() + "&7. " + (stats.lives == 0 ? "&b&lFINAL KILL" : stats.coloredLives() + " " + (stats.lives == 1 ? "life" : "lives") + " left")));
                }
            }
        } else {
            for (Player p : gamers) {
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

            final FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(stats.teamColor.color).build();
            fwm.addEffect(effect);
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);

            TaskUtil.laterAsync(2, fw::detonate);

            if (players.size() <= 1) {
                victory(players.get(0));
            }
            return;
        }

        tasks.add(stats.respawnTask = new BukkitRunnable() {
            int i = options.getRespawnTime() - 1;

            @Override
            public void run() {
                if (i == options.getRespawnTime() - 1 && lastHitBy != null && lastHitBy != playerWhoDied) {
                    playerWhoDied.setSpectatorTarget(lastHitBy);
                }
                i--;
                if (i < 1) {
                    respawn(playerWhoDied);
                    cancel();
                    return;
                }

                playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.CLICK, 1, 1);
                title(playerWhoDied, color((i == 3 ? ChatColor.RED : i == 2 ? ChatColor.GOLD : ChatColor.GREEN) + "" + i), color(lastHitBy == null || lastHitBy == playerWhoDied ? "" : "&7Spectating " + lastHitBy.getDisplayName()), 0, 20, 10);
            }
        }.runTaskTimer(instance, 20, 20));
    }

    public void respawn(Player playerWhoDied) {
        if (state.equals(GameState.ENDING)) return;
        playerWhoDied.setLevel(0);

        final PlayerStats stats = statMap.get(playerWhoDied.getUniqueId());

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

                if (i > (options.getRespawnTime() * 4)) {
                    cancel();
                    stats.spawnProtectionTask = null;

                    actionbar(playerWhoDied, "&8Your spawn protection has worn off");
                    playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.FIZZ, 0.25f, 1);

                    for (byte a = 0; a < 40; a++) {
                        final Object particlePacket = particles.SMOKE_LARGE().packetMotion(false, playerWhoDied.getLocation().clone().add(r.nextDouble()*0.2, 0.5+(r.nextDouble()*0.2), r.nextDouble()*0.2), Vector.getRandom().multiply(0.3));
                        particles.sendPacket(gamers, particlePacket);
                    }

                    return;
                }
                if (i % 4 == 0) {
                    actionbar(playerWhoDied, "&8Your spawn protection wears off in &l" + ((options.getRespawnTime() * 4) - i) / 4 + "&8 seconds");
                    playerWhoDied.playSound(playerWhoDied.getLocation(), Sound.LAVA_POP, 0.25f, 2);
                }

                for (double a = 0; a < Math.PI * 2; a+= Math.PI / 5) {
                    final Object particlePacket = particles.VILLAGER_HAPPY().packet(false, playerWhoDied.getLocation().clone().add(Math.cos(a), 1, Math.sin(a)));
                    particles.sendPacket(gamers, particlePacket);
                }

            }
        }.runTaskTimer(instance, 0, 5);

        tasks.add(stats.spawnProtectionTask);
    }

    public void victory(Player winner) {
        if (state.equals(GameState.ENDING)) return;
        state = GameState.ENDING;

        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        if (diamondBlockTask != null) diamondBlockTask.cancel();

        final List<PlayerStats> killList = new ArrayList<>(statMap.values());

        killList.sort(Comparator.comparingInt(a -> a.kills));
        Collections.reverse(killList);
        statMap.clear();

        // This is a mess lol
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

        winner.setGameMode(GameMode.ADVENTURE);

        for (Player player : gamers) {
            final String winMsg = player == winner ? "&6&lVICTORY" : "&c&lDEFEAT";
            title(player, color(winMsg), ChatColor.GRAY + "" + winMessages[r.nextInt(winMessages.length)], 0, 100, 0);

            GameManager.getPlayerToGame().remove(player.getUniqueId());
            WorldBorderUtil.hide(player);

            player.sendMessage(color(s.replace("%WINMSG%", winMsg)));
            if (player != winner) player.setGameMode(GameMode.SPECTATOR);
        }

        TaskUtil.later(5 * 20, () -> {
            GameManager.getGames().remove(this);
            GameManager.getGamePositions().remove(pos);
            for (Player player : gamers) {
                GameManager.addPlayer(player);
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
            lines.add(color(value.player.getDisplayName() + " &8| " + value.coloredLives()));
        }

        lines.add("");
        lines.add(color("&6emortal.live &8&m            "));

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

    public GameState getState() {
        return state;
    }

    public List<Player> getGamers() {
        return gamers;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameOptions getOptions() {
        return options;
    }

    public int getId() {
        return id;
    }

}
