package com.moyskleytech.mc.BuildBattle.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.moyskleytech.mc.BuildBattle.BuildBattle;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public abstract class Scheduler {
    public static interface Task {
        void cancel();
    }

    private static Scheduler instance;

    public static Scheduler getInstance() {
        if (instance == null) {
            if (isFolia()) {
                instance = new FoliaScheduler();
            } else
                instance = new BukkitScheduler();
        }
        return instance;
    }

    public static class FoliaScheduler extends Scheduler {
        public static class FoliaTask implements Task {
            ScheduledTask data;

            public FoliaTask(ScheduledTask innerTask) {
                data = innerTask;
            }

            @Override
            public void cancel() {
                data.cancel();
            }

        }

        @Override
        public void runTask(Runnable r) {
            Bukkit.getServer().getGlobalRegionScheduler().execute(BuildBattle.getInstance(), r);
        }

        @Override
        public Task runEntityTask(Entity e, long delay, Runnable r) {
            if (delay <= 0)
                delay = 1;
            if (e != null)
                return new FoliaTask(e.getScheduler().runDelayed(BuildBattle.getInstance(), (task) -> {
                    r.run();
                }, null, delay));
            return null;
        }

        @Override
        public Task runTaskAsync(Consumer<Task> r) {
            final FoliaTask st = new FoliaTask(
                    Bukkit.getServer().getAsyncScheduler().runNow(BuildBattle.getInstance(), (task) -> {
                        r.accept(null);
                    }));
            return st;
        }

        @Override
        public Task runChunkTask(Location e, long delay, Runnable r) {
            if (delay == 0) {
                Bukkit.getServer().getRegionScheduler().execute(BuildBattle.getInstance(), e, () -> {
                    r.run();
                });
                return null;
            }
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getRegionScheduler().runDelayed(BuildBattle.getInstance(), e, (task) -> {
                        r.run();
                    }, delay));
        }

        @Override
        public Task runTaskLater(Runnable r, long delay) {
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getGlobalRegionScheduler().runDelayed(BuildBattle.getInstance(), (task) -> {
                        r.run();
                    }, delay));
        }

        @Override
        public Task runTaskTimer(Consumer<Task> r, long delay, long period) {
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(BuildBattle.getInstance(), (task) -> {
                        r.accept(new FoliaTask(task));
                    }, delay, period));
        }

        @Override
        public Task runTaskTimerAsync(Consumer<Task> r, long delay, long period) {
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getAsyncScheduler().runAtFixedRate(BuildBattle.getInstance(), (task) -> {
                        r.accept(new FoliaTask(task));
                    }, (long) (delay * 50), (long) (period * 50), TimeUnit.MILLISECONDS));
        }

        @Override
        public Task runEntityTaskTimer(Entity e, long delay, long prerio, Consumer<Task> r) {
            if (delay <= 0)
                delay = 1;
            if (e != null)
                return new FoliaTask(e.getScheduler().runAtFixedRate(BuildBattle.getInstance(), (task) -> {
                    r.accept(new FoliaTask(task));
                }, null, delay, prerio));
            return null;
        }

        @Override
        public Task runChunkTaskTimer(Location e, long delay, long preriod, Consumer<Task> r) {
            if (delay <= 0)
                delay = 1;
            return new FoliaTask(
                    Bukkit.getServer().getRegionScheduler().runAtFixedRate(BuildBattle.getInstance(), e, (task) -> {
                        r.accept(new FoliaTask(task));
                    }, delay, preriod));
        }

    }

    @SuppressWarnings("deprecation")
    public static class BukkitScheduler extends Scheduler {
        public static class NormalTask implements Task {
            BukkitTask data;

            public NormalTask(BukkitTask innerTask) {
                data = innerTask;
            }

            @Override
            public void cancel() {
                data.cancel();
            }

        }

        public static class RunnableTask implements Task {
            BukkitRunnable data;

            public RunnableTask(BukkitRunnable innerTask) {
                data = innerTask;
            }

            @Override
            public void cancel() {
                data.cancel();
            }

        }

        @Override
        public void runTask(Runnable r) {
            Bukkit.getScheduler().runTask(BuildBattle.getInstance(), r);
        }

        @Override
        public Task runEntityTask(Entity e, long delay, Runnable r) {
            return this.runTaskLater(r, delay);
        }

        @Override
        public Task runTaskAsync(Consumer<Task> r) {
            NormalTask nt = new NormalTask(
                    Bukkit.getScheduler().runTaskAsynchronously(BuildBattle.getInstance(), () -> {
                        r.accept(null);
                    }));
            return nt;
        }

        @Override
        public Task runChunkTask(Location e, long delay, Runnable r) {
            if (delay <= 0) {
                return new NormalTask(Bukkit.getScheduler().runTask(BuildBattle.getInstance(), r));
            }
            return this.runTaskLater(r, delay);
        }

        @Override
        public Task runTaskLater(Runnable r, long delay) {
            if (delay <= 0) {
                return new NormalTask(Bukkit.getScheduler().runTask(BuildBattle.getInstance(), r));
            }
            return new NormalTask(Bukkit.getScheduler().runTaskLater(BuildBattle.getInstance(), r, delay));
        }

        @Override
        public Task runTaskTimer(Consumer<Task> r, long delay, long period) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    r.accept(new RunnableTask(this));
                }
            };
            return new NormalTask(runnable.runTaskTimer(BuildBattle.getInstance(), delay, period));
        }

        @Override
        public Task runTaskTimerAsync(Consumer<Task> r, long delay, long period) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    r.accept(new RunnableTask(this));
                }
            };
            return new NormalTask(runnable.runTaskTimerAsynchronously(BuildBattle.getInstance(), delay, period));
        }

        @Override
        public Task runEntityTaskTimer(Entity e, long delay, long prerio, Consumer<Task> r) {
            return runTaskTimer(r, delay, prerio);
        }

        @Override
        public Task runChunkTaskTimer(Location e, long delay, long preriod, Consumer<Task> r) {
            return runTaskTimer(r, delay, preriod);
        }

    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public abstract void runTask(Runnable r);

    public abstract Task runTaskLater(Runnable r, long delay);

    public abstract Task runTaskAsync(Consumer<Task> r);

    public abstract Task runTaskTimer(Consumer<Task> r, long delay, long period);

    public abstract Task runTaskTimerAsync(Consumer<Task> r, long delay, long period);

    public abstract Task runEntityTask(Entity e, long delay, Runnable r);

    public abstract Task runEntityTaskTimer(Entity e, long delay, long prerio, Consumer<Task> r);

    public abstract Task runChunkTask(Location e, long delay, Runnable r);

    public abstract Task runChunkTaskTimer(Location e, long delay, long preriod, Consumer<Task> r);

}
