package net.countercraft.movecraft.warfare;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.warfare.bar.AssaultBarManager;
import net.countercraft.movecraft.warfare.bar.SiegeBarManager;
import net.countercraft.movecraft.warfare.bar.config.PlayerManager;
import net.countercraft.movecraft.warfare.commands.*;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.AssaultManager;
import net.countercraft.movecraft.warfare.features.assault.RegionDamagedSign;
import net.countercraft.movecraft.warfare.features.assault.listener.AssaultExplosionListener;
import net.countercraft.movecraft.warfare.features.assault.listener.CraftDetectListener;
import net.countercraft.movecraft.warfare.features.assault.listener.CraftRepairListener;
import net.countercraft.movecraft.warfare.features.siege.SiegeLeaderListener;
import net.countercraft.movecraft.warfare.features.siege.SiegeManager;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumSet;

public final class MovecraftWarfare extends JavaPlugin {
    private static MovecraftWarfare instance;
    private AssaultManager assaultManager;
    private SiegeManager siegeManager;

    public static synchronized MovecraftWarfare getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        File folder = new File(getDataFolder(), "userdata");
        if (!folder.exists()) {
            getLogger().info("Created userdata directory");
            folder.mkdirs();
        }

        String[] languages = { "en" };
        for (String s : languages) {
            if (!new File(getDataFolder() + "/localisation/mcwlang_" + s + ".properties").exists()) {
                saveResource("localisation/mcwlang_" + s + ".properties", false);
            }
        }
        Config.Locale = getConfig().getString("Locale", "en");
        I18nSupport.init();

        Config.AssaultEnable = getConfig().getBoolean("AssaultEnable", false);
        Config.SiegeEnable = getConfig().getBoolean("SiegeEnable", false);

        if (MovecraftWorldGuard.getInstance() == null || MovecraftRepair.getInstance().getEconomy() == null) {
            Config.AssaultEnable = false;
            Config.SiegeEnable = false;
        }
        if (MovecraftRepair.getInstance() == null) {
            Config.AssaultEnable = false;
            Config.SiegeEnable = false;
        }

        var playerManager = new PlayerManager();
        getServer().getPluginManager().registerEvents(playerManager, this);

        if (Config.AssaultEnable) {
            assaultManager = new AssaultManager(this);
            assaultManager.runTaskTimerAsynchronously(this, 0, 20);

            Config.AssaultDamagesCapPercent = getConfig().getDouble("AssaultDamagesCapPercent", 1.0);
            Config.AssaultCooldownHours = getConfig().getInt("AssaultCooldownHours", 24);
            Config.AssaultDelay = getConfig().getInt("AssaultDelay", 1800);
            Config.AssaultDuration = getConfig().getInt("AssaultDuration", 1800);
            Config.AssaultCostPercent = getConfig().getDouble("AssaultCostPercent", 0.25);
            Config.AssaultDamagesPerBlock = getConfig().getInt("AssaultDamagesPerBlock", 15);
            Config.AssaultRequiredDefendersOnline = getConfig().getInt("AssaultRequiredDefendersOnline", 2);
            Config.AssaultRequiredOwnersOnline = getConfig().getInt("AssaultRequiredOwnersOnline", 1);
            Config.AssaultMaxBalance = getConfig().getDouble("AssaultMaxBalance", 5000000);
            Config.AssaultOwnerWeightPercent = getConfig().getDouble("AssaultOwnerWeightPercent", 1.0);
            Config.AssaultMemberWeightPercent = getConfig().getDouble("AssaultMemberWeightPercent", 1.0);
            Config.AssaultChunkSavePerTick = getConfig().getInt("AssaultChunkSavePerTick", 1);
            Config.AssaultChunkSavePeriod = getConfig().getInt("AssaultChunkSavePeriod", 1);
            Config.AssaultChunkRepairPerTick = getConfig().getInt("AssaultChunkRepairPerTick", 1);
            Config.AssaultChunkRepairPeriod = getConfig().getInt("AssaultChunkSavePeriod", 1);
            Config.AssaultDestroyableBlocks = EnumSet.noneOf(Material.class);
            for (String s : getConfig().getStringList("AssaultDestroyableBlocks")) {
                EnumSet<Material> materials = Tags.parseMaterials(s);
                if (materials.isEmpty()) {
                    getLogger().info("Failed to load AssaultDestroyableBlock: '" + s + "'");
                } else {
                    Config.AssaultDestroyableBlocks.addAll(materials);
                }
            }

            // Startup assault bar manager (every second)
            AssaultBarManager assaultBarManager = new AssaultBarManager(playerManager);
            assaultBarManager.runTaskTimerAsynchronously(this, 15, 20);
            getServer().getPluginManager().registerEvents(assaultBarManager, this);

            getServer().getPluginManager().registerEvents(new AssaultExplosionListener(), this);
            getServer().getPluginManager().registerEvents(new CraftDetectListener(), this);
            getServer().getPluginManager().registerEvents(new CraftRepairListener(), this);
            getServer().getPluginManager().registerEvents(new RegionDamagedSign(), this);
        }

        getCommand("assaultinfo").setExecutor(new AssaultInfoCommand());
        getCommand("assault").setExecutor(new AssaultCommand());
        getCommand("assaultbar").setExecutor(new AssaultBarCommand(playerManager));
        getCommand("assaultrepair").setExecutor(new AssaultRepairCommand());

        if (Config.SiegeEnable) {
            Config.SiegeTaskSeconds = getConfig().getInt("SiegeTaskSeconds", 600);
            Config.SiegeNoRetreat = getConfig().getBoolean("SiegeNoRetreat", true);
            getLogger().info("Enabling siege");

            siegeManager = new SiegeManager(this);
            siegeManager.runTaskTimerAsynchronously(this, 0, 20);

            // Startup siege bar manager (every second)
            SiegeBarManager siegeBarManager = new SiegeBarManager(playerManager);
            siegeBarManager.runTaskTimerAsynchronously(this, 15, 20);
            getServer().getPluginManager().registerEvents(siegeBarManager, this);

            getServer().getPluginManager().registerEvents(new SiegeLeaderListener(), this);
        }

        getCommand("siege").setExecutor(new SiegeCommand());
        getCommand("siegebar").setExecutor(new SiegeBarCommand(playerManager));
    }

    public AssaultManager getAssaultManager() {
        return assaultManager;
    }

    public SiegeManager getSiegeManager() {
        return siegeManager;
    }
}
