package com.moyskleytech.mc.BuildBattle.generator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import com.moyskleytech.obsidian.material.ObsidianMaterial;

import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VoidGen extends ChunkGenerator {

    @Override
    public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkx, int chunkz,
            @NotNull BiomeGrid biomegrid) {
        ChunkData chunkData = Bukkit.getServer().createChunkData(world);
        int min = world.getMinHeight();
        int max = world.getMaxHeight();
        Biome biome = Biome.THE_VOID;
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = min; y < max; y += 4)
                    biomegrid.setBiome(x, y, z, biome);
        return chunkData;
    }
}
