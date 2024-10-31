package com.moyskleytech.mc.BuildBattle.config;

import com.moyskleytech.mc.BuildBattle.BuildBattle;
import com.moyskleytech.mc.BuildBattle.config.ConfigGenerator.ConfigSection;
import com.moyskleytech.mc.BuildBattle.service.Service;
import com.moyskleytech.mc.BuildBattle.services.Data;
import com.moyskleytech.mc.BuildBattle.utils.ObsidianUtil;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import com.iridium.iridiumcolorapi.IridiumColorAPI;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.*;

public class ObsidianConfig extends Service {

    public static ObsidianConfig getInstance() {
        return Service.get(ObsidianConfig.class);
    }

    public BuildBattle plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;
    public ObsidianConfig(BuildBattle plugin) {
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
                    .path(dataFolder.toPath().resolve("config.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            generator = new ConfigGenerator(loader, configurationNode);
            ConfigSection config = generator.start()
                    .key("version").defValue(plugin.getVersion())
                    .key("prefix").defValue("&f[<RAINBOW1>BuildBattle</RAINBOW>&f]")
                    .key("themes").defValue(List.of("Car","Boat","Cat","Snowman"))
                    ;
            PasterConfig.build(config);

            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<String> themes()
    {
        return getStringList("themes");
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
        String s = node((Object[]) path.split("\\.")).getString();
        if (s != null)
            return IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&',
                    Objects.requireNonNull(s)));
        return s;
    }

    public Location getLocation(String path) {
        String world = getString(path + ".world");
        double x = getDouble(path + ".x", 0);
        double y = getDouble(path + ".y", 0);
        double z = getDouble(path + ".z", 0);
        float yaw = (float) getDouble(path + ".yaw", 0);
        float pitch = (float) getDouble(path + ".pitch", 0);
        if (world == null)
            return null;
        var w = Bukkit.getWorld(world);
        if (w == null)
            return null;
        Location l = new Location(w, x, y, z, yaw, pitch);
        return l;
    }

    public String getString(String path, String def) {
        final var str = getString(path);
        if (str == null) {
            return def;
        }
        return str;
    }

    public Component prefix() {
        return ObsidianUtil.component(getString("prefix"));
    }

    public PasterConfig paster() {
        return new PasterConfig();
    }

    public class PasterConfig {
        public static ConfigSection build(ConfigSection section) throws SerializationException {
            return section.section("paster")
                    .key("blockPerTick").defValue(30000)
                    .key("tickAware").defValue(true)
                    .back();
        }

        public int blockPerTick() {
            return getInt("paster.blockPerTick", 1).intValue();
        }
        public boolean tickAware() {
            return getBoolean("paster.tickAware", true).booleanValue();
        }
    }

    public VoteItem getVoteItem(int i) {
        return Data.getInstance().getItems().voteItems.get(i);
    }
}
