package com.moyskleytech.mc.BuildBattle.services;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

public class Paster extends Service {

    List<BukkitTask> tasks = new ArrayList<>();

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
        BukkitTask task = new BukkitRunnable() {
            Iterator<Location> iterator = offsets.iterator();

            @Override
            public void run() {
                for (int i = 0; i < blockPerTick; i++) {
                    if (!iterator.hasNext())
                        break;
                    Location offset = iterator.next();
                    Block dBlock = destination.clone().add(offset.getX(), offset.getY(), offset.getZ()).getBlock();
                    Block sBlock = source.clone().add(offset.getX(), offset.getY(), offset.getZ()).getBlock();
                    BlockState state = dBlock.getState();
                    if (sBlock.getType() == Material.AIR)
                        i--;
                    else {
                        state.setBlockData(sBlock.getBlockData());
                        state.update(true, false);
                    }
                }
                if (!iterator.hasNext()) {
                    paster.complete(null);
                }
            }
        }.runTaskTimer(BuildBattle.getInstance(), 1, 1);
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
        BukkitTask task = new BukkitRunnable() {
            Iterator<Location> iterator = offsets.iterator();

            @Override
            public void run() {
                for (int i = 0; i < blockPerTick; i++) {
                    if (!iterator.hasNext())
                        break;
                    Location offset = iterator.next();
                    Block dBlock = destination.clone().add(offset.getX(), offset.getY(), offset.getZ()).getBlock();
                    Logger.error("{}", dBlock.getLocation());
                    BlockState state = dBlock.getState();
                    if (state.getType() != Material.AIR) {
                        state.setType(Material.AIR);
                        state.update(true,false);
                    } else {
                        i--;
                    }
                }
                if (!iterator.hasNext()) {
                    paster.complete(null);
                }
            }
        }.runTaskTimer(BuildBattle.getInstance(), 1, 1);
        tasks.add(task);
        return paster.thenAccept(e -> tasks.remove(task));
    }
}
