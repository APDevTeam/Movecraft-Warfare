package net.countercraft.movecraft.warfare.features.assault.listener;

import net.countercraft.movecraft.repair.events.ProtoRepairCreateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import net.countercraft.movecraft.worldguard.utils.WorldGuardUtils;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CraftRepairListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onProtoRepairCreate(@NotNull ProtoRepairCreateEvent e) {
        HitBox hitBox = e.getProtoRepair().getHitBox();
        if (hitBox.isEmpty())
            return;

        WorldGuardUtils wgUtils = MovecraftWorldGuard.getInstance().getWGUtils();
        World w = e.getProtoRepair().getWorld();
        for (String regionName : wgUtils.getRegions(hitBox, w)) {
            // Detect an assaulted region by one without owners and with TNT denied
            if (!wgUtils.isTNTDenied(regionName, w) || !wgUtils.getUUIDOwners(regionName, w).isEmpty())
                continue;

            e.setCancelled(true);
            e.setFailMessage(I18nSupport.getInternationalisedString("Assault Repair - Not Permitted"));
            return;
        }
    }
}
