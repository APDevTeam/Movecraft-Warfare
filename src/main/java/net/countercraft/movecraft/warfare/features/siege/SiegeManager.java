package net.countercraft.movecraft.warfare.features.siege;

import net.countercraft.movecraft.warfare.features.siege.tasks.SiegePaymentTask;
import net.countercraft.movecraft.warfare.features.siege.tasks.SiegePreparationTask;
import net.countercraft.movecraft.warfare.features.siege.tasks.SiegeProgressTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class SiegeManager extends BukkitRunnable {
    @NotNull private final Set<Siege> sieges = new HashSet<>();
    @NotNull private final Plugin warfare;

    public SiegeManager(@NotNull Plugin warfare) {
        this.warfare = warfare;

        // load the sieges.yml file
        File siegesFile = new File(warfare.getDataFolder().getAbsolutePath() + "/sieges.yml");
        if(!siegesFile.exists())
        {
            warfare.saveResource("sieges.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(siegesFile);
        if(!config.contains("sieges") || !config.isConfigurationSection("sieges")) {
            warfare.getLogger().severe("Failed to load siege configuration. All sieges should be contained within 'sieges' section.");
            return;
        }

        ConfigurationSection globalSection = config.getConfigurationSection("sieges");

        for(String name : globalSection.getKeys(false)) {
            if(!globalSection.isConfigurationSection(name)) {
                warfare.getLogger().severe("Failed to load siege configuration for '" + name + "': Impropper formatting.");
                continue;
            }
            ConfigurationSection siegeSection = globalSection.getConfigurationSection(name);

            try {
                sieges.add(new Siege(name, new SiegeConfig(siegeSection)));
            }
            catch (IllegalArgumentException e) {
                warfare.getLogger().severe("Failed to load siege configuration for '" + name + "' " + e.getMessage());
            }
            catch (NullPointerException e) {
                warfare.getLogger().severe("Failed to load siege configuration for '" + name + "'");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.HOUR_OF_DAY) == 1 && now.get(Calendar.MINUTE) == 1) {
            for (Siege siege : sieges) {
                new SiegePaymentTask(siege).runTask(warfare);
            }
        }

        for (Siege siege : sieges) {
            switch (siege.getStage().get()) {
                case IN_PROGRESS:
                case SUDDEN_DEATH:
                    new SiegeProgressTask(siege).runTask(warfare);
                    break;
                case PREPARATION:
                    new SiegePreparationTask(siege).runTask(warfare);
                    break;
            }
        }
    }

    public Set<Siege> getSieges() {
        return sieges;
    }
}
