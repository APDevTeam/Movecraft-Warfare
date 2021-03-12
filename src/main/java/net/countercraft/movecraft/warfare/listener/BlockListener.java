package net.countercraft.movecraft.warfare.listener;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BlockListener implements Listener {
    final int[] fragileBlocks = new int[]{26, 34, 50, 55, 63, 64, 65, 68, 69, 70, 71, 72, 75, 76, 77, 93, 94, 96, 131, 132, 143, 147, 148, 149, 150, 151, 171, 323, 324, 330, 331, 356, 404};
    private long lastDamagesUpdate = 0;

    @EventHandler(priority = EventPriority.NORMAL)
    public void explodeEvent(EntityExplodeEvent e) {
        List<Assault> assaults = MovecraftWarfare.getInstance().getAssaultManager() != null ? MovecraftWarfare.getInstance().getAssaultManager().getAssaults() : null;
        if (assaults == null || assaults.size() == 0) {
            return;
        }

        for (final Assault assault : assaults) {
            Iterator<Block> i = e.blockList().iterator();
            while (i.hasNext()) {
                Block b = i.next();
                if (b.getWorld() != assault.getWorld())
                    continue;

                Location l = b.getLocation();
                if (!MovecraftWorldGuard.getInstance().getWGUtils().regionContains(assault.getRegionName(), l))
                    continue;

                // first see if it is outside the destroyable area
                MovecraftLocation min = assault.getMinPos();
                MovecraftLocation max = assault.getMaxPos();

                if (l.getBlockX() < min.getX() ||
                        l.getBlockX() > max.getX() ||
                        l.getBlockZ() < min.getZ() ||
                        l.getBlockZ() > max.getZ() ||
                        !Config.AssaultDestroyableBlocks.contains(b.getType()) ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.SOUTH).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.DOWN).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.UP).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.EAST).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.WEST).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.NORTH).getTypeId()) >= 0) {
                    i.remove();
                }


                // whether or not you actually destroyed the block, add to damages
                long damages = assault.getDamages() + Config.AssaultDamagesPerBlock;
                assault.setDamages(Math.min(damages, assault.getMaxDamages()));

                // notify nearby players of the damages, do this 1 second later so all damages from this volley will be included
                if (System.currentTimeMillis() < lastDamagesUpdate + 4000) {
                    continue;
                }
                final Location floc = l;
                final World fworld = b.getWorld();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        long fdamages = assault.getDamages();
                        for (Player p : fworld.getPlayers()) {
                            if (Math.round(p.getLocation().getBlockX() / 1000.0) == Math.round(floc.getBlockX() / 1000.0) &&
                                    Math.round(p.getLocation().getBlockZ() / 1000.0) == Math.round(floc.getBlockZ() / 1000.0)) {
                                p.sendMessage(I18nSupport.getInternationalisedString("Damage") + ": " + fdamages);
                            }
                        }
                    }
                }.runTaskLater(Movecraft.getInstance(), 20);
                lastDamagesUpdate = System.currentTimeMillis();
            }
        }
    }
}