// 
// Decompiled by Procyon v0.5.36
// 

package com.moyskleytech.mc.BuildBattle.scoreboard;

import org.bukkit.plugin.Plugin;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.scoreboard.api.PlaceholderFunction;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.HashMap;

import com.moyskleytech.mc.BuildBattle.scoreboard.builder.ScoreboardBuilder;
import com.moyskleytech.mc.BuildBattle.scoreboard.data.PlaceholderData;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.board.BoardPlayer;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.board.ConfigBoard;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler;
import com.moyskleytech.mc.BuildBattle.utils.Scheduler.Task;

import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;

import com.moyskleytech.mc.BuildBattle.scoreboard.api.UpdateCallback;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.PlaceholderAPI;

public class Scoreboard {
    private ConfigBoard holder;
    private Task holderTask;
    protected Task animationTask;
    protected Task updateTask;
    private long ANIMATION_TASK_INTERVAL;
    private long UPDATE_TASK_INTERVAL;
    private boolean occupyMaxHeight;
    private boolean occupyMaxWidth;
    private UpdateCallback callback;
    private boolean updateTaskRunning;
    private Player player;
    private List<LanguagePlaceholder> lines;
    private final HashMap<String, Object> persistentPlaceholders;

    public Scoreboard(final Player player) {
        this.ANIMATION_TASK_INTERVAL = 2L;
        this.UPDATE_TASK_INTERVAL = 20L;
        this.occupyMaxHeight = false;
        this.occupyMaxWidth = false;
        this.updateTaskRunning = false;
        persistentPlaceholders = new HashMap<>();
        if (BuildBattle.getPluginInstance() == null) {
            throw new NullPointerException("Plugin instance not set! call ScoreboardManager.install() first");
        }
        this.player = player;
        this.holder = new ConfigBoard(player.getName());
        BoardPlayer.getBoardPlayer(player).attachConfigBoard(this.holder);

        holderTask = Scheduler.getInstance().runTaskTimerAsync((task)->{
            holder.run();
        }, 1, 1);
        this.startUpdateTask();
        ScoreboardManager.getInstance().addToCache(this);
    }

    public static ScoreboardBuilder builder() {
        return new ScoreboardBuilder();
    }

    public void setLines(List<LanguagePlaceholder> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        lines = this.resizeContent(lines);

        this.lines = lines;

        refresh();
    }

    public void setVisibility(final boolean visible) {
        if (visible) {
            BoardPlayer.getBoardPlayer(player).attachConfigBoard(this.holder);
        } else {
            BoardPlayer.getBoardPlayer(player).kill();
        }
        this.refresh();
    }

    public void refresh() {
        if (lines != null) {
            holder.setLines(lines);
        }
    }

    public void setTitle(final LanguagePlaceholder title, final boolean animate) {
        Objects.requireNonNull(title, "Title cannot be null");

        this.holder.setTitle(List.of(title), ANIMATION_TASK_INTERVAL);
    }

    public void setAnimatedTitle(final List<LanguagePlaceholder> animatedTitle) {
        if (animatedTitle == null || animatedTitle.isEmpty()) {
            throw new IllegalArgumentException("Animated title cannot be null or empty");
        }
        if (animatedTitle.size() == 1) {
            this.setTitle(animatedTitle.get(0), false);
            return;
        }

        this.holder.setTitle(animatedTitle, ANIMATION_TASK_INTERVAL);
    }

    private void cancelUpdateTask() {
        if (this.updateTask != null) {
            if (this.updateTaskRunning) {
                this.updateTask.cancel();
            }
            this.updateTask = null;
        }
    }

    // public LanguagePlaceholder makeUnique(LanguagePlaceholder toUnique, final List<LanguagePlaceholder> from) {
    //     if (toUnique == null) {
    //         toUnique = LanguagePlaceholder.of(" ");
    //     }
    //     LanguagePlaceholder toUniqueTmp = toUnique;
    //     boolean contains = from.stream().anyMatch(lp -> lp.string().equals(toUniqueTmp.string()));
    //     ;
    //     while (contains
    //             || (this.occupyMaxWidth && !contains && toUnique.string().length() < 40)) {
    //         toUnique = toUnique.append(" ");
    //         LanguagePlaceholder toUniqueTmp2 = toUnique;
    //         contains = from.stream().anyMatch(lp -> lp.string().equals(toUniqueTmp2.string()));
    //     }
    //     // if (stringBuilder.length() > 40) {
    //     // return stringBuilder.substring(0, 40);
    //     // }
    //     return toUnique;
    // }

    public List<LanguagePlaceholder> resizeContent(final List<LanguagePlaceholder> lines) {
        final ArrayList<LanguagePlaceholder> newList = new ArrayList<LanguagePlaceholder>();
        //lines.forEach(line -> newList.add(this.makeUnique(line, newList)));
        lines.forEach(line -> newList.add(line));
        if (newList.size() > 15) {
            return newList.subList(0, 15);
        }
        if (this.occupyMaxHeight) {
            while (newList.size() < 16) {
                //newList.add(this.makeUnique(LanguagePlaceholder.of(" "), newList));
                newList.add(LanguagePlaceholder.of(" "));
            }
        }
        return newList;
    }

    public void destroy() {
        holderTask.cancel();

        this.cancelTasks();
        ScoreboardManager.getInstance().removeFromCache(this.player.getUniqueId());

        this.player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void setObjective(String objectiveName) {
        holder.setObjective(objectiveName);
    }

    public void addInternalPlaceholder(final String placeholder, final Object value) {
        this.persistentPlaceholders.put(placeholder, value.toString());
    }

    public void setUpdateTaskInterval(final long interval) {
        this.UPDATE_TASK_INTERVAL = interval;
        this.startUpdateTask();
    }

    public void setAnimationTaskInterval(final long interval) {
        this.ANIMATION_TASK_INTERVAL = interval;
    }

    public void setCallback(final UpdateCallback callback) {
        this.callback = callback;
    }

    protected void startUpdateTask() {
        this.cancelUpdateTask();
        this.updateTaskRunning = true;
        this.updateTask =  Scheduler.getInstance().runTaskTimer((task)->{
            if (Scoreboard.this.holder == null) {
                task.cancel();
                return;
            }
            if (Scoreboard.this.callback != null) {
                final boolean cancelled = Scoreboard.this.callback.onCallback(Scoreboard.this);
                if (cancelled) {
                    return;
                }
            }
            refresh();
        }, 0,  this.UPDATE_TASK_INTERVAL);
       
    }

    protected void cancelTasks() {
        this.cancelUpdateTask();
    }

    public ConfigBoard getHolder() {
        return this.holder;
    }

    public void setOccupyMaxHeight(final boolean occupyMaxHeight) {
        this.occupyMaxHeight = occupyMaxHeight;
    }

    public void setOccupyMaxWidth(final boolean occupyMaxWidth) {
        this.occupyMaxWidth = occupyMaxWidth;
    }

    public Player getPlayer() {
        return player;
    }

}
