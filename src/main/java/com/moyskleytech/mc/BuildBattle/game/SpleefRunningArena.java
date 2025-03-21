package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.WinnerMessageConfig;
import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;
import com.moyskleytech.mc.BuildBattle.scoreboard.ScoreboardManager;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.mc.BuildBattle.services.Paster;
import com.moyskleytech.mc.BuildBattle.services.WorldPool;
import com.moyskleytech.mc.BuildBattle.ui.VotingUI;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;

import lombok.Getter;
import net.kyori.adventure.text.Component;

@SuppressWarnings("unchecked")
@Getter
public class SpleefRunningArena extends BaseRunningArena implements Listener {
    SpleefArena arena;
    SpleefArenaState state = SpleefArenaState.NONE;
    Plot playerPlot;
    Map<UUID, ItemStack[]> playersInventory = new HashMap<>();
    Map<UUID, Integer> playersRank = new HashMap<>();

    private int countdown = 0;
    private List<LocationDB> spawnLoc;

    public String getName() {
        return arena.getName();
    }

    public SpleefRunningArena(SpleefArena arena, World world) {
        super(arena, world);
        this.arena = arena;
        this.world = world;

        setState(SpleefArenaState.LOBBY);
        spawnLoc = arena.getSpawnOffsets();
        BuildBattle.getInstance().registerListener(this);
    }

    public CompletableFuture<Void> pasteLobby() {
        if (currentAction != null) {
            return currentAction.thenAccept(Void -> {
                currentAction = pasteLobby();
            });
        }
        blockMovement = true;

        CompletableFuture<Void> pasting = Service.get(Paster.class).paste(arena.plotSchematicCenter.toBukkit(),
                world.getSpawnLocation(),
                arena.plotSize + arena.contourSize,
                arena.plotSize + arena.contourSize,
                arena.plotHeight + arena.contourSize);

        pasting = pasting.thenAccept(e -> {
            blockMovement = false;
        });

        return currentAction = pasting;
    }

    public CompletableFuture<Void> unpasteLobby() {
        if (currentAction != null) {
            return currentAction.thenAccept(Void -> {
                currentAction = unpasteLobby();
            });
        }
        blockMovement = true;
        CompletableFuture<Void> pasting = Service.get(Paster.class).unpaste(
                world.getSpawnLocation(),
                arena.lobbySize, arena.lobbySize, arena.lobbyHeight);

        return currentAction = pasting;
    }

