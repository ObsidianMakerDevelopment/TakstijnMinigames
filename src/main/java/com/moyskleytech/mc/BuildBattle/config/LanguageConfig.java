package com.moyskleytech.mc.BuildBattle.config;

import com.moyskleytech.mc.BuildBattle.utils.Logger;

import net.md_5.bungee.api.ChatColor;
import com.iridium.iridiumcolorapi.IridiumColorAPI;

import org.apache.maven.model.Build;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ConfigGenerator.ConfigSection;
import com.moyskleytech.mc.BuildBattle.game.Plot;
import com.moyskleytech.mc.BuildBattle.placeholderapi.Placeholders;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LanguageConfig extends Service {

    public static class LanguagePlaceholder {
        private Player p;
        private Component message;

        public LanguagePlaceholder clone() {
            return LanguagePlaceholder.of(message).with(p);
        }

        public LanguagePlaceholder append(String string) {
            return append(ObsidianUtil.component(string));
        }

        public LanguagePlaceholder append(Component string) {
            LanguagePlaceholder newValue = clone();
            newValue.message = newValue.message.append(string);
            return newValue;
        }

        public LanguagePlaceholder(Player p2) {
            this.p = p2;
        }

        public LanguagePlaceholder(String p2) {
            this.message = ObsidianUtil.component(p2);
        }

        public LanguagePlaceholder(Component p2) {
            this.message = p2;
        }

        public static LanguagePlaceholder of(String s) {
            return new LanguagePlaceholder(s);
        }

        public static LanguagePlaceholder of(Component s) {
            return new LanguagePlaceholder(s);
        }

        public Component component() {
            Component c = ObsidianUtil.component(placeholders(message));
            return c;
        }

        public String string() {
            Component c = placeholders(message);
            return LegacyComponentSerializer.legacySection().serialize(c);
        }

        public LanguagePlaceholder with(Player p) {
            this.p = p;
            return this;
        }

        public LanguagePlaceholder replace(String string, String replacement) {
            if (replacement == null)
                replacement = "";
            String replacementString = replacement;
            message = message.replaceText(builder -> builder.match(string).replacement(replacementString).build());
            return this;
        }

        public LanguagePlaceholder replace(String string, ComponentLike replacement) {
            message = message.replaceText(builder -> builder.match(string).replacement(replacement).build());
            return this;
        }

        private Component placeholders(Component src) {
            Component process = src;
            var papi = BuildBattle.getInstance().papi();
            process = process.replaceText(
                    builder -> builder.match("%prefix%").replacement(ObsidianConfig.getInstance().prefix()).build());
            if (p != null) {
                process = process
                        .replaceText(builder -> builder.match("%target%").replacement(p.displayName()).build());
            }
            if (papi != null) {
                var pattern = Pattern.compile("[%]([^%]+)[%]");
                process = process.replaceText(builder -> builder.match(pattern).replacement((mr, b) -> {
                    return ObsidianUtil.component(papi.process("%" + mr.group(1) + "%", p));
                }));
            }
            {
                var pattern = Pattern.compile("[%]([^%]+)[%]");
                process = process.replaceText(builder -> builder.match(pattern).replacement((mr, b) -> {
                    String result = null;
                    result = Placeholders.run(p, "arena_" + mr.group(1));
                    if (mr.group(1).equals(result))
                        result = Placeholders.run(p, mr.group(1));
                    return ObsidianUtil.component(result);
                }));
            }
            return process;
        }

    }

    public static LanguageConfig getInstance() {
        return Service.get(LanguageConfig.class);
    }

    public LanguagePlaceholder with(Player p) {
        return new LanguagePlaceholder(p);
    }

    public BuildBattle plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public LanguageConfig(BuildBattle plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public void onLoad() throws ServiceLoadException {
        try {
            loadDefaults();
            super.onLoad();
        } catch (Throwable e) {
            throw new ServiceLoadException(e);
        }

    }

    public ConfigurationNode node(Object... keys) {
        return configurationNode.node(keys);
    }

    public void loadDefaults() {
        this.dataFolder = plugin.getDataFolder();

        try {

            loader = YamlConfigurationLoader
                    .builder()
                    .path(dataFolder.toPath().resolve("language.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            generator = new ConfigGenerator(loader, configurationNode);
            ConfigSection section = generator.start()
                    .key("version").defValue(plugin.getVersion())
                    .key("voted").defValue("%prefix%You voted for the current plot");

            ErrorMessages.build(section);
            EditorMessages.build(section);
            ScoreboardConfig.build(section);
            UiConfig.build(section);

            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ErrorMessages error() {
        return new ErrorMessages();
    }

    public EditorMessages editor() {
        return new EditorMessages();
    }

    public class EditorMessages {

        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("editor")
                    .key("nowInEdition").defValue("%prefix% %arena% is now in edition mode")
                    .key("saved").defValue("%prefix% %arena% has been saved and is ready to be played")
                    .key("teleportedLobby").defValue("%prefix% You have been teleported to %arena%'s lobby")
                    .key("teleportedSchematic").defValue("%prefix% You have been teleported to %arena%'s schematic")
                    .key("arenaHasNoLobby").defValue("%prefix% %arena%'s lobby is not set")
                    .key("arenaHasNoSchematic").defValue("%prefix% %arena%'s schematic is not set")
                    .key("renamed").defValue("%prefix% %previous% has been renamed to %arena%")
                    .key("changed").defValue("%prefix% %arena%'s %value% has been changed")
                    .back();
        }

        public LanguagePlaceholder nowInEdition(String name) {
            return LanguagePlaceholder.of(getString("editor.nowInEdition")).replace("%arena%", name);
        }

        public LanguagePlaceholder saved(String name) {
            return LanguagePlaceholder.of(getString("editor.saved")).replace("%arena%", name);
        }

        public LanguagePlaceholder teleportedLobby(String name) {
            return LanguagePlaceholder.of(getString("editor.teleportedLobby")).replace("%arena%", name);
        }

        public LanguagePlaceholder teleportedSchematic(String name) {
            return LanguagePlaceholder.of(getString("editor.teleportedSchematic")).replace("%arena%", name);
        }

        public LanguagePlaceholder arenaHasNoLobby(String name) {
            return LanguagePlaceholder.of(getString("editor.arenaHasNoLobby")).replace("%arena%", name);
        }

        public LanguagePlaceholder arenaHasNoSchematic(String name) {
            return LanguagePlaceholder.of(getString("editor.arenaHasNoSchematic")).replace("%arena%", name);
        }

        public LanguagePlaceholder renamed(String oldName, String newName) {
            return LanguagePlaceholder.of(getString("editor.renamed")).replace("%previous%", newName).replace("%arena%",
                    newName);

        }

        public LanguagePlaceholder changed(String name, String string) {
            return LanguagePlaceholder.of(getString("editor.changed")).replace("%value%", string).replace("%arena%",
                    name);
        }
    }

    public UiConfig ui() {
        return new UiConfig();
    }

    public class UiConfig {
        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("ui")
                    .key("voting_title").defValue("%prefix%Voting for map")
                    .key("vote_item_name").defValue("%theme%")
                    .key("vote_lore").defValue(List.of(
                            "Vote for theme &b%theme%",
                            "",
                            "Time remaining %countdown%",
                            "Current votes: %votes%",
                            "",
                            "&eClick to vote &b%theme%!"))
                    .back();
        }

        public LanguagePlaceholder votingTitle() {
            return LanguagePlaceholder.of(getString("ui.voting_title"));
        }

        public LanguagePlaceholder votingItemName() {
            return LanguagePlaceholder.of(getString("ui.vote_item_name"));
        }

        public List<LanguagePlaceholder> votingLore() {
            return getStringList("ui.vote_lore").stream().map(line -> LanguagePlaceholder.of(line)).toList();
        }
    }

    public class ErrorMessages {

        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("error")
                    .key("non_existing_map").defValue("%prefix%§cThere is no map for the specified name")
                    .key("arena_already_registered").defValue("%prefix%§cThe arena is already registered")
                    .key("not_playing").defValue("%prefix%§cYou are not currently in a game")
                    .key("nothingToSave").defValue("%prefix%§cThere is nothing to save")
                    .back();
        }

        public LanguagePlaceholder nonExistingMap() {
            return LanguagePlaceholder.of(getString("error.non_existing_map"));
        }

        public LanguagePlaceholder nonExistingMap(String name) {
            return nonExistingMap().replace("%arena%", name);
        }

        public LanguagePlaceholder arenaAlreadyRegistered() {
            return LanguagePlaceholder.of(getString("error.arena_already_registered"));
        }

        public LanguagePlaceholder notPlaying() {
            return LanguagePlaceholder.of(getString("error.not_playing"));
        }

        public LanguagePlaceholder nothingToSave() {
            return LanguagePlaceholder.of(getString("error.nothingToSave"));
        }
    }

    public ScoreboardConfig scoreboard() {
        return new ScoreboardConfig();
    }

    public class ScoreboardConfig {

        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("scoreboard")
                    .key("animatedTitle").defValue(List.of("&eBuildBattle", "&aBuildBattle"))
                    .key("lobbyScoreboard")
                    .defValue(List.of(
                            "&r",
                            "&rArena: %arena%",
                            "&rPlayers: %player_count%",
                            "&rStart in: %minutes%m%seconds%s",
                            "&r",
                            "&7BBv%bb_version%"))
                    .key("startingScoreboard")
                    .defValue(List.of("&r",
                            "&rArena: %arena%",
                            "&rTheme: %theme%",
                            "&rGame initializing",
                            "&r",
                            "&7BBv%bb_version%"))
                    .key("buildingScoreboard")
                    .defValue(List.of(
                            "&r",
                            "&rArena: %arena%",
                            "&rTheme: %theme%",
                            "&rTime: %minutes%m%seconds%s",
                            "&r",
                            "&7BBv%bb_version%"))
                    .key("votingScoreboard")
                    .defValue(List.of(
                            "&r",
                            "&rArena: %arena%",
                            "&rTheme: %theme%",
                            "&rPlot of:%current_plot%",
                            "&rVoting time:%countdown%",
                            "&r",
                            "&7BBv%bb_version%"))
                    .key("winnerScoreboard")
                    .defValue(List.of(
                        "&r",
                        "&rArena: %arena%",
                        "&rTheme: %theme%",
                        "&rWinner:%winner%",
                        "&rGame ending in:%countdown%s",
                        "&r",
                        "&7BBv%bb_version%"
                    ))
                    .back();
        }

        public List<LanguagePlaceholder> animatedTitle() {
            return getStringList("scoreboard.animatedTitle").stream().map(line -> LanguagePlaceholder.of(line))
                    .toList();
        }

        public List<LanguagePlaceholder> lobbyScoreboard() {
            return getStringList("scoreboard.lobbyScoreboard").stream().map(line -> LanguagePlaceholder.of(line))
                    .toList();
        }

        public List<LanguagePlaceholder> startingScoreboard() {
            return getStringList("scoreboard.startingScoreboard").stream().map(line -> LanguagePlaceholder.of(line))
                    .toList();
        }

        public List<LanguagePlaceholder> buildingScoreboard() {
            return getStringList("scoreboard.buildingScoreboard").stream().map(line -> LanguagePlaceholder.of(line))
                    .toList();
        }

        public List<LanguagePlaceholder> votingScoreboard() {
            return getStringList("scoreboard.votingScoreboard").stream().map(line -> LanguagePlaceholder.of(line))
                    .toList();
        }

        public List<LanguagePlaceholder> winnerScoreboard() {
            return getStringList("scoreboard.winnerScoreboard").stream().map(line -> LanguagePlaceholder.of(line))
                    .toList();
        }
    }

    public LanguagePlaceholder voted() {
        return LanguagePlaceholder.of(getString("voted"));
    }
    public void forceReload() {
        loadDefaults();
    }

    public double getDouble(String path, double def) {
        return node((Object[]) path.split("\\.")).getDouble(def);
    }

    public void saveConfig() {
        try {
            this.loader.save(this.configurationNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    public List<String> getStringList(String string) {
        final var list = new ArrayList<String>();
        try {
            for (String s : Objects.requireNonNull(node((Object[]) string.split("\\.")).getList(String.class))) {
                s = ChatColor.translateAlternateColorCodes('&', s);
                s = IridiumColorAPI.process(s);
                list.add(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Integer getInt(String path, Integer def) {
        return node((Object[]) path.split("\\.")).getInt(def);
    }

    public Byte getByte(String path, Byte def) {
        final var val = node((Object[]) path.split("\\.")).getInt(def);
        if (val > 127 || val < -128)
            return def;
        return (byte) val;
    }

    public Boolean getBoolean(String path, boolean def) {
        return node((Object[]) path.split("\\.")).getBoolean(def);
    }

    @SuppressWarnings("all")
    public String getString(@Nonnull String path) {
        return ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(node((Object[]) path.split("\\.")).getString()));
    }

    public String getString(String path, String def) {
        final var str = getString(path);
        if (str == null) {
            return def;
        }
        return str;
    }

    

}
