package com.moyskleytech.mc.BuildBattle.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.bukkit.event.Listener;
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
public abstract class BaseRunningArena implements Listener {
    World world;
    BaseArena arena;
    List<Player> players = new ArrayList<>();
    Map<UUID, ItemStack[]> playersInventory = new HashMap<>();

    boolean blockMovement = false;
    boolean preventBuildDestroy = false;
    protected Player winner;
    CompletableFuture<Void> currentAction = null;
    protected boolean stopping;

    public String getName() {
        return arena.getName();
    }

    public BaseRunningArena(BaseArena arena, World world) {
        this.arena = arena;
        this.world = world;

        BuildBattle.getInstance().registerListener(this);
    }

    public void stop() {
        stopping = true;
        BuildBattle.getInstance().unregisterListener(this);

    }

    private CompletableFuture<Void> runLaterOrNow() {
        if (currentAction == null)
            return CompletableFuture.completedFuture(null);
        else
            return currentAction;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public CompletableFuture<Boolean> join(Player p) {
        return CompletableFuture.completedFuture(false);
    }

    public void leave(Player p) {

    }

    public void tick() {

    }

    public void createScoreboard(Player player) {
        final var scoreboardOptional = ScoreboardManager.getInstance()
                .fromCache(player.getUniqueId());
        scoreboardOptional.ifPresent(Scoreboard::destroy);

        final var title = LanguageConfig
                .getInstance()
                .scoreboard().animatedTitle();

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
        return List.of();
    }

    public List<LanguagePlaceholder> processPlaceholders(List<LanguagePlaceholder> source, Player p) {
        return source;
    }

    public abstract boolean belongToPlot(Block b);
    
}