    public void stop() {
        super.stop();
        stopping = true;
        BuildBattle.getInstance().unregisterListener(this);
        WinnerMessageConfig cfg = LanguageConfig.getInstance().winnerMessage();
        {
            for (Player p : players) {
                try {
                    var header = processPlaceholders(cfg.header(), p);
                    List<LanguagePlaceholder> center_ = new ArrayList<>();
                    for (int i = 0; i < cfg.numberPlayerShown(); i++) {
                        int ij = i;
                        Optional<Map.Entry<UUID,Integer>> pl = playersRank.entrySet().stream()
                                .filter(x -> x.getValue().intValue() == ij).findAny();
                        pl.ifPresent(id->{
                            Player plr = Bukkit.getPlayer(id.getKey());
                            center_.add(
                                cfg.row().replace("%name%", plr.displayName())
                                        .replace("%position%", String.valueOf(ij + 1))
                                        .replace("%score%", String.valueOf(ij + 1)));
                        });
                    }
                    List<LanguagePlaceholder> center = processPlaceholders(center_, p);
                    var footer = processPlaceholders(cfg.footer(), p);
                    List<LanguagePlaceholder> wholeMsaage = new ArrayList<>();
                    wholeMsaage.addAll(header);
                    wholeMsaage.addAll(center);
                    wholeMsaage.addAll(footer);
                    for (var lp : wholeMsaage)
                        p.sendMessage(lp.component());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                Integer rank = playersRank.get(p.getUniqueId());
                if(rank!=null)
                {
                    List<String> rewardCmds = ObsidianConfig.getInstance().getStringList("spl_reward."+(rank.intValue()+1));
                    for(String cmd: rewardCmds)
                    {
                        if(cmd!=null)
                        {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", p.getName()));
                            BuildBattle.getInstance().getLogger().fine(cmd.replaceAll("%player%", p.getName()));
                        }
                    }
                }
            }
        }
        new ArrayList<>(players).forEach(this::leave);
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        arenas.removeRunning(this);

        Logger.trace("Unpasting arena's lobby");
        unpasteLobby().thenAccept(Void -> {
            currentAction = runLaterOrNow().thenAccept(Void3 -> {
                Plot plot = playerPlot;
                Logger.trace("Unpasting arena's plot");

                CompletableFuture<Void> unpastePlot = Service.get(Paster.class).unpaste(plot.center,
                        arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                        arena.plotHeight + arena.contourSize);

                currentAction = runLaterOrNow().thenAccept(Void4 -> {
                    unpastePlot.thenAccept(Void2 -> {
                        Logger.trace("Unregistering world from used");

                        WorldPool.getInstance().freeWorld(this.world);
                    });
                });
            });
        });
    }

    private CompletableFuture<Void> runLaterOrNow() {
        if (currentAction == null)
            return CompletableFuture.completedFuture(null);
        else
            return currentAction;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public CompletableFuture<Boolean> join(Player p) {
        if(players.contains(p)) return CompletableFuture.completedFuture(false);

        if (state == SpleefArenaState.LOBBY) {
            if (players.size() >= spawnLoc.size()) {
                return CompletableFuture.completedFuture(false);
            }
            players.add(p);
            this.playersInventory.put(p.getUniqueId(), p.getInventory().getContents());
            p.getInventory().clear();
            SpleefArenas arenas = Service.get(SpleefArenas.class);
            arenas.put(p, this);

            // teleport to lobby
            return p.teleportAsync(world.getSpawnLocation()).thenApply(teleport -> {
                Scheduler.getInstance().runEntityTask(p, 0, () -> {
                    p.setRespawnLocation(world.getSpawnLocation(),true);
                    p.setGameMode(GameMode.ADVENTURE);
                    p.setAllowFlight(true);
                    p.setFlying(true);
                });

                try {
                    LanguagePlaceholder lp = LanguageConfig.getInstance().forSpleefGameState(SpleefArenaState.LOBBY,
                            -1);
                    if (lp != null)
                        p.sendMessage(lp.with(p).component());

                    createScoreboard(p);

                } catch (Throwable t) {
                    t.printStackTrace();
                }

                return teleport;
            });
        }
        return CompletableFuture.completedFuture(false);
        // Do not join if the arena isn't in lobby mode
    }

    public void leave(Player p) {
        players.remove(p);
        if (state != SpleefArenaState.LOBBY && state != SpleefArenaState.ENDING) {
            if (players.size() == 1) {
                winner = players.get(0);
                setState(SpleefArenaState.ENDING);
            }
        }
        if (players.isEmpty()) {
            if (!stopping)
                stop();
        }
        SpleefArenas arenas = Service.get(SpleefArenas.class);
        arenas.put(p, null);

        ScoreboardManager.getInstance().fromCache(p.getUniqueId()).ifPresent(scoreboard -> scoreboard.destroy());
        p.getInventory().clear();
        p.getInventory().setContents(this.playersInventory.get(p.getUniqueId()));
        // this.playersInventory.put(p.getUniqueId(), p.getInventory().getContents());
        p.teleport(ObsidianUtil.getSpleefMainLobby());
        p.setRespawnLocation(ObsidianUtil.getSpleefMainLobby(),true);
    }

    private void setState(SpleefArenaState state) {
        if (this.state == state)
            return;
        this.state = state;
        if (state == SpleefArenaState.LOBBY) {
            preventBuildDestroy = true;
            pasteLobby();
            countdown = arena.getLobbyDuration();
        }
        if (state == SpleefArenaState.STARTING) {
            preventBuildDestroy = true;
            blockMovement = true;

            // Paste Arena

            Location center = world.getSpawnLocation();
            playerPlot = new Plot();
            playerPlot.center = center;
            AtomicInteger i = new AtomicInteger();
            // Paste the plots

            List<CompletableFuture<Boolean>> teleports = players.stream().map(
                    player -> {
                        var pos = i.getAndIncrement();
                        if (pos < spawnLoc.size()) {

                            // Teleport players to plots
                            player.setGameMode(GameMode.SURVIVAL);
                            player.setAllowFlight(true);
                            // player.setFlying(true);
                            player.getInventory().setItem(EquipmentSlot.HAND, arena.tool.build());

                            return player.teleportAsync(
                                    world.getSpawnLocation().clone().add(spawnLoc.get(pos).toBukkit().toVector()));
                        } else {
                            leave(player);
                            return CompletableFuture.completedFuture(false);
                        }
                    }).toList();
            currentAction = runLaterOrNow()
                    .thenAccept(
                            Void3 -> currentAction = ObsidianUtil.future(teleports).thenAccept(ignored_teleport -> {
                                currentAction = runLaterOrNow().thenAccept(Void4 -> {
                                    var plot = playerPlot;
                                    Scheduler.getInstance().runTask(() -> {
                                        setState(SpleefArenaState.BATTLE);
                                    });
                                });
                            }));

        }
        if (state == SpleefArenaState.BATTLE) {
            // Start countdown
            preventBuildDestroy = false;
            blockMovement = false;
            countdown = arena.getGameDuration();

            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.SURVIVAL);
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    });
        }

