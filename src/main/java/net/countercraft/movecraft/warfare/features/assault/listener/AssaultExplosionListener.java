package net.countercraft.movecraft.warfare.features.assault.listener;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.Assault;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AssaultExplosionListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void explodeEvent(EntityExplodeEvent e) {
        if (MovecraftWarfare.getInstance().getAssaultManager() == null)
            return;
        List<Assault> assaults = MovecraftWarfare.getInstance().getAssaultManager().getAssaults();
        if (assaults == null || assaults.isEmpty())
            return;

        for (final Assault assault : assaults) {
            if (e.getLocation().getWorld() != assault.getWorld())
                continue;

            e.blockList().removeAll(processAssault(assault, e.blockList()));
        }
    }

    @NotNull
    private Set<Block> processAssault(Assault assault, @NotNull List<Block> blockList) {
        Set<Block> result = new HashSet<>();
        int exploded = 0;
        for (Block b : blockList) {
            // first see if it is outside the region area
            Location l = b.getLocation();
            if (!MovecraftWorldGuard.getInstance().getWGUtils().regionContains(assault.getRegionName(), l))
                continue;

            // remove if outside assault area
            if (!assault.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(l))) {
                result.add(b);
                continue;
            }

            // remove if not destroyable
            if (!Config.AssaultDestroyableBlocks.contains(b.getType())) {
                result.add(b);
                continue;
            }

            // remove if fragile
            if (isFragile(b)) {
                result.add(b);
                continue;
            }

            exploded++;
        }

        // whether you actually destroyed the block or not, add to damages
        long damages = exploded + result.size();
        damages *= Config.AssaultDamagesPerBlock;
        damages += assault.getDamages();
        assault.setDamages(Math.min(damages, assault.getMaxDamages()));

        return result;
    }

    private boolean isFragile(@NotNull Block base) {
        for (Block b : getNearbyBlocks(base)) {
            if (Tags.FRAGILE_MATERIALS.contains(b.getType()))
                return true;
        }
        return false;
    }

    @NotNull
    private Set<Block> getNearbyBlocks(@NotNull Block b) {
        Set<Block> blocks = new HashSet<>();
        blocks.add(b.getRelative(BlockFace.SOUTH));
        blocks.add(b.getRelative(BlockFace.DOWN));
        blocks.add(b.getRelative(BlockFace.UP));
        blocks.add(b.getRelative(BlockFace.EAST));
        blocks.add(b.getRelative(BlockFace.WEST));
        blocks.add(b.getRelative(BlockFace.NORTH));
        return blocks;
    }
}
