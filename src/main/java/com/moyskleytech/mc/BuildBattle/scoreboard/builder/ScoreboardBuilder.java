// 
// Decompiled by Procyon v0.5.36
// 

package com.moyskleytech.mc.BuildBattle.scoreboard.builder;

import java.util.HashMap;
import java.util.Objects;
import com.moyskleytech.mc.BuildBattle.scoreboard.api.UpdateCallback;
import com.moyskleytech.mc.BuildBattle.scoreboard.api.PlaceholderFunction;
import java.util.List;
import org.bukkit.entity.Player;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig.LanguagePlaceholder;
import com.moyskleytech.mc.BuildBattle.scoreboard.Scoreboard;
import java.util.Map;

public class ScoreboardBuilder
{
    private final Map<String, String> placeholders;
    private Scoreboard scoreboard;
    private Player holder;
    private boolean occupyWidth;
    private boolean occupyHeight;
    private long interval;
    private long animationInterval;
    private LanguagePlaceholder title;
    private String objectiveName ="";
    private List<LanguagePlaceholder> lines;
    private List<LanguagePlaceholder> animatedTitle;
    private PlaceholderFunction papiFunction;
    private UpdateCallback updateCallback;
    
    public ScoreboardBuilder updateCallback(final UpdateCallback callback) {
        this.updateCallback = callback;
        return this;
    }
    
    public ScoreboardBuilder placeholder(final String placeholder, final Object value) {
        this.placeholders.put(placeholder, value.toString());
        return this;
    }
    
    public ScoreboardBuilder placeholderHook(final PlaceholderFunction papiFunction) {
        this.papiFunction = papiFunction;
        return this;
    }
    
    public ScoreboardBuilder player(final Player player) {
        this.holder = player;
        return this;
    }
    
    public ScoreboardBuilder lines(final List<LanguagePlaceholder> lines) {
        this.lines = lines;
        return this;
    }
        
    public ScoreboardBuilder title(final LanguagePlaceholder title) {
        this.title = title;
        return this;
    }
    
    public ScoreboardBuilder animatedTitle(final List<LanguagePlaceholder> title) {
        this.animatedTitle = title;
        return this;
    }
    
    public ScoreboardBuilder occupyMaxHeight(final boolean bool) {
        this.occupyHeight = bool;
        return this;
    }
    
    public ScoreboardBuilder occupyMaxWidth(final boolean bool) {
        this.occupyWidth = bool;
        return this;
    }
    
    public ScoreboardBuilder displayObjective(final String objectiveName) {
        this.objectiveName = objectiveName;
        return this;
    }
    
    public ScoreboardBuilder updateInterval(final long interval) {
        this.interval = interval;
        return this;
    }
    
    public ScoreboardBuilder animationInterval(final long interval) {
        this.animationInterval = interval;
        return this;
    }
    
    public Scoreboard build() {
        Objects.requireNonNull(this.holder, "Holder cannot be null");
        (this.scoreboard = new Scoreboard(this.holder)).setAnimationTaskInterval(this.animationInterval);
        this.scoreboard.setOccupyMaxHeight(this.occupyHeight);
        this.scoreboard.setOccupyMaxWidth(this.occupyWidth);
        this.scoreboard.setUpdateTaskInterval(this.interval);
        if (this.title != null) {
            this.scoreboard.setAnimatedTitle(List.of(this.title));
        }
        if (this.lines != null) {
            this.scoreboard.setLines(this.lines);
        }
        if (this.animatedTitle != null) {
            this.scoreboard.setAnimatedTitle(this.animatedTitle);
        }
        if (this.updateCallback != null) {
            this.scoreboard.setCallback(this.updateCallback);
        }
        final Map<String, String> placeholders = this.placeholders;
        final Scoreboard scoreboard = this.scoreboard;
        Objects.requireNonNull(scoreboard);
        scoreboard.setObjective(objectiveName);
        placeholders.forEach(scoreboard::addInternalPlaceholder);
        return this.scoreboard;
    }
    
    public ScoreboardBuilder() {
        this.placeholders = new HashMap<String, String>();
        this.interval = 20L;
        this.animationInterval = 2L;
    }
}
