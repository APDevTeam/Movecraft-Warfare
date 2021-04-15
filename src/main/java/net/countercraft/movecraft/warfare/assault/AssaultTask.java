package net.countercraft.movecraft.warfare.assault;

import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.events.AssaultLoseEvent;
import net.countercraft.movecraft.warfare.events.AssaultWinEvent;
import net.countercraft.movecraft.warfare.utils.WarfareRepair;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class AssaultTask extends BukkitRunnable {
    private final Assault assault;

    public AssaultTask(Assault assault) {
        this.assault = assault;
    }


    @Override
    public void run() {
        if (!assault.getRunning().get())
            //in-case the server is lagging and a new assault task is started at the exact time on ends
            return;

        if (assault.getDamages() >= assault.getMaxDamages())
            assaultWon();
        else if (System.currentTimeMillis() - assault.getStartTime() > Config.AssaultDuration * 1000L)
            assaultLost();
    }

    private void assaultWon() {
        // assault was successful
        assault.getRunning().set(false);
        Bukkit.getServer().broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Assault Successful"), assault.getRegionName()));
        Bukkit.getPluginManager().callEvent(new AssaultWinEvent(assault));
        MovecraftWorldGuard.getInstance().getWGUtils().setTNTDeny(assault.getRegionName(), assault.getWorld());

        if(!assault.makeBeacon())
            Bukkit.getServer().broadcastMessage(ERROR_PREFIX + String.format(I18nSupport.getInternationalisedString("Assault - Beacon Placement Failed"), this));

        MovecraftWorldGuard.getInstance().getWGUtils().clearOwners(assault.getRegionName(), assault.getWorld());
        MovecraftWarfare.getInstance().getAssaultManager().getAssaults().remove(assault);
    }

    private void assaultLost() {
        // assault has failed to reach damage cap within required time
        assault.getRunning().set(false);
        Bukkit.getServer().broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Assault Failed"), assault.getRegionName()));
        Bukkit.getPluginManager().callEvent(new AssaultLoseEvent(assault));
        MovecraftWorldGuard.getInstance().getWGUtils().setTNTDeny(assault.getRegionName(), assault.getWorld());

        // repair the damages that have occurred so far
        if (!WarfareRepair.getInstance().repairRegionRepairState(assault.getWorld(), assault.getRegionName(), null))
            Bukkit.getServer().broadcastMessage(ERROR_PREFIX+String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), assault.getRegionName().toUpperCase()));

        MovecraftWarfare.getInstance().getAssaultManager().getAssaults().remove(assault);
    }
}