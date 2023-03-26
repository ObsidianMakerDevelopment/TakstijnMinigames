package com.moyskleytech.mc.obsidiancore.placeholderapi;

import com.moyskleytech.mc.obsidiancore.ObsidianCore;
import com.moyskleytech.mc.obsidiancore.utils.Logger;
import com.moyskleytech.mc.obsidiancore.utils.ObsidianUtil;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankingExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "obsidian";
    }

    @Override
    public @NotNull String getAuthor() {
        return "boiscljo";
    }

    @Override
    public @NotNull String getVersion() {
        return ObsidianCore.getInstance().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null)
            return null;
        Logger.trace("Placeholder '" + identifier + "' was requested.");
        String[] identifiers = identifier.split("_");
        if (identifiers.length < 1)
            return null;
        if ("nickname".equals(identifiers[0]))
            return ObsidianUtil.getNickname(player);
        if ("skin".equals(identifiers[0]))
            return ObsidianUtil.getSkin(player);
        Logger.trace("Placeholder '" + identifiers + "' was unresolved.");

        return super.onPlaceholderRequest(player, identifier);
    }

    public String process(String process,Player player) {
        return PlaceholderAPI.setPlaceholders(player, process);
    }
    

}
