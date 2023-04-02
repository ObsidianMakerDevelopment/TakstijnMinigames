package com.moyskleytech.mc.BuildBattle.placeholderapi;

import org.bukkit.entity.Player;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.Logger;

public class Placeholders {

    public static String run(Player player, String identifier) {
        try {
            if (player == null)
                return null;
            Logger.trace("Placeholder '" + identifier + "' was requested.");
            String[] identifiers = identifier.split("_");
            if (identifiers.length < 1)
                return null;
            Arenas arenas = Service.get(Arenas.class);
            switch (identifiers[0]) {
                case "version":
                    return BuildBattle.getInstance().getVersion();
                case "arena":
                    if (identifiers.length < 2)
                        return null;
                    RunningArena arena = arenas.getArenaForPlayer(player);
                    if (arena == null)
                        return null;
                    switch (identifiers[1]) {
                        case "theme":
                            return arena.getTheme();
                        case "state":
                            return arena.getState().toString();
                        case "winner":
                            if (arena.getWinner() == null)
                                return "";
                            return arena.getWinner().getDisplayName();
                        case "countdown":
                            return String.valueOf(arena.getCountdown());
                        case "minute":
                            return String.valueOf(arena.minutes());
                        case "second":
                            return String.valueOf(arena.seconds());
                        case "currentplot":
                            return String.valueOf(arena.getCurrent_plot().owner.getDisplayName());
                        case "players":
                            return String.valueOf(arena.getPlayers().size());
                        case "name":
                            return String.valueOf(arena.getName());
                    }
            }
            Logger.trace("Placeholder '" + identifiers + "' was unresolved.");
        } catch (Throwable t) {

        }
        return identifier;
    }

}