        if (state == SpleefArenaState.ENDING) {
            blockMovement = true;
            players.forEach(
                    player -> {
                        // Teleport players to plots
                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    });
            Paster paster = Service.get(Paster.class);
            // Unpaste all plots
            currentAction = runLaterOrNow().thenAccept(Void3 -> {
                Plot plot = playerPlot;
                var plotPasting = paster.unpaste(plot.center,
                        arena.plotSize + arena.contourSize, arena.plotSize + arena.contourSize,
                        arena.plotHeight + arena.contourSize);

                CompletableFuture<Void> unpasterAll = plotPasting;

                currentAction = unpasterAll.thenAccept(e -> stop());
            });
        }
        LanguagePlaceholder lp = LanguageConfig.getInstance().forSpleefGameState(state, -1);
        if (lp != null) {
            for (Player p : players)
                p.sendMessage(lp.with(p).component());
        }
    }

    public void tick() {
        LanguagePlaceholder lp = LanguageConfig.getInstance().forSpleefGameState(state, countdown);
        if (lp != null) {
            for (Player p : players)
                p.sendMessage(lp.with(p).component());
        }
        if (state == SpleefArenaState.LOBBY) {
            if (players.size() >= arena.getMinimumPlayers()) {
                // Once enough players have joined reduce the countdown
                countdown--;
                if (countdown <= 0) {
                    setState(SpleefArenaState.STARTING);
                }
            }
        }
    }

    private void setWinner(Player p) {
        winner = p;
    }

    public void createScoreboard(Player player) {
        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        final var title = LanguageConfig
                .getInstance()
                .scoreboard().animatedSpleefTitle();

        Scoreboard.builder()
                .player(player)
                .updateInterval(10L)
                .animationInterval(2L)
                .animatedTitle(title)
                .updateCallback(board -> {
                    board.setLines(getScoreboardLines(player, board));
                    return true;
                })
                .build();
    }

    public List<LanguagePlaceholder> getScoreboardLines(Player player, Scoreboard board) {
        List<LanguagePlaceholder> scoreboard_lines = List.of();
        if (state == SpleefArenaState.LOBBY)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().lobbySpleefScoreboard();
        else if (state == SpleefArenaState.STARTING)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().startingSpleefScoreboard();
        else if (state == SpleefArenaState.BATTLE)
            scoreboard_lines = LanguageConfig.getInstance().scoreboard().buildingSpleefScoreboard();

        return processPlaceholders(scoreboard_lines, board.getPlayer());
    }

    public List<LanguagePlaceholder> processPlaceholders(List<LanguagePlaceholder> source, Player p) {
        final var lines = new ArrayList<LanguagePlaceholder>();
        source.stream()
                .filter(Objects::nonNull)
                .forEach(line -> {
                    lines.add(line.with(p)
                            .replace("%bb_version%", BuildBattle.getInstance().getVersion())
                            .replace("%state%", state.toString())
                            .replace("%countdown%", String.valueOf(countdown))
                            .replace("%winner%", winner != null ? winner.displayName() : Component.empty())
                            .replace("%rank%",
                                    playersRank.containsKey(p.getUniqueId())
                                            ? playersRank.get(p.getUniqueId()).toString()
                                            : "?")

                            .replace("%minutes%", String.valueOf(minutes()))
                            .replace("%seconds%", String.valueOf(seconds()))

                            .replace("%player_count%", String.valueOf(players.size()))
                            .replace("%arena%", arena.getName()));
                });
        return lines;
    }

    public int seconds() {
        return countdown % 60;
    }

    public int minutes() {
        return countdown / 60;
    }

    public boolean belongToPlot(@NotNull Block block) {
        Plot plot = playerPlot;

        double dx = Math.abs(block.getX() - plot.center.getX());
        double dy = Math.abs(block.getY() - plot.center.getY());
        double dz = Math.abs(block.getZ() - plot.center.getZ());

        return dx <= arena.plotSize && dz <= arena.plotSize && dy <= arena.plotHeight;
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent death) {
        if (getState() == SpleefArenaState.BATTLE)
            if (players.contains(death.getPlayer())) {
                Player pl = death.getPlayer();
                pl.setGameMode(GameMode.SPECTATOR);
                long countLiving = players.stream().filter(p -> p.getGameMode() == GameMode.SURVIVAL).count();
                playersRank.put(pl.getUniqueId(), Integer.valueOf((int) countLiving));

                if (countLiving == 1) {

                    Player winner = players.stream().filter(p -> p.getGameMode() == GameMode.SURVIVAL).findAny().get();
                    playersRank.put(winner.getUniqueId(), Integer.valueOf(0));

                    setWinner(winner);
                    setState(SpleefArenaState.ENDING);
                }
                if (countLiving == 0) {
                    setWinner(death.getPlayer());
                    setState(SpleefArenaState.ENDING);
                }
            }
    }

    @EventHandler
    void playerMove(PlayerMoveEvent event) {
        if (blockMovement)
            event.setCancelled(true);
    }
}
