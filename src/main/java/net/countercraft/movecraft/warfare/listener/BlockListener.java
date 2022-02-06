package net.countercraft.movecraft.warfare.listener;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class BlockListener implements Listener {
    private long lastDamagesUpdate = 0;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        if(!(e.getBlock().getState() instanceof Sign))
            return;

        Sign s = (Sign) e.getBlock().getState();
        if (s.getLine(0).equalsIgnoreCase(
                ChatColor.RED + I18nSupport.getInternationalisedString("Region Damaged"))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void explodeEvent(EntityExplodeEvent e) {
        List<Assault> assaults = MovecraftWarfare.getInstance().getAssaultManager() != null ? MovecraftWarfare.getInstance().getAssaultManager().getAssaults() : null;
        if (assaults == null || assaults.size() == 0)
            return;

        for (final Assault assault : assaults) {
            if(e.getLocation().getWorld() != assault.getWorld())
                continue;

            Iterator<Block> i = e.blockList().iterator();
            while (i.hasNext()) {
                Block b = i.next();
                // first see if it is outside the region area
                Location l = b.getLocation();
                if (!MovecraftWorldGuard.getInstance().getWGUtils().regionContains(assault.getRegionName(), l))
                    continue;

                MovecraftLocation min = assault.getMinPos();
                MovecraftLocation max = assault.getMaxPos();

                // remove it outside assault area
                if (l.getBlockX() < min.getX() ||
                        l.getBlockX() > max.getX() ||
                        l.getBlockZ() < min.getZ() ||
                        l.getBlockZ() > max.getZ())
                    i.remove();

                // remove if not destroyable
                if(!Config.AssaultDestroyableBlocks.contains(b.getType()))
                    i.remove();

                // remove if fragile
                if(isFragile(b))
                    i.remove();

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

    private boolean isFragile(@NotNull Block base) {
        for(Block b : getNearbyBlocks(base)) {
            if(Tags.FRAGILE_MATERIALS.contains(b.getType()))
                return true;
        }
        return false;
    }

    @NotNull
    private HashSet<Block> getNearbyBlocks(@NotNull Block b) {
        HashSet<Block> blocks = new HashSet<>();
        blocks.add(b.getRelative(BlockFace.SOUTH));
        blocks.add(b.getRelative(BlockFace.DOWN));
        blocks.add(b.getRelative(BlockFace.UP));
        blocks.add(b.getRelative(BlockFace.EAST));
        blocks.add(b.getRelative(BlockFace.WEST));
        blocks.add(b.getRelative(BlockFace.NORTH));
        return blocks;
    }
}