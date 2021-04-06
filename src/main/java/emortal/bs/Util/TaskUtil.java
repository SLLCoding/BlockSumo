package emortal.bs.Util;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static emortal.bs.Main.instance;

public class TaskUtil {
    public static void async(Runnable run) {
        instance.getServer().getScheduler().runTaskAsynchronously(instance, run);
    }
    public static BukkitTask later(long delay, Runnable run) {
        return instance.getServer().getScheduler().runTaskLater(instance, run, delay);
    }
    public static BukkitTask timer(long delay, long period, Runnable run) {
        return instance.getServer().getScheduler().runTaskTimer(instance, run, delay, period);
    }
    public static BukkitTask loop(long delay, long period, long repeats, Runnable run, Runnable runOnFinish) {
        return new BukkitRunnable() {
            long i = repeats;
            @Override
            public void run() {
                i--;
                if (i < 1) {
                    runOnFinish.run();
                    cancel();
                }
                run.run();
            }
        }.runTaskTimer(instance, delay, period);
    }

    public static BukkitTask laterAsync(long delay, Runnable run) {
        return instance.getServer().getScheduler().runTaskLaterAsynchronously(instance, run, delay);
    }
    public static BukkitTask timerAsync(long delay, long period, Runnable run) {
        return instance.getServer().getScheduler().runTaskTimerAsynchronously(instance, run, delay, period);
    }
    public static BukkitTask loopAsync(long delay, long period, long repeats, Runnable run, Runnable runOnFinish) {
        return new BukkitRunnable() {
            long i = repeats;
            @Override
            public void run() {
                i--;
                if (i < 1) {
                    runOnFinish.run();
                    cancel();
                }
                run.run();
            }
        }.runTaskTimerAsynchronously(instance, delay, period);
    }
}
