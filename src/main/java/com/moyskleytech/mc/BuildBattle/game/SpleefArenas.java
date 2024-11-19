package com.moyskleytech.mc.BuildBattle.game;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

public class SpleefArenas extends Service implements Listener {
    private List<SpleefArena> arenas;
    private final List<SpleefRunningArena> runningArenas = new ArrayList<>();
    private final Map<Player, SpleefRunningArena> arenaForPlayer = new HashMap<>();
    private File arenasFolder;

    public List<SpleefArena> getArenas() {
        Logger.trace("Arenas::getArenas()");
        return new ArrayList<>(arenas);
    }

    public List<SpleefRunningArena> getRunningArenas() {
        // Logger.trace("Arenas::getRunningArenas()");
        return new ArrayList<>(runningArenas);
    }

    public void addRunning(SpleefRunningArena arena) {
        Service.get(BaseArenas.class).addRunning(arena);

        Logger.trace("Arenas::addRunning({})", arena);
        runningArenas.add(arena);
    }

    public void removeRunning(SpleefRunningArena arena) {
        Service.get(BaseArenas.class).removeRunning(arena);

        Logger.trace("Arenas::removeRunning({})", arena);
        runningArenas.remove(arena);
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        BuildBattle plugin = BuildBattle.getInstance();
        File folder = plugin.getDataFolder();
        arenasFolder = new File(folder, "spleef_arenas");
        arenasFolder.mkdir();

        Data data = Service.get(Data.class);
        arenas = new ArrayList<>();
        for (File arena : arenasFolder.listFiles()) {
            SpleefArena arena2 = (data.load(SpleefArena.class, arena));
            if (!arena.getName().equals(arena2.getId().toString() + ".yml")) {
                throw new ServiceLoadException("File " + arena + " contains arena " + arena2.getId()
                        + " and should be named " + arena2.getId() + ".yml");
            }
            arenas.add(arena2);
        }

        BuildBattle.getInstance().registerListener(this);
        super.onLoad();
    }

    public ActionResult register(SpleefArena a) {
        Logger.trace("Arenas::register({})", a);
        Data data = Service.get(Data.class);
        if (byId(a.getId()) != null) {
            return ActionResult.failure(ActionResult.ARENA_ALREADY_REGISTERED);
        }
        arenas.add(a);
        File arenaFile = new File(arenasFolder, a.getId().toString() + ".yml");
        data.save(a, arenaFile);
        return ActionResult.success();
    }

    public ActionResult save(SpleefArena a) {
        Logger.trace("Arenas::save({})", a);

        if (!register(a).isSuccess()) {
            Data data = Service.get(Data.class);
            File arenaFile = new File(arenasFolder, a.getId().toString() + ".yml");
            data.save(a, arenaFile);
        }
        return ActionResult.success();
    }

    @Override
    public void onUnload() {
        Logger.trace("Arenas::onUnload()");

        getRunningArenas().forEach(arena -> arena.stop());
        arenaForPlayer.clear();
        super.onUnload();
    }

    public void put(Player p, SpleefRunningArena runningArena) {
        Logger.trace("Arenas::put({},{})", p, runningArena);

        if (runningArena == null)
            arenaForPlayer.remove(p);
        else
            arenaForPlayer.put(p, runningArena);
    }

    public SpleefRunningArena getArenaForPlayer(Player p) {
        // Logger.trace("Arenas::getArenaForPlayer({})",p);

        return arenaForPlayer.get(p);
    }

    public boolean isArena(World w) {
        // Logger.trace("Arenas::isArena({})",w);
        return runningArenas.stream().anyMatch(arena -> arena.world.equals(w));
    }

    public ActionResult joinRandomly(Player player) {
        Logger.trace("Arenas::joinRandomly({})", player);

        Optional<SpleefRunningArena> joinable = runningArenas.stream().filter(ra -> ra.state == SpleefArenaState.LOBBY)
                .findAny();
        if (joinable.isPresent()) {
            joinable.get().join(player);
            return ActionResult.success();
        } else {
            List<SpleefArena> maps = getArenas();
            Collections.shuffle(maps);
            if (maps.size() == 0)
                return ActionResult.failure(ActionResult.MAP_NOT_EXISTING);

            SpleefArena toStart = maps.get(0);
            SpleefRunningArena running = toStart.start();
            running.join(player);
            return ActionResult.success();
        }
    }

    public ActionResult join(Player player, String map) {
        Logger.trace("Arenas::join({},{})", player, map);

        return join(player, map, true);
    }

    public ActionResult join(Player player, String map, boolean allowExisting) {
        Logger.trace("Arenas::join({},{},{})", player, map, allowExisting);

        Optional<SpleefRunningArena> joinable = runningArenas.stream()
                .filter(ra -> ra.state == SpleefArenaState.LOBBY && ra.arena.getName().equals(map)).findAny();
        if (allowExisting && joinable.isPresent()) {
            joinable.get().join(player);
            return ActionResult.success();
        } else {
            SpleefArena toStart = byName(map);
            if (toStart == null)
                return ActionResult.failure(ActionResult.MAP_NOT_EXISTING);

            SpleefRunningArena running = toStart.start();
            running.join(player);
            return ActionResult.success();
        }
    }

    public SpleefArena byName(String map) {
        Logger.trace("Arenas::byName({})", map);

        return arenas.stream().filter(ar -> ar.getName().equalsIgnoreCase(map)).findAny().orElse(null);
    }

    public SpleefArena byId(UUID map) {
        Logger.trace("Arenas::byId({})", map);

        return arenas.stream().filter(ar -> ar.getId().equals(map)).findAny().orElse(null);
    }

    public @NonNull List<String> names() {
        Logger.trace("Arenas::names()");

        return arenas.stream().map(arena -> arena.getName()).toList();
    }
}
