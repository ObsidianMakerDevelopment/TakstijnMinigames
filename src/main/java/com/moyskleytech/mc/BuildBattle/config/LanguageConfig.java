package com.moyskleytech.mc.BuildBattle.config;

import com.moyskleytech.mc.BuildBattle.utils.Logger;

import net.md_5.bungee.api.ChatColor;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
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
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LanguageConfig extends Service {

    public static class LanguagePlaceholder {
        private Player p;
        private Component message;

        public LanguagePlaceholder(Player p2) {
            this.p = p2;
        }

        public LanguagePlaceholder(String p2) {
            this.message = ObsidianUtil.component(p2);
        }
        public static LanguagePlaceholder of(String s)
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

        private Component placeholders(Component src) {
            Component process = src;
            var papi = BuildBattle.getInstance().papi();
            process= process.replaceText(builder-> builder.match("%prefix").replacement(ObsidianConfig.getInstance().prefix()).build());
            if(p!=null)
                process=process.replaceText(builder-> builder.match("%prefix").replacement(p.displayName()).build());
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

    public JavaPlugin plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public LanguageConfig(JavaPlugin plugin) {
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
                    .key("version").defValue(plugin.getDescription().getVersion())
                    .section("transaction")
                    .key("deposit").defValue("%logo% &aDeposited %amount% %ore%")
                    .key("withdraw").defValue("%logo% &bWithdrew %amount% %ore%")
                    .back()
                    .key("above-zero").defValue("%logo% &cAmount must be above 0")
                    .key("missing-ore").defValue("%logo% &cYou do not have the required %amount% %ore%")
                    .key("invalid-ore").defValue("%logo% &c%ore% is not a valid bank ore")
                    .key("missing-space")
                    .defValue("%logo% &cYou do not have enough place in inventory for %amount% %ore%");

            ErrorMessages.build(section);

            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ErrorMessages error() {
        return new ErrorMessages();
    }

    public class ErrorMessages {

        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("error")
            .key("non_existing_map").defValue("There is no map for the specified name")
            .key("arena_already_registered").defValue("The arena is already registered")
            .key("not_playing").defValue("You are not currently in a game")
            .back();
        }
        public LanguagePlaceholder nonExistingMap()
        {
            return LanguagePlaceholder.of(getString("error.non_existing_map"));
        }
        public LanguagePlaceholder arenaAlreadyRegistered()
        {
            return LanguagePlaceholder.of(getString("error.arena_already_registered"));
        }
        public LanguagePlaceholder notPlaying()
        {
            return LanguagePlaceholder.of(getString("error.not_playing"));
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
            .back();
        }
        public List<String> animatedTitle()
        {
            return getStringList("scoreboard.animatedTitle");
        }
        public List<String> lobbyScoreboard() {
            return getStringList("scoreboard.lobbyScoreboard");
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

    public String getString(String path) {
        return IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(node((Object[]) path.split("\\.")).getString())));
    }

    public String getString(String path, String def) {
        final var str = getString(path);
        if (str == null) {
            return def;
        }
        return str;
    }

  
}
