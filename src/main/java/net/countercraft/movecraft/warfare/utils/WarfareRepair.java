package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.config.Config;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashSet;
import java.util.function.Predicate;

public class WarfareRepair {
    private static WarfareRepair instance;
    private final Plugin plugin;

    public WarfareRepair(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static WarfareRepair getInstance() {
        return instance;
    }

    public boolean saveRegionRepairState(World world, ProtectedRegion region) {
        File saveDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots/" + region.getId().replaceAll("´\\s+", "_"));

        HashSet<Chunk> chunks = getChunksInRegion(region, world);

        // TODO: Make this spread across multiple ticks and possibly async
        for(Chunk c : chunks) {
            if(!MovecraftRepair.getInstance().getWEUtils().saveChunk(c, saveDirectory, Config.AssaultDestroyableBlocks))
                return false;
        }
        return true;
    }

    public boolean repairRegionRepairState(World world, String regionName) {
        if (world == null || regionName == null)
            return false;

        ProtectedRegion region = Movecraft.getInstance().getWorldGuardPlugin().getRegionManager(world).getRegion(regionName);
        if(region == null)
            return false;

        File saveDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots/" + regionName.replaceAll("´\\s+", "_"));

        HashSet<Chunk> chunks = getChunksInRegion(region, world);
        Predicate<MovecraftLocation> regionTester = new IsInRegion(region);

        // TODO: Make this spread across multiple ticks and possibly async
        for(Chunk c : chunks) {
            if(!MovecraftRepair.getInstance().getWEUtils().repairChunk(c, saveDirectory, regionTester))
                return false;
        }
        return true;
    }

    private HashSet<Chunk> getChunksInRegion(ProtectedRegion region, World world) {
        HashSet<Chunk> chunks = new HashSet<>();
        for(int x = (int) Math.floor(region.getMinimumPoint().getBlockX() / 16.0); x < Math.floor(region.getMaximumPoint().getBlockX() / 16.0) + 1; x++) {
            for(int z = (int) Math.floor(region.getMinimumPoint().getBlockZ() / 16.0); z < Math.floor(region.getMaximumPoint().getBlockZ() / 16.0) + 1; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }
}
