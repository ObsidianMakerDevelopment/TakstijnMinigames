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
        List<Location> offsets = new LinkedList<>();
        for (int y = -height; y <= height; y++) {
            for (int x = -width; x <= width; x++) {
                for (int z = -depth; z <= depth; z++) {
                    offsets.add(new Location(source.getWorld(), x, y, z));
                }
            }
        }
        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = ObsidianConfig.getInstance().paster().blockPerTick();
        boolean tickAware = ObsidianConfig.getInstance().paster().tickAware();
        Task task = Scheduler.getInstance().runTaskTimerAsync(
                new Consumer<Scheduler.Task>() {
                    Iterator<Location> iterator = offsets.iterator();

                    @Override
                    public void accept(Task task) {
                        int blockPerTickAware = blockPerTick;
                        if (tickAware) {
                            blockPerTickAware = (int) Math.ceil((Bukkit.getTPS()[0] / 20.0) * blockPerTick);
                        }
                        AtomicInteger blocks = new AtomicInteger();
                        for (AtomicInteger i = new AtomicInteger(); i.get() < blockPerTickAware; i.incrementAndGet()) {
                            if (!iterator.hasNext())
                                break;
                            Location offset = iterator.next();
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
                        if (!iterator.hasNext()) {
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
        List<Location> offsets = new LinkedList<>();
        for (int y = height; y >= -height; y--) {
            for (int x = -width; x <= width; x++) {
                for (int z = -depth; z <= depth; z++) {
                    offsets.add(new Location(destination.getWorld(), x, y, z));
                }
            }
        }
        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = ObsidianConfig.getInstance().paster().blockPerTick();
        boolean tickAware = ObsidianConfig.getInstance().paster().tickAware();
        Task task = Scheduler.getInstance().runTaskTimerAsync(
                new Consumer<Scheduler.Task>() {
                    Iterator<Location> iterator = offsets.iterator();

                    @Override
                    public void accept(Task task) {
                        int blockPerTickAware = blockPerTick;
                        if (tickAware) {
                            blockPerTickAware = (int) Math.ceil((Bukkit.getTPS()[0] / 20.0) * blockPerTick);
                        }
                        AtomicInteger blocks = new AtomicInteger();
                        for (AtomicInteger i = new AtomicInteger(); i.get() < blockPerTickAware; i.incrementAndGet()) {
                            if (!iterator.hasNext())
                                break;
                            Location offset = iterator.next();
                            Scheduler.getInstance().runChunkTask(offset, 0, () -> {
                                Block dBlock = destination.clone().add(offset.getX(), offset.getY(), offset.getZ())
                                        .getBlock();
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
                        if (!iterator.hasNext()) {
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
