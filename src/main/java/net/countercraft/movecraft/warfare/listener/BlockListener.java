package net.countercraft.movecraft.listener;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.MathUtils;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.assault.Assault;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BlockListener implements Listener {

    final int[] fragileBlocks = new int[]{26, 34, 50, 55, 63, 64, 65, 68, 69, 70, 71, 72, 75, 76, 77, 93, 94, 96, 131, 132, 143, 147, 148, 149, 150, 151, 171, 323, 324, 330, 331, 356, 404};
    private long lastDamagesUpdate = 0;

    @EventHandler(priority = EventPriority.NORMAL)
    public void explodeEvent(EntityExplodeEvent e) {
        processAssault(e);
    }

    //TODO: move to Warfare plugin
    private void processAssault(EntityExplodeEvent e){
        List<Assault> assaults = MovecraftWarfare.getInstance().getAssaultManager() != null ? MovecraftWarfare.getInstance().getAssaultManager().getAssaults() : null;
        if (assaults == null || assaults.size() == 0) {
            return;
        }
        WorldGuardPlugin worldGuard = Movecraft.getInstance().getWorldGuardPlugin();
        for (final Assault assault : assaults) {
            Iterator<Block> i = e.blockList().iterator();
            while (i.hasNext()) {
                Block b = i.next();
                if (b.getWorld() != assault.getWorld())
                    continue;
                ApplicableRegionSet regions = worldGuard.getRegionManager(b.getWorld()).getApplicableRegions(b.getLocation());
                boolean isInAssaultRegion = false;
                for (com.sk89q.worldguard.protection.regions.ProtectedRegion tregion : regions.getRegions()) {
                    if (assault.getRegionName().equals(tregion.getId())) {
                        isInAssaultRegion = true;
                    }
                }
                if (!isInAssaultRegion)
                    continue;
                // first see if it is outside the destroyable area
                com.sk89q.worldedit.Vector min = assault.getMinPos();
                com.sk89q.worldedit.Vector max = assault.getMaxPos();

                if (b.getLocation().getBlockX() < min.getBlockX() ||
                        b.getLocation().getBlockX() > max.getBlockX() ||
                        b.getLocation().getBlockZ() < min.getBlockZ() ||
                        b.getLocation().getBlockZ() > max.getBlockZ() ||
                        !Settings.AssaultDestroyableBlocks.contains(b.getTypeId()) ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.SOUTH).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.DOWN).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.UP).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.EAST).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.WEST).getTypeId()) >= 0 ||
                        Arrays.binarySearch(fragileBlocks, b.getRelative(BlockFace.NORTH).getTypeId()) >= 0) {
                    i.remove();
                }


                // whether or not you actually destroyed the block, add to damages
                long damages = assault.getDamages() + Settings.AssaultDamagesPerBlock;
                assault.setDamages(Math.min(damages, assault.getMaxDamages()));

                // notify nearby players of the damages, do this 1 second later so all damages from this volley will be included
                if (System.currentTimeMillis() < lastDamagesUpdate + 4000) {
                    continue;
                }
                final Location floc = b.getLocation();
                final World fworld = b.getWorld();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        long fdamages = assault.getDamages();
                        for (Player p : fworld.getPlayers()) {
                            if (Math.round(p.getLocation().getBlockX() / 1000.0) == Math.round(floc.getBlockX() / 1000.0) &&
                                    Math.round(p.getLocation().getBlockZ() / 1000.0) == Math.round(floc.getBlockZ() / 1000.0)) {
                                p.sendMessage(I18nSupport.getInternationalisedString("Damage")+": " + fdamages);
                            }
                        }
                    }
                }.runTaskLater(Movecraft.getInstance(), 20);
                lastDamagesUpdate = System.currentTimeMillis();

            }
        }

    }

    @Nullable
    private Craft adjacentCraft(@NotNull Location location) {
        for (Craft craft : CraftManager.getInstance().getCraftsInWorld(location.getWorld())) {
            if (!MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(location))) {
                continue;
            }
            return craft;
        }
        return null;
    }

    private boolean pistonFacingLocation(Location loc) {
        final Vector[] SHIFTS = {new Vector(0,1,0), new Vector(0,-1,0),
                new Vector(1,0,0), new Vector(-1,0,0),
                new Vector(0,0,1), new Vector(0,0,-1)};
        for (Vector shift : SHIFTS) {
            final Location test = loc.add(shift);
            if (!(test.getBlock().getState().getData() instanceof PistonBaseMaterial)) {
                continue;
            }
            PistonBaseMaterial piston = (PistonBaseMaterial) test.getBlock().getState().getData();
            if (!test.getBlock().getRelative(piston.getFacing()).getLocation().equals(loc)) {
                continue;
            }
            return true;
        }
        return false;
    }
}