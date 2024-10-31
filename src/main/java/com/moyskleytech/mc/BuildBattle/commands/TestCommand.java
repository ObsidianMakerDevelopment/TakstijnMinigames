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

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
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

    @CommandMethod("bb test pos1")
    @CommandPermission("obsidian.bb.test.paste")
    private void commandPos1(final Player player) {
        pos1.put(player.getUniqueId(), player.getLocation());
    }

    @CommandMethod("bb test pos2")
    @CommandPermission("obsidian.bb.test.paste")
    private void commandPos2(final Player player) {
        pos2.put(player.getUniqueId(), player.getLocation());
    }

    @CommandMethod("bb test paste [width] [depth] [height]")
    @CommandPermission("obsidian.bb.test.paste")
    private void commandPaste(final Player player, @Argument(value = "width", defaultValue = "10") int width,
            @Argument(value = "depth", defaultValue = "10") int depth,
            @Argument(value = "height", defaultValue = "10") int height) {
        Paster paster = Service.get(Paster.class);
        paster.paste(pos1.get(player.getUniqueId()), pos2.get(player.getUniqueId()), width, depth, height, player)
                .thenAccept(Void -> {
                    send(player, ObsidianUtil.component("Completed pasting test"));
                });
    }

    @CommandMethod("bb test unpaste [width] [depth] [height]")
    @CommandPermission("obsidian.bb.test.paste")
    private void commandUnpaste(final Player player, @Argument(value = "width", defaultValue = "10") int width,
            @Argument(value = "depth", defaultValue = "10") int depth,
            @Argument(value = "height", defaultValue = "10") int height) {
        Paster paster = Service.get(Paster.class);
        paster.unpaste(pos2.get(player.getUniqueId()), width, depth, height, player).thenAccept(Void -> {
            send(player, ObsidianUtil.component("Completed unpasting test"));
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
