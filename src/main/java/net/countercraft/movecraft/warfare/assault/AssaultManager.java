package net.countercraft.movecraft.warfare.assault;

import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.features.assault.Assault;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Process assaults every 20 ticks
 */
public class AssaultManager extends BukkitRunnable {
    private final List<Assault> assaults = new CopyOnWriteArrayList<>();
    private final MovecraftWarfare warfare;

    public AssaultManager(MovecraftWarfare warfare) {
        this.warfare = warfare;
    }

    @Override
    public void run() {
        for (Assault assault : assaults) {
            new AssaultTask(assault).runTask(warfare);
        }
    }

    public List<Assault> getAssaults() {
        return assaults;
    }

    @Nullable
    public Assault getAssault(String regionName) {
        for(Assault assault : assaults) {
            if(assault.getRegionName().equalsIgnoreCase(regionName))
                return assault;
        }
        return null;
    }
}
