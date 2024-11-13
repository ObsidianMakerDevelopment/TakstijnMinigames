package com.moyskleytech.mc.BuildBattle.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Async.Schedule;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler.Task;

public class Paster extends Service {

    List<Task> tasks = new ArrayList<>();

    @Override
    public void onLoad() throws ServiceLoadException {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        tasks.forEach(f -> f.cancel());
        tasks.clear();
        super.onUnload();
    }

    public CompletableFuture<Void> paste(Location source, Location destination, int width, int depth, int height) {
        return paste(source, destination, width, depth, height, null);
    }

    public CompletableFuture<Void> paste(Location source, Location destination, int width, int depth, int height,
            CommandSender maybePlayerForSending) {
        AtomicInteger x = new AtomicInteger(-width);
        AtomicInteger y = new AtomicInteger(-height);
        AtomicInteger z = new AtomicInteger(-depth);

        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = 600;// ObsidianConfig.getInstance().paster().blockPerTick();
        boolean tickAware = ObsidianConfig.getInstance().paster().tickAware();
        Task task = Scheduler.getInstance().runTaskTimer(
                new Consumer<Scheduler.Task>() {
                    public Vector getNext() {
                        Vector toReturn = new Vector(x.get(), y.get(), z.get());
                        if (y.get() > height)
                            return null;
                        if (z.incrementAndGet() > depth) {
                            z.set(-depth);
                            if (x.incrementAndGet() > width) {
                                x.set(-width);
                                if (maybePlayerForSending != null) {
                                    boolean isConnected = true;
                                    if (maybePlayerForSending instanceof Player p) {
                                        isConnected = p.isConnected();
                                    }
                                    if (isConnected) {
                                        int range = height * 2;
                                        int percent = 100 * (y.get() + height) / range;
                                        maybePlayerForSending.sendActionBar(
                                                ObsidianUtil
                                                        .component("Pasting Y=" + (int) (y.get() + destination.getY())
                                                                + " | " + percent + "%"));
                                    }
                                }
                                if (y.incrementAndGet() > height) {

                                }
                            }
                        }

                        return toReturn;
                    }

                    @Override
                    public void accept(Task task) {
                        int blockPerTickAware = blockPerTick;
                        if (tickAware) {
                            blockPerTickAware = (int) Math.ceil((Bukkit.getTPS()[0] / 20.0) * blockPerTick);
                        }
                        AtomicInteger blocks = new AtomicInteger();
                        for (AtomicInteger i = new AtomicInteger(); i.get() < blockPerTickAware; i.incrementAndGet()) {

                            Vector offset = getNext();
                            if (offset == null)
                                break;
                            Location src = source.clone().add(offset.getX(), offset.getY(), offset.getZ());
                            Scheduler.getInstance().runChunkTask(src, 0, () -> {
                                Block sBlock = src.getBlock();
                                blocks.incrementAndGet();
                                if (sBlock.getType() == Material.AIR)
                                    i.decrementAndGet();
                                else {
                                    Location dst = destination.clone().add(offset.getX(), offset.getY(), offset.getZ());
                                    BlockData sBdata = sBlock.getBlockData();
                                    Scheduler.getInstance().runChunkTask(dst, 0, () -> {
                                        Block dBlock = dst.getBlock();
                                        BlockState state = dBlock.getState();
                                        state.setBlockData(sBdata);
                                        state.update(true, false);
                                    });
                                }
                            });
                            if (blocks.get() > 4 * blockPerTickAware)
                                break;
                        }
                        if (y.get() > height) {
                            Scheduler.getInstance().runTask(() -> {
                                paster.complete(null);
                            });
                        }
                    }
                }, 1, 1);

        tasks.add(task);

        return paster.thenAccept(e -> tasks.remove(task));
    }

    public CompletableFuture<Void> unpaste(Location destination, int width, int depth, int height) {
        return unpaste(destination, width, depth, height, null);
    }

