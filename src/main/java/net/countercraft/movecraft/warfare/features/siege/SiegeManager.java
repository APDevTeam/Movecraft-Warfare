package net.countercraft.movecraft.warfare.features.siege;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.warfare.features.siege.tasks.SiegePaymentTask;
import net.countercraft.movecraft.warfare.features.siege.tasks.SiegePreparationTask;
import net.countercraft.movecraft.warfare.features.siege.tasks.SiegeProgressTask;

public class SiegeManager extends BukkitRunnable {
    @NotNull private final Set<Siege> sieges = new HashSet<>();
    @NotNull private final Plugin warfare;

    public SiegeManager(@NotNull Plugin warfare) {
        this.warfare = warfare;
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
