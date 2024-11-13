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

public class PillarArenas extends Service implements Listener {
    private List<PillarArena> arenas;
    private final List<PillarRunningArena> runningArenas = new ArrayList<>();
    private final Map<Player, PillarRunningArena> arenaForPlayer = new HashMap<>();
    private File arenasFolder;

    public List<PillarArena> getArenas() {
        Logger.trace("Arenas::getArenas()");
        return new ArrayList<>(arenas);
    }

    public List<PillarRunningArena> getRunningArenas() {
        // Logger.trace("Arenas::getRunningArenas()");
        return new ArrayList<>(runningArenas);
    }

    public void addRunning(PillarRunningArena arena) {
        Logger.trace("Arenas::addRunning({})", arena);
        runningArenas.add(arena);
    }

    public void removeRunning(PillarRunningArena arena) {
        Logger.trace("Arenas::removeRunning({})", arena);
        runningArenas.remove(arena);
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        BuildBattle plugin = BuildBattle.getInstance();
        File folder = plugin.getDataFolder();
        arenasFolder = new File(folder, "Pillar_arenas");
        arenasFolder.mkdir();

        Data data = Service.get(Data.class);
        arenas = new ArrayList<>();
        for (File arena : arenasFolder.listFiles()) {
            PillarArena arena2 = (data.load(PillarArena.class, arena));
            if (!arena.getName().equals(arena2.getId().toString() + ".yml")) {
                throw new ServiceLoadException("File " + arena + " contains arena " + arena2.getId()
                        + " and should be named " + arena2.getId() + ".yml");
            }
            arenas.add(arena2);
        }

        BuildBattle.getInstance().registerListener(this);
        super.onLoad();
    }

    public ActionResult register(PillarArena a) {
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

    public ActionResult save(PillarArena a) {
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

    public void put(Player p, PillarRunningArena runningArena) {
        Logger.trace("Arenas::put({},{})", p, runningArena);

        if (runningArena == null)
            arenaForPlayer.remove(p);
        else
            arenaForPlayer.put(p, runningArena);
    }

    public PillarRunningArena getArenaForPlayer(Player p) {
        // Logger.trace("Arenas::getArenaForPlayer({})",p);

        return arenaForPlayer.get(p);
    }

    public boolean isArena(World w) {
        // Logger.trace("Arenas::isArena({})",w);
        return runningArenas.stream().anyMatch(arena -> arena.world.equals(w));
    }

    public ActionResult joinRandomly(Player player) {
        Logger.trace("Arenas::joinRandomly({})", player);

        Optional<PillarRunningArena> joinable = runningArenas.stream().filter(ra -> ra.state == PillarArenaState.LOBBY)
                .findAny();
        if (joinable.isPresent()) {
            joinable.get().join(player);
            return ActionResult.success();
        } else {
            List<PillarArena> maps = getArenas();
            Collections.shuffle(maps);
            if (maps.size() == 0)
                return ActionResult.failure(ActionResult.MAP_NOT_EXISTING);

            PillarArena toStart = maps.get(0);
            PillarRunningArena running = toStart.start();
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

        Optional<PillarRunningArena> joinable = runningArenas.stream()
                .filter(ra -> ra.state == PillarArenaState.LOBBY && ra.arena.getName().equals(map)).findAny();
        if (allowExisting && joinable.isPresent()) {
            joinable.get().join(player);
            return ActionResult.success();
        } else {
            PillarArena toStart = byName(map);
            if (toStart == null)
                return ActionResult.failure(ActionResult.MAP_NOT_EXISTING);

            PillarRunningArena running = toStart.start();
            running.join(player);
            return ActionResult.success();
        }
    }

    public PillarArena byName(String map) {
        Logger.trace("Arenas::byName({})", map);

        return arenas.stream().filter(ar -> ar.getName().equalsIgnoreCase(map)).findAny().orElse(null);
    }

    public PillarArena byId(UUID map) {
        Logger.trace("Arenas::byId({})", map);

        return arenas.stream().filter(ar -> ar.getId().equals(map)).findAny().orElse(null);
    }

    public @NonNull List<String> names() {
        Logger.trace("Arenas::names()");

        return arenas.stream().map(arena -> arena.getName()).toList();
    }
}
