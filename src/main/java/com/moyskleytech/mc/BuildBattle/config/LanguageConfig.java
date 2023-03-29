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

        public LanguagePlaceholder clone()
        {
            return LanguagePlaceholder.of(message).with(p);
        }

		public LanguagePlaceholder append(String string) {
			return append(ObsidianUtil.component(string));
		}
        public LanguagePlaceholder append(Component string) {
			LanguagePlaceholder newValue = clone();
            newValue.message=newValue.message.append(string);
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
        public static LanguagePlaceholder of(String s)
        {
            return new LanguagePlaceholder(s);
        }
        public static LanguagePlaceholder of(Component s)
        {
            return new LanguagePlaceholder(s);
        }

        public Component component() {
            Component c = ObsidianUtil.component(placeholders(message));
            Logger.trace("Loaded translation for {}={}", message, c);
            return c;
        }
        public String string() {
            Component c = placeholders(message);
            Logger.trace("Loaded translation for {}={}", message, c);
            return LegacyComponentSerializer.legacySection().serialize(c);
        }

        public LanguagePlaceholder with(Player p) {
            this.p = p;
            return this;
        }

        public LanguagePlaceholder replace(String string, String replacement) {
            message=message.replaceText(builder-> builder.match(string).replacement(replacement).build());
            return this;
        }
        public LanguagePlaceholder replace(String string, ComponentLike replacement) {
            message=message.replaceText(builder-> builder.match(string).replacement(replacement).build());
            return this;
        }

        private Component placeholders(Component src) {
            Component process = src;
            var papi = BuildBattle.getInstance().papi();
            process= process.replaceText(builder-> builder.match("%prefix%").replacement(ObsidianConfig.getInstance().prefix()).build());
            if(p!=null)
                process=process.replaceText(builder-> builder.match("%target%").replacement(p.displayName()).build());
            if (papi != null)
            {
                var pattern = Pattern.compile("[%]([^%]+)[%]");
                process = process.replaceText(builder->builder.match(pattern).replacement((mr,b)->{
                    return ObsidianUtil.component(papi.process("%"+mr.group(1)+"%",p));
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
                    .key("version").defValue(plugin.getVersion());

            ErrorMessages.build(section);
            EditorMessages.build(section);
            ScoreboardConfig.build(section);

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
            .back();
        }
        public LanguagePlaceholder nowInEdition(String name)
        {
            return LanguagePlaceholder.of(getString("editor.nowInEdition")).replace("%arena%", name);
        }
        public LanguagePlaceholder saved(String name)
        {
            return LanguagePlaceholder.of(getString("editor.saved")).replace("%arena%", name);
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
        public LanguagePlaceholder nonExistingMap()
        {
            return LanguagePlaceholder.of(getString("error.non_existing_map"));
        }
        public LanguagePlaceholder nonExistingMap(String name)
        {
            return nonExistingMap().replace("%arena%",name);
        }
        public LanguagePlaceholder arenaAlreadyRegistered()
        {
            return LanguagePlaceholder.of(getString("error.arena_already_registered"));
        }
        public LanguagePlaceholder notPlaying()
        {
            return LanguagePlaceholder.of(getString("error.not_playing"));
        }
        public LanguagePlaceholder nothingToSave()
        {
            return LanguagePlaceholder.of(getString("error.nothingToSave"));
        }
    }
    public ScoreboardConfig scoreboard() {
        return new ScoreboardConfig();
    }

    public class ScoreboardConfig {

        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("scoreboard")
            .key("animatedTitle").defValue(List.of("&eBuildBattle","&aBuildBattle"))
            .key("lobbyScoreboard").defValue(List.of("","Arena: %arena%","Players: %player_count%","","&7%bb_version%"))
            .key("startingScoreboard").defValue(List.of("","Arena: %arena%","Theme: %theme%","","&7%bb_version%"))
            .key("buildingScoreboard").defValue(List.of("","Arena: %arena%","Theme: %theme%","Time:%minutes%m%seconds%s","","&7%bb_version%"))
            .key("votingScoreboard").defValue(List.of("","Arena: %arena%","Theme: %theme%","Plot of:%current_plot%","Voting time:%countdown%","","&7%bb_version%"))
            .key("winnerScoreboard").defValue(List.of("","Arena: %arena%","Theme: %theme%","Winner:%winner%","Game ending in:%countdown%s","","&7%bb_version%"))
            .back();
        }
        public List<LanguagePlaceholder> animatedTitle()
        {
            return getStringList("scoreboard.animatedTitle").stream().map(line->LanguagePlaceholder.of(line)).toList();
        }
        public List<LanguagePlaceholder> lobbyScoreboard() {
            return getStringList("scoreboard.lobbyScoreboard").stream().map(line->LanguagePlaceholder.of(line)).toList();
        }
        public List<LanguagePlaceholder> startingScoreboard() {
            return getStringList("scoreboard.startingScoreboard").stream().map(line->LanguagePlaceholder.of(line)).toList();
        }
        public List<LanguagePlaceholder> buildingScoreboard() {
            return getStringList("scoreboard.buildingScoreboard").stream().map(line->LanguagePlaceholder.of(line)).toList();
        }
        public List<LanguagePlaceholder> votingScoreboard() {
            return getStringList("scoreboard.votingScoreboard").stream().map(line->LanguagePlaceholder.of(line)).toList();
        }
        public List<LanguagePlaceholder> winnerScoreboard() {
            return getStringList("scoreboard.winnerScoreboard").stream().map(line->LanguagePlaceholder.of(line)).toList();
        }
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
