package net.countercraft.movecraft.warfare.features.assault.tasks;

import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.Assault;
import net.countercraft.movecraft.warfare.features.assault.AssaultUtils;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultBroadcastEvent;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultLoseEvent;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultWinEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

import java.time.Duration;
import java.time.LocalDateTime;

public class AssaultTask extends BukkitRunnable {
    private final Assault assault;

    public AssaultTask(Assault assault) {
        this.assault = assault;
    }

    @Override
    public void run() {
        if (assault.getStage().get() != Assault.Stage.IN_PROGRESS) {
            // in-case the server is lagging and a new assault task is started at the exact
            // time one ends
            return;
        }

        if (assault.getDamages() >= assault.getMaxDamages()) {
            assaultWon();
        } else if (Duration.between(assault.getStartTime(), LocalDateTime.now())
                .toMillis() > (Config.AssaultDuration + Config.AssaultDelay)
                        * 1000L) {
            assaultLost();
        }
    }

    private void assaultWon() {
        // assault was successful
        assault.getStage().set(Assault.Stage.INACTIVE);
        String broadcast = String.format(I18nSupport.getInternationalisedString("Assault - Assault Successful"),
                assault.getRegionName());
        Bukkit.getServer().broadcastMessage(broadcast);
        Bukkit.getPluginManager().callEvent(new AssaultWinEvent(assault));
        MovecraftWorldGuard.getInstance().getWGUtils().setTNTDeny(assault.getRegionName(), assault.getWorld());

        AssaultBroadcastEvent event = new AssaultBroadcastEvent(assault, broadcast, AssaultBroadcastEvent.Type.WIN);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!assault.makeBeacon()) {
            broadcast = ERROR_PREFIX + " "
                    + String.format(I18nSupport.getInternationalisedString("Assault - Beacon Placement Failed"),
                            assault.getRegionName());
            Bukkit.getServer().broadcastMessage(broadcast);

            event = new AssaultBroadcastEvent(assault, broadcast, AssaultBroadcastEvent.Type.BEACON_FAIL);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        MovecraftWorldGuard.getInstance().getWGUtils().clearOwners(assault.getRegionName(), assault.getWorld());
        MovecraftWarfare.getInstance().getAssaultManager().getAssaults().remove(assault);
    }

    private void assaultLost() {
        // assault has failed to reach damage cap within required time
        assault.getStage().set(Assault.Stage.INACTIVE);
        String broadcast = String.format(I18nSupport.getInternationalisedString("Assault - Assault Failed"),
                assault.getRegionName());
        Bukkit.getServer().broadcastMessage(broadcast);
        Bukkit.getPluginManager().callEvent(new AssaultLoseEvent(assault));
        MovecraftWorldGuard.getInstance().getWGUtils().setTNTDeny(assault.getRegionName(), assault.getWorld());

        AssaultBroadcastEvent event = new AssaultBroadcastEvent(assault, broadcast, AssaultBroadcastEvent.Type.LOSE);
        Bukkit.getServer().getPluginManager().callEvent(event);

        // repair the damages that have occurred so far
        if (!MovecraftWarfare.getInstance().getAssaultManager().getRepairUtils()
                .repairRegionRepairState(assault.getWorld(), assault.getRegionName(), null)) {
            broadcast = ERROR_PREFIX + " "
                    + String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"),
                            assault.getRegionName().toUpperCase());
            Bukkit.getServer().broadcastMessage(broadcast);

            event = new AssaultBroadcastEvent(assault, broadcast, AssaultBroadcastEvent.Type.REPAIR_FAIL);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        MovecraftWarfare.getInstance().getAssaultManager().getAssaults().remove(assault);

        // Try to save a record to the assault info file
        AssaultUtils.saveInfoFile(assault);
    }
}
