package com.moyskleytech.mc.obsidiancore.config;

import com.moyskleytech.mc.obsidiancore.service.Service;
import net.md_5.bungee.api.ChatColor;
import com.iridium.iridiumcolorapi.IridiumColorAPI;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
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

    public JavaPlugin plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public ObsidianConfig(JavaPlugin plugin) {
        super();
        this.plugin = plugin;
        
    }
    @Override
    public void onLoad() throws ServiceLoadException {
        try{
            loadDefaults();
            super.onLoad();
        }
        catch(Throwable e){
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
            generator.start()
                    .key("version").defValue(plugin.getDescription().getVersion())
                    .key("locale").defValue("en")
                    .key("name").defValue("&f[<RAINBOW1>ObsidianCore</RAINBOW>&f]")
                    .section("afk")
                    .key("enabled").defValue(true)
                    .key("minute").defValue(20)
                    .back()
                    .section("claim")
                    .key("enabled").defValue(false)
                    .back()
                    .section("tpa")
                    .key("enabled").defValue(true)
                    .back()
                    .section("fishing")
                    .key("enabled").defValue(false)
                    .back()
                    .section("banking")
                    .key("enabled").defValue(false)
                    .back()
                    .section("enchant")
                    .key("enabled").defValue(true)
                    .back()
                    .section("void-teleport")
                    .key("enabled").defValue(true)
                    .key("worlds").defValue(List.of("lobby"))
                    .key("threashold").defValue(-64)
                    .back()
                    .section("homes")
                    .key("enabled").defValue(true)
                    .key("max-homes-msg").defValue("§4You have reached the maximum amount of saved homes!")
                    .key("max-homes").defValue(0)
                    .key("tp-delay").defValue(3)
                    .key("tp-cooldown").defValue(0)
                    .key("tp-cancelOnMove").defValue(false)
                    .key("tp-cancelOnMove-msg").defValue("§4Movement detected! Teleporting has been cancelled!")
                    .key("tp-cooldown-msg").defValue("§4You must wait another %s second(s) before teleporting!")
                    .back()
                    .section("consume")
                    .key("enabled").defValue(false)
                    .key("infinity").defValue(true)
                    .back()
                    .section("network")
                    .key("current").defValue("hub")
                    .key("servers").defValue(List.of("hub", "skyblock", "bedwars", "minigames","survival1","survival2","survival3","creative"))
                    .back()
                    .section("motd")
                    .key("line1").defValue(
                            "<RAINBOW1>&k&l▬▬▬▬▬▬▬&r</RAINBOW> <GRADIENT:FFF8BA>Play.obsidian.fun</GRADIENT:0072FF>&a[1.7-1.18] <RAINBOW1>&k§l▬▬▬▬▬▬▬</RAINBOW>")
                    .key("line2").defValue("&r&a NOW 1.18 &l|&r &bBedwars, Survival, Creative")
                    .back()
                    .section("via")
                    .key("enabled").defValue(false)
                    .key("ver").defValue(754)
                    .key("message")
                    .defValue(
                            "&eYou are using a version of minecraft that does not support the new height limit, you can still play survival but at your own risks!")
                    .back()
                    .key("ip").defValue("Server ip is Play.obsidian.fun!")
                    .section("starterKit")
                    .key("enabled").defValue(true)
                    .key("worlds").defValue(List.of("world", "survival2", "s118"))
                    .section("kit")
                    .key("helmet").defValue("leather_helmet")
                    .key("chestplate").defValue("leather_chestplate")
                    .key("legs").defValue("leather_leggings")
                    .key("boots").defValue("leather_boots")
                    .key("0").defValue("stone_pickaxe")
                    .key("1").defValue("stone_axe")
                    .key("2").defValue("stone_sword")
                    .key("3").defValue("stone_shovel")
                    .key("6").defValue("16 oak_log")
                    .key("7").defValue("16 torch")
                    .key("8").defValue("16 cooked_cod")
                    .back()
                    .back()
                    .section("tab")
                    .key("enabled").defValue(true)
                    .key("skin").defValue(true)
                    .key("chat").defValue(true)
                    .key("motd").defValue(true)
                    .key("tablist").defValue(true)
                    .key("header").defValue(List.of("&eplay.OBSIDIAN.fun","&3--------------"))
                    .key("footer").defValue(List.of("&3--------------","&l<GRADIENT:FF08BA>Type /hub to go to lobby</GRADIENT:0072FF>","&e&lEnjoy your stay"))
                    .back()
                    .section("death")
                    .key("skull").defValue(true)
                    .back()
                    .section("skyblock")
                    .key("enabled").defValue(true)
                    .back()
                    .section("mangrove")
                    .key("enabled").defValue(true)
                    .back()
                    .section("luckperms")
                    .key("enabled").defValue(true)
                    .back()
                    .section("join-leave")
                    .key("enabled").defValue(true)
                    .key("empty").defValue(true)
                    .back()
                    .section("fun")
                    .key("spring").defValue(true)
                    .key("rickroll").defValue(true)
                    .back()
                    .section("essentials")
                    .key("velocity").defValue(true)
                    .key("craft").defValue(true)
                    .key("pardon").defValue(true)
                    .key("tp").defValue(true)
                    .key("kick").defValue(true)
                    .key("ip").defValue(true)
                    .key("ban").defValue(true)
                    .key("gamemode").defValue(true)
                    .key("sudo").defValue(true)
                    .key("offlineTP").defValue(true)
                    .back()
                    

            ;

            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void forceReload() {
        /*
         * loader = YamlConfigurationLoader
         * .builder()
         * .path(dataFolder.toPath().resolve("config.yml"))
         * .nodeStyle(NodeStyle.BLOCK)
         * .build();
         * 
         * try {
         * configurationNode = loader.load();
         * } catch (ConfigurateException e) {
         * e.printStackTrace();
         * }
         */
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
        if(s!=null)
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
        if(world==null)
            return null;
        var w = Bukkit.getWorld(world);
        if(w==null)
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

    public @NotNull StarterKitConfig starterKit() {
        return new StarterKitConfig();
    }

    public class StarterKitConfig {
        public boolean enabled() {
            return getBoolean("starterKit.enabled", true);
        }

        public List<String> worlds() {
            return getStringList("starterKit.worlds");
        }

        public Map<String, String> kit() {
            Map<String, String> kit = new HashMap<>();
            var node = node("starterKit", "kit");
            for (var children : node.childrenMap().entrySet()) {
                kit.put(children.getKey().toString(), children.getValue().getString());
            }
            return kit;
        }

    }

    public @NotNull HomesConfig homes() {
        return new HomesConfig();
    }

    public class HomesConfig {
        public boolean enabled() {
            return getBoolean("homes.enabled", true);
        }

        public int tpDelay() {
            return getInt("homes.tp-delay", 3);
        }

        public int tpCooldown() {
            return getInt("homes.tp-cooldown", 0);
        }

        public boolean tpCancelOnMove() {
            return getBoolean("homes.tp-cancelOnMove", false);
        }

        public int maxHomes() {
            return getInt("homes.max-homes", 0);
        }

        public String getMaxHomeMessage() {
            return getString("homes.max-homes-msg");
        }

        public String getCancelOnMoveMessage() {
            return getString("homes.tp-cancelOnMove-msg");
        }

        public String getCooldownMessage() {
            return getString("homes.tp-cooldown-msg");
        }

        public void setMaxHomes(int num) {
            try {
                node("homes", "max-homes").set(num);
                loader.save(configurationNode);
            } catch (SerializationException e) {
                e.printStackTrace();
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        }
    }

    public @NotNull AFKConfig afk() {
        return new AFKConfig();
    }

    public class AFKConfig {
        public boolean enabled() {
            return getBoolean("afk.enabled", true);
        }

        public double numberMinute() {
            return getDouble("afk.minute", 20);
        }
    }

    public @NotNull TPAConfig tpa() {
        return new TPAConfig();
    }

    public class TPAConfig {
        public boolean enabled() {
            return getBoolean("tpa.enabled", true);
        }
    }

    public @NotNull ClaimConfig claim() {
        return new ClaimConfig();
    }

    public class ClaimConfig {
        public boolean enabled() {
            return getBoolean("claim.enabled", true);
        }
    }

    public @NotNull FishingConfig fishing() {
        return new FishingConfig();
    }

    public class FishingConfig {
        public boolean enabled() {
            return getBoolean("fishing.enabled", true);
        }
    }

    public @NotNull ConsumeConfig consume() {
        return new ConsumeConfig();
    }

    public class ConsumeConfig {
        public boolean enabled() {
            return getBoolean("consume.enabled", true);
        }

        public boolean infinity() {
            return getBoolean("consume.infinity", true);
        }
    }

    public @NotNull SkyblockConfig skyblock() {
        return new SkyblockConfig();
    }

    public class SkyblockConfig {
        public boolean enabled() {
            return getBoolean("skyblock.enabled", true);
        }
    }

    public @NotNull MangroveConfig mangrove() {
        return new MangroveConfig();
    }

    public class MangroveConfig {
        public boolean enabled() {
            return getBoolean("mangrove.enabled", true);
        }
    }

    public @NotNull DeathConfig death() {
        return new DeathConfig();
    }

    public class DeathConfig {
        public boolean skull() {
            return getBoolean("death.skull", true);
        }
    }

    public @NotNull LuckPermsConfig luckperms() {
        return new LuckPermsConfig();
    }

    public class LuckPermsConfig {
        public boolean enabled() {
            return getBoolean("luckperms.enabled", true);
        }
    }

    public @NotNull FunConfig fun() {
        return new FunConfig();
    }

    public class FunConfig {
        public boolean spring() {
            return getBoolean("fun.spring", true);
        }
        public boolean rickroll() {
            return getBoolean("fun.rickroll", true);
        }
    }

    public @NotNull JoinLeaveConfig joinLeave() {
        return new JoinLeaveConfig();
    }

    public class JoinLeaveConfig {
        public boolean enabled() {
            return getBoolean("join-leave.enabled", true);
        }
        public boolean emptyJoinLeave() {
            return getBoolean("join-leave.empty", true);
        }
    }

    public @NotNull MOTDConfig motd() {
        return new MOTDConfig();
    }

    public class MOTDConfig {
        public String line1() {
            return getString("motd.line1");
        }

        public String line2() {
            return getString("motd.line2");
        }
    }

    public @NotNull boolean bankingEnabled() {
        return getBoolean("banking.enabled", false);
    }

    public @NotNull boolean enchantEnabled() {
        return getBoolean("enchant.enabled", false);
    }

    public @NotNull ViaConfig via() {
        return new ViaConfig();
    }

    public class ViaConfig {
        public boolean enabled() {
            return getBoolean("via.enabled", false);
        }

        public int version() {
            return getInt("via.ver", 0);
        }

        public String message() {
            return getString("via.message");
        }
    }

    public @NotNull TabConfig tab() {
        return new TabConfig();
    }

    public class TabConfig {
        public boolean enabled() {
            return getBoolean("tab.enabled", false);
        }

        public boolean skin() {
            return getBoolean("tab.skin", false);
        }

        public boolean chat() {
            return getBoolean("tab.chat", false);
        }

        public boolean motd() {
            return getBoolean("tab.motd", false);
        }
        public boolean tablist() {
            return getBoolean("tab.tablist", false);
        }
        public List<String> header()
        {
            return getStringList("tab.header");
        }
        public List<String> footer()
        {
            return getStringList("tab.footer");
        }
    }
    
    public @NotNull EssentialsConfig essentials() {
        return new EssentialsConfig();
    }

    public class EssentialsConfig {
        public boolean velocity() {
            return getBoolean("essentials.velocity", false);
        }

        public boolean craft() {
            return getBoolean("essentials.craft", false);
        }

        public boolean pardon() {
            return getBoolean("essentials.pardon", false);
        }

        public boolean tp() {
            return getBoolean("essentials.tp", false);
        }

        public boolean kick() {
            return getBoolean("essentials.kick", false);
        }

        public boolean ip() {
            return getBoolean("essentials.ip", false);
        }

        public boolean ban() {
            return getBoolean("essentials.ban", false);
        }

        public boolean gamemode() {
            return getBoolean("essentials.gamemode", false);
        }

        public boolean sudo() {
            return getBoolean("essentials.sudo", false);
        }
        public boolean offlineTP() {
            return getBoolean("essentials.offlineTP", false);
        }

        public boolean spawner() {
            return getBoolean("essentials.spawner", true);
        }
    }

    public @NotNull VoidConfig voidTeleport() {
        return new VoidConfig();
    }

    public class VoidConfig {
        // .section("void-teleport")
        // .key("enabled").defValue(true)
        // .key("worlds").defValue(List.of("lobby"))
        // .back()
        public boolean enabled() {
            return getBoolean("void-teleport.enabled", false);
        }

        public List<String> worlds() {
            return getStringList("void-teleport.worlds");
        }

        public int threashold() {
            return getInt("void-teleport.threashold", -64);
        }
    }

    public @NotNull NetworkConfig network() {
        return new NetworkConfig();
    }

    public class NetworkConfig {
        /*
         * .section("network")
         * .key("current").defValue("hub")
         * .key("servers").defValue(List.of("hub", "skyblock", "bedwars", "minigames"))
         */
        public String current() {
            return getString("network.current");
        }

        public List<String> servers() {
            return getStringList("network.servers");
        }

        public Location getSpawn() {
            return getLocation("network.spawn");
        }

        public void setSpawn(Location l) {
            try {
                if (l == null)
                {
                    node("network").removeChild("spawn");
                }
                else
                {
                    node("network", "spawn", "world").set(l.getWorld().getName());
                    node("network", "spawn", "x").set(l.getX());
                    node("network", "spawn", "y").set(l.getY());
                    node("network", "spawn", "z").set(l.getZ());
                    node("network", "spawn", "yaw").set(l.getYaw());
                    node("network", "spawn", "pitch").set(l.getPitch());
                }

                loader.save(configurationNode);
            } catch (SerializationException e) {
                e.printStackTrace();
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        }

        public void setWarp(String name, Location l) {
            try {
                node("network", "warps", name, "world").set(l.getWorld().getName());
                node("network", "warps", name, "x").set(l.getX());
                node("network", "warps", name, "y").set(l.getY());
                node("network", "warps", name, "z").set(l.getZ());
                node("network", "warps", name, "yaw").set(l.getYaw());
                node("network", "warps", name, "pitch").set(l.getPitch());

                loader.save(configurationNode);
            } catch (SerializationException e) {
                e.printStackTrace();
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        }

        public void delWarp(String name) {
            try {
                var warps = node("network", "warps");
                warps.removeChild(name);

                loader.save(configurationNode);
            } catch (SerializationException e) {
                e.printStackTrace();
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
        }

        public Map<String, Location> warps() {
            var warps = node("network", "warps");
            Map<String, Location> ret = new HashMap<>();
            for (var n : warps.childrenMap().keySet()) {
                ret.put(n.toString(), getLocation("network.warps." + n.toString()));
            }
            return ret;
        }

        public Location warp(String name) {
            return getLocation("network.warps." + name);
        }
    }

    @NotNull
    public String logo() {
        return getString("name");
    }

    public String ip() {
        return getString("ip");
    }

}
