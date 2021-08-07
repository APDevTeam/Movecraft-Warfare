package net.countercraft.movecraft.warfare.siege;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SiegeManager extends BukkitRunnable {
    private final List<Siege> sieges = new CopyOnWriteArrayList<>();
    private final Plugin mcw;


    public SiegeManager(Plugin mcw) {
        this.mcw = mcw;
    }

    @Override
    public void run() {
        for (Siege siege : sieges) {
            new SiegePaymentTask(siege).runTask(mcw);
            if (siege.getStage().get() == Siege.Stage.IN_PROGRESS) {
                new SiegeProgressTask(siege).runTask(mcw);
            } else if (siege.getStage().get() == Siege.Stage.PREPERATION) {
                new SiegePreparationTask(siege).runTask(mcw);
            }
        }
    }

    public List<Siege> getSieges() {
        return sieges;
    }
}
