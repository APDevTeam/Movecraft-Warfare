package net.countercraft.movecraft.warfare.features.siege.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import net.countercraft.movecraft.warfare.features.siege.Siege;

public abstract class SiegeTask extends BukkitRunnable {
    protected final Siege siege;

    protected SiegeTask(Siege siege) {
        this.siege = siege;
    }

    @Override
    public abstract void run();
}
