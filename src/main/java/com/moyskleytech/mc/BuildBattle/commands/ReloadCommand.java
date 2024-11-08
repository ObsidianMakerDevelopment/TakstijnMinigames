package com.moyskleytech.mc.BuildBattle.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.moyskleytech.mc.BuildBattle.BuildBattle;

import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class ReloadCommand extends CommandManager.Command {

    static boolean init = false;

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        super.onLoad();
    }
    public void onPostEnabled() {
        if (init)
            return;
     
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @Command("bb reload")
    @Permission("obsidian.bb.admin")
    private void commandReload(final @NotNull Player player) {
        BuildBattle.getInstance().reload();
    }

}
