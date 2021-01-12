package net.countercraft.movecraft.warfare.assault;

import net.countercraft.movecraft.warfare.MovecraftWarfare;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Procces assaults every 20 ticks
 */
public class AssaultManager extends BukkitRunnable {
    private final List<Assault> assaults = new CopyOnWriteArrayList<>();
    private final MovecraftWarfare mcw;

    public AssaultManager(MovecraftWarfare mcw) {
        this.mcw = mcw;
    }

    @Override
    public void run() {
        for (Assault assault : assaults) {
            new AssaultTask(assault).runTask(mcw);
        }
    }

    public void addAssault(Assault assault) {
        assault.setStartTime(System.currentTimeMillis());
        assault.getStage().set(AssaultStage.IN_PROGRESS);
        assaults.add(assault);
    }

    public List<Assault> getAssaults() {
        return assaults;
    }
}
