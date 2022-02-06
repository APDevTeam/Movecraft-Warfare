package net.countercraft.movecraft.warfare.features.siege;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import net.countercraft.movecraft.warfare.features.siege.tasks.SiegePaymentTask;
import net.countercraft.movecraft.warfare.features.siege.tasks.SiegePreparationTask;
import net.countercraft.movecraft.warfare.features.siege.tasks.SiegeProgressTask;

public class SiegeManager extends BukkitRunnable {
    @NotNull private final Set<Siege> sieges = new HashSet<>();
    @NotNull private final Plugin warfare;

    public SiegeManager(@NotNull Plugin warfare) {
        this.warfare = warfare;

        // load the sieges.yml file
        File siegesFile = new File(warfare.getDataFolder().getAbsolutePath() + "/sieges.yml");
        InputStream input;
        try {
            input = new FileInputStream(siegesFile);
        }
        catch (FileNotFoundException e) {
            input = null;
        }

        if (input == null) {
            warfare.getLogger().severe("Failed to load siege configuration.  Please check the sieges.yml file.");
        }
        else {
            try {
                Map data = new Yaml().loadAs(input, Map.class);
                Map<String, Map<String, ?>> siegesMap = (Map<String, Map<String, ?>>) data.get("sieges");
                for (Map.Entry<String, Map<String, ?>> entry : siegesMap.entrySet()) {
                    sieges.add(new Siege(SiegeConfig.load(entry)));
                }
                warfare.getLogger().info("Siege configuration loaded.");
            }
            catch (ClassCastException e) {
                warfare.getLogger().severe("Failed to load siege configuration.  Please check the sieges.yml file.");
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
            if (siege.getStage().get() == Siege.Stage.IN_PROGRESS) {
                new SiegeProgressTask(siege).runTask(warfare);
            }
            else if (siege.getStage().get() == Siege.Stage.PREPERATION) {
                new SiegePreparationTask(siege).runTask(warfare);
            }
            else {
                // Siege is inactive, do nothing
            }
        }
    }

    public Set<Siege> getSieges() {
        return sieges;
    }
}
