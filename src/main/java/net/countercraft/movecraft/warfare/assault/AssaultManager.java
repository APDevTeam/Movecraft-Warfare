package net.countercraft.movecraft.warfare.assault;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Process assaults every 20 ticks
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

    public List<Assault> getAssaults() {
        return assaults;
    }

    @Nullable
    public Assault getAssault(@NotNull ProtectedRegion region) {
        for(Assault assault : assaults) {
            if(assault.getRegion().equals(region))
                return assault;
        }
        return null;
    }
}
