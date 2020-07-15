package net.countercraft.movecraft.warfare;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.warfare.assault.AssaultManager;
import net.countercraft.movecraft.warfare.commands.AssaultCommand;
import net.countercraft.movecraft.warfare.commands.AssaultInfoCommand;
import net.countercraft.movecraft.warfare.commands.SiegeCommand;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.siege.Siege;
import net.countercraft.movecraft.warfare.siege.SiegeManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class MovecraftWarfare extends JavaPlugin {
    private static MovecraftWarfare instance;
    private AssaultManager assaultManager;
    private SiegeManager siegeManager;

    public static synchronized MovecraftWarfare getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if(instance != null)
            return;

        instance = this;
        Config.AssaultEnable = getConfig().getBoolean("AssaultEnable", false);
        Config.SiegeEnable = getConfig().getBoolean("SiegeEnable", false);

        if(Movecraft.getInstance().getWorldGuardPlugin() == null || Movecraft.getInstance().getEconomy() == null) {
            Config.AssaultEnable = false;
            Config.SiegeEnable = false;
        }
        if(Movecraft.getInstance().getWorldEditPlugin() == null) {
            Config.AssaultEnable = false;
        }

        if(Config.AssaultEnable) {
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
            Config.AssaultDestroyableBlocks = new HashSet<>(getConfig().getIntegerList("AssaultDestroyableBlocks"));

            this.getCommand("assaultinfo").setExecutor(new AssaultInfoCommand());
            this.getCommand("assault").setExecutor(new AssaultCommand());
        }

        if(Config.SiegeEnable) {
            siegeManager = new SiegeManager(this);
            getLogger().info("Enabling siege");
            //load the sieges.yml file
            File siegesFile = new File(MovecraftWarfare.getInstance().getDataFolder().getAbsolutePath() + "/sieges.yml");
            InputStream input;
            try {
                input = new FileInputStream(siegesFile);
            } catch (FileNotFoundException e) {
                input = null;
            }
            if (input != null) {
                Map data = new Yaml().loadAs(input, Map.class);
                Map<String, Map<String, ?>> siegesMap = (Map<String, Map<String, ?>>) data.get("sieges");
                List<Siege> sieges = siegeManager.getSieges();
                for (Map.Entry<String, Map<String, ?>> entry : siegesMap.entrySet()) {
                    Map<String,Object> siegeMap = (Map<String, Object>) entry.getValue();
                    sieges.add(new Siege(
                            entry.getKey(),
                            (String) siegeMap.get("RegionToControl"),
                            (String) siegeMap.get("SiegeRegion"),
                            (Integer) siegeMap.get("ScheduleStart"),
                            (Integer) siegeMap.get("ScheduleEnd"),
                            (Integer) siegeMap.getOrDefault("DelayBeforeStart", 0),
                            (Integer) siegeMap.get("SiegeDuration"),
                            (Integer) siegeMap.getOrDefault("DailyIncome", 0),
                            (Integer) siegeMap.getOrDefault("CostToSiege", 0),
                            (Boolean) siegeMap.getOrDefault("DoubleCostPerOwnedSiegeRegion", true),
                            (List<Integer>) siegeMap.get("DaysOfTheWeek"),
                            (List<String>) siegeMap.getOrDefault("CraftsToWin", Collections.emptyList()),
                            (List<String>) siegeMap.getOrDefault("SiegeCommandsOnStart", Collections.emptyList()),
                            (List<String>) siegeMap.getOrDefault("SiegeCommandsOnWin", Collections.emptyList()),
                            (List<String>) siegeMap.getOrDefault("SiegeCommandsOnLose", Collections.emptyList())));
                }
                getLogger().log(Level.INFO, "Siege configuration loaded.");
            }
            siegeManager.runTaskTimerAsynchronously(this, 0, 20);

            this.getCommand("siege").setExecutor(new SiegeCommand());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public AssaultManager getAssaultManager() {
        return assaultManager;
    }

    public SiegeManager getSiegeManager(){return siegeManager;}
}
