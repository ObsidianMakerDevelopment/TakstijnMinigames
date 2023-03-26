package com.moyskleytech.mc.BuildBattle.placeholderapi;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.utils.Logger;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;


public class BuildBattleExpansion extends PlaceholderExpansion {
    @Override
    public  String getIdentifier() {
        return "obsidian";
    }

    @Override
    public  String getAuthor() {
        return "boiscljo";
    }

    @Override
    public  String getVersion() {
        return BuildBattle.getInstance().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player,  String identifier) {
        if (player == null)
            return null;
        Logger.trace("Placeholder '" + identifier + "' was requested.");
        String[] identifiers = identifier.split("_");
        if (identifiers.length < 1)
            return null;
        Logger.trace("Placeholder '" + identifiers + "' was unresolved.");

        return super.onPlaceholderRequest(player, identifier);
    }

    public String process(String process,Player player) {
        return PlaceholderAPI.setPlaceholders(player, process);
    }
    

}
