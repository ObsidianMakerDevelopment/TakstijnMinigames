package com.moyskleytech.mc.BuildBattle.commands;

import com.moyskleytech.mc.BuildBattle.config.LanguageConfig;
import com.moyskleytech.mc.BuildBattle.config.ObsidianConfig;
import com.moyskleytech.mc.BuildBattle.game.Arena;
import com.moyskleytech.mc.BuildBattle.game.Arenas;
import com.moyskleytech.mc.BuildBattle.game.RunningArena;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Paster;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Default;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.PlayerSource;

import net.kyori.adventure.text.Component;

public class TestCommand extends CommandManager.Command {

    static boolean init = false;

    private Map<UUID, Location> pos1 = new HashMap();
    private Map<UUID, Location> pos2 = new HashMap();

    @Override
    public void onLoad() throws ServiceLoadException {
        onPostEnabled();
        super.onLoad();
    }

    public void onPostEnabled() {
        CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @Command("bb test pos1")
    @Permission("obsidian.bb.test.paste")
    private void commandPos1(final PlayerSource player) {
        pos1.put(player.source().getUniqueId(), player.source().getLocation());
    }

    @Command("bb test pos2")
    @Permission("obsidian.bb.test.paste")
    private void commandPos2(final PlayerSource player) {
        pos2.put(player.source().getUniqueId(), player.source().getLocation());
    }

    @Command("bb test paste [width] [depth] [height]")
    @Permission("obsidian.bb.test.paste")
    private void commandPaste(final PlayerSource player, @Argument(value = "width") @Default(value ="10") int width,
            @Argument(value = "depth") @Default(value ="10") int depth,
            @Argument(value = "height") @Default(value ="10") int height) {
        Paster paster = Service.get(Paster.class);
        paster.paste(pos1.get(player.source().getUniqueId()), pos2.get(player.source().getUniqueId()), width, depth, height, player.source())
                .thenAccept(Void -> {
                    send(player.source(), ObsidianUtil.component("Completed pasting test"));
                });
    }

    @Command("bb test unpaste [width] [depth] [height]")
    @Permission("obsidian.bb.test.paste")
    private void commandUnpaste(final PlayerSource player, @Argument(value = "width") @Default(value ="10") int width,
            @Argument(value = "depth") @Default(value ="10") int depth,
            @Argument(value = "height") @Default(value ="10") int height) {
        Paster paster = Service.get(Paster.class);
        paster.unpaste(pos2.get(player.source().getUniqueId()), width, depth, height, player.source()).thenAccept(Void -> {
            send(player.source(), ObsidianUtil.component("Completed unpasting test"));
        });
    }

    private void send(Player player, Component c) {
        try {
            player.sendMessage(c);
        } catch (Throwable npe) {
            try {
                Bukkit.getPlayer(player.getUniqueId()).sendMessage(c);
            } catch (Throwable t) {
                Bukkit.getConsoleSender().sendMessage(c);
            }
        }
    }
}
