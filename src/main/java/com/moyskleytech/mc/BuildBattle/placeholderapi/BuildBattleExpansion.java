package com.moyskleytech.mc.BuildBattle.placeholderapi;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class BuildBattleExpansion extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "obsidianbb";
    }

    @Override
    public String getAuthor() {
        return "boiscljo";
    }

    @Override
    public String getVersion() {
        return BuildBattle.getInstance().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        Placeholders.run(player,identifier);
        return super.onPlaceholderRequest(player, identifier);
    }

    public String process(String process, Player player) {
        try{
            return process_(process, player);
        }
        catch(Throwable t)
        {

        }
        return process;
    }

    public String process_(String process, Player player) {
        return PlaceholderAPI.setPlaceholders(player, process);
    }

}