    public CompletableFuture<Void> unpaste(Location destination, int width, int depth, int height,
            CommandSender maybePlayerForSending) {
        List<Vector> offsets = new LinkedList<>();
        AtomicInteger x = new AtomicInteger(-width);
        AtomicInteger y = new AtomicInteger(height);
        AtomicInteger z = new AtomicInteger(-depth);

        int minY = destination.getWorld().getMinHeight();
        int maxY = destination.getWorld().getMaxHeight();

        while (y.get() + destination.getY() > maxY) {
            y.decrementAndGet();
        }
        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = 600;// ObsidianConfig.getInstance().paster().blockPerTick();
        boolean tickAware = ObsidianConfig.getInstance().paster().tickAware();
        Task task = Scheduler.getInstance().runTaskTimer(
                new Consumer<Scheduler.Task>() {
                    public Vector getNext() {
                        Vector toReturn = new Vector(x.get(), y.get(), z.get());
                        if (y.get() < -height)
                            return null;
                        if (z.incrementAndGet() > depth) {
                            z.set(-depth);
                            if (x.incrementAndGet() > width) {
                                x.set(-width);
                                if (maybePlayerForSending != null) {
                                    boolean isConnected = true;
                                    if (maybePlayerForSending instanceof Player p) {
                                        isConnected = p.isConnected();
                                    }
                                    if (isConnected) {
                                        int range = height * 2;
                                        int percent = 100 * (height - y.get()) / range;
                                        maybePlayerForSending.sendActionBar(
                                                ObsidianUtil
                                                        .component("Pasting Y=" + (int) (y.get() + destination.getY())
                                                                + " | " + percent + "%"));
                                    }
                                }
                                if (y.decrementAndGet() < -height) {

                                }
                            }
                        }

                        return toReturn;
                    }

                    @Override
                    public void accept(Task task) {
                        int blockPerTickAware = blockPerTick;
                        if (tickAware) {
                            blockPerTickAware = (int) Math.ceil((Bukkit.getTPS()[0] / 20.0) * blockPerTick);
                        }
                        AtomicInteger blocks = new AtomicInteger();
                        for (AtomicInteger i = new AtomicInteger(); i.get() < blockPerTickAware; i.incrementAndGet()) {

                            Vector offset = getNext();
                            if (offset == null)
                                break;
                            Location destinationPos = destination.clone().add(offset.getX(), offset.getY(),
                                    offset.getZ());
                            Scheduler.getInstance().runChunkTask(destinationPos, 0, () -> {
                                Block dBlock = destinationPos.getBlock();
                                BlockState state = dBlock.getState();
                                blocks.incrementAndGet();
                                if (state.getType() != Material.AIR) {
                                    state.setType(Material.AIR);
                                    state.update(true, false);
                                } else {
                                    i.decrementAndGet();
                                }
                            });
                            if (blocks.get() > 4 * blockPerTickAware)
                                break;
                        }
                        if (y.get() < -height || y.get() + destination.getY() < minY) {
                            Scheduler.getInstance().runTask(() -> {
                                paster.complete(null);
                            });
                        }
                    }
                }, 1, 1);
        tasks.add(task);
        return paster.thenAccept(e -> tasks.remove(task));
    }

    public CompletableFuture<Void> unpaste(Location pos1, Location pos2,
            CommandSender maybePlayerForSending) {
        List<Vector> offsets = new LinkedList<>();

        Location destination = new Location(pos1.getWorld(), (pos1.getX() + pos2.getX()) / 2,
                (pos1.getY() + pos2.getY()) / 2, (pos1.getZ() + pos2.getZ()) / 2);

        int width = (int)Math.abs(pos1.getX()-pos2.getX());
        int height = (int)Math.abs(pos1.getY()-pos2.getY());
        int depth = (int)Math.abs(pos1.getZ()-pos2.getZ());

        AtomicInteger x = new AtomicInteger(-width);
        AtomicInteger y = new AtomicInteger(height);
        AtomicInteger z = new AtomicInteger(-depth);

        int minY = destination.getWorld().getMinHeight();
        int maxY = destination.getWorld().getMaxHeight();

        while (y.get() + destination.getY() > maxY) {
            y.decrementAndGet(); 
        }
        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = 600;//ObsidianConfig.getInstance().paster().blockPerTick();
        boolean tickAware = ObsidianConfig.getInstance().paster().tickAware();
        Task task = Scheduler.getInstance().runTaskTimer(
                new Consumer<Scheduler.Task>() {
                    public Vector getNext() {
                        Vector toReturn = new Vector(x.get(), y.get(), z.get());
                        if (y.get() < -height)
                            return null;
                        if (z.incrementAndGet() > depth) {
                            z.set(-depth);
                            if (x.incrementAndGet() > width) {
                                x.set(-width);
                                if (maybePlayerForSending != null) {
                                    boolean isConnected = true;
                                    if (maybePlayerForSending instanceof Player p) {
                                        isConnected = p.isConnected();
                                    }  
                                    if (isConnected) {
                                        int range = height * 2;
                                        int percent = 100 * (height-y.get() ) / range;   
                                                                
                                        maybePlayerForSending.sendActionBar(
                                                ObsidianUtil
                                                        .component("Pasting Y=" + (int)(y.get()+destination.getY()) + " | " + percent + "%"));
                                    }
                                }
                                if (y.decrementAndGet() < -height) {

                                }
                            }
                        }

                        return toReturn;
                    }

                    @Override
                    public void accept(Task task) {
                        int blockPerTickAware = blockPerTick;
                        if (tickAware) {
                            blockPerTickAware = (int) Math.ceil((Bukkit.getTPS()[0] / 20.0) * blockPerTick);
                        }
                        AtomicInteger blocks = new AtomicInteger();
                        for (AtomicInteger i = new AtomicInteger(); i.get() < blockPerTickAware; i.incrementAndGet()) {

                            Vector offset = getNext();
                            if (offset == null)
                                break;
                            Location destinationPos = destination.clone().add(offset.getX(), offset.getY(),
                                    offset.getZ());
                            Scheduler.getInstance().runChunkTask(destinationPos, 0, () -> {
                                Block dBlock = destinationPos.getBlock();
                                BlockState state = dBlock.getState();
                                blocks.incrementAndGet();
                                if (state.getType() != Material.AIR) {
                                    state.setType(Material.AIR);
                                    state.update(true, false);
                                } else {
                                    i.decrementAndGet();
                                }
                            });
                            if (blocks.get() > 4 * blockPerTickAware)
                                break;
                        }
                        if (y.get() < -height || y.get() + destination.getY() < minY) {
                            Scheduler.getInstance().runTask(() -> {
                                paster.complete(null);
                            });
                        }
                    }
                }, 1, 1);
        tasks.add(task);
        return paster.thenAccept(e -> tasks.remove(task));
    }
}
