import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.service.Service;

public class Paster extends Service {

    List<BukkitTask> tasks = new ArrayList<>();

    @Override
    public void onLoad() throws ServiceLoadException {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        tasks.forEach(f->f.cancel());
        tasks.clear();
        super.onUnload();
    }

    public CompletableFuture<Void> paste(Location source, Location destination, int width, int depth, int height) {
        List<Location> offsets = new ArrayList<>(height * width * depth * 9);
        for (int x = -width; x <= width; x++) {
            for (int y = -height; y <= height; y++) {
                for (int z = -depth; z <= depth; z++) {
                    offsets.add(new Location(null, x, y, z));
                }
            }
        }
        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = ObsidianConfig.getInstance().paster().blockPerTick();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blockPerTick; i++) {
                    if (offsets.size() == 0)
                        break;
                    Block dBlock = destination.add(offsets.get(0)).getBlock();
                    Block sBlock = source.add(offsets.get(0)).getBlock();
                    dBlock.setBlockData(sBlock.getBlockData(), false);
                    if (dBlock.getState() != null)
                    dBlock.getState().update();
                }
                if (offsets.size() == 0) {
                    paster.complete(null);
                }
            }
        }.runTaskTimer(BuildBattle.getInstance(), 1, 1);
        tasks.add(task);
        paster = paster.thenAccept(e -> tasks.remove(task));

        return paster;
    }

    public CompletableFuture<Void> unpaste(Location destination, int width, int depth, int height) {
        List<Location> offsets = new ArrayList<>(height * width * depth * 9);
        for (int x = -width; x <= width; x++) {
            for (int y = -height; y <= height; y++) {
                for (int z = -depth; z <= depth; z++) {
                    offsets.add(new Location(null, x, y, z));
                }
            }
        }
        CompletableFuture<Void> paster = new CompletableFuture<>();
        int blockPerTick = ObsidianConfig.getInstance().paster().blockPerTick();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < blockPerTick; i++) {
                    if (offsets.size() == 0)
                        break;
                    Block b = destination.add(offsets.get(0)).getBlock();
                    b.setType(Material.AIR);
                    if (b.getState() != null)
                        b.getState().update();
                }
                if (offsets.size() == 0) {
                    paster.complete(null);
                }
            }
        }.runTaskTimer(BuildBattle.getInstance(), 1, 1);
        tasks.add(task);
        paster = paster.thenAccept(e -> tasks.remove(task));

        return paster;
    }
}