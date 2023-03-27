// 
// Decompiled by Procyon v0.5.36
// 

package com.moyskleytech.mc.BuildBattle.scoreboard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import java.util.Objects;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.UUID;
import java.util.Map;
import org.bukkit.event.Listener;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.scoreboard.scoreboardr.plugin.Session;
import com.moyskleytech.mc.BuildBattle.service.Service;

public class ScoreboardManager extends Service implements Listener
{
    private final Map<UUID, Scoreboard> cachedBoards;
    private boolean toReset;
    private boolean legacy;
    
    public ScoreboardManager() {
        this.cachedBoards = new HashMap<UUID, Scoreboard>();
        this.toReset = true;
    }
    @Override
    public void onLoad() throws ServiceLoadException {
        JavaPlugin plugin = BuildBattle.getPluginInstance();
        Session.makeSession(plugin);
        
        Bukkit.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
        final String[] bukkitVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        int versionNumber = 0;
        for (int i = 0; i < 2; ++i) {
            versionNumber += Integer.parseInt(bukkitVersion[i]) * ((i == 0) ? 100 : 1);
        }
        this.legacy = (versionNumber < 113);

        super.onLoad();
    }
    @Override
    public void onUnload() {
        onDisable();
        super.onUnload();
    }
    
    
    public boolean isLegacy() {
        return legacy;
    }
    
    public void setResetBoardsOnDisabled(final boolean boardsOnDisabled) {
        toReset = boardsOnDisabled;
    }
    
  
    
    public static ScoreboardManager getInstance() {
        return Service.get(ScoreboardManager.class);
    }
    
    public void onDisable() {
        if (!this.toReset) {
            return;
        }
        List.copyOf((Collection<Scoreboard>)this.cachedBoards.values()).forEach(Scoreboard::destroy);
        this.cachedBoards.clear();
    }
    
    public void addToCache(final Scoreboard board) {
        Objects.requireNonNull(board, "Board cannot be null!");
        this.cachedBoards.put(board.getPlayer().getUniqueId(), board);
    }
    
    public void removeFromCache(final UUID uuid) {
        this.cachedBoards.remove(uuid);
    }
    
    public Optional<Scoreboard> fromCache(final UUID uuid) {
        if (this.cachedBoards.containsKey(uuid)) {
            return Optional.of(this.cachedBoards.get(uuid));
        }
        return Optional.empty();
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        final UUID uuid = e.getPlayer().getUniqueId();
        this.fromCache(uuid).ifPresent(Scoreboard::destroy);
        this.cachedBoards.remove(uuid);
    }
}
