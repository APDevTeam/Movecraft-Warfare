package net.countercraft.movecraft.warfare.siege;

import org.bukkit.scheduler.BukkitRunnable;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;

public abstract class SiegeTask extends BukkitRunnable {
    protected final Siege siege;

    protected SiegeTask(Siege siege) {
        this.siege = siege;
    }

    @Override
    public abstract void run();
}
