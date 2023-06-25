package net.countercraft.movecraft.warfare.features.assault;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.repair.util.WarfareUtils;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.tasks.ChunkRepairTask;
import net.countercraft.movecraft.warfare.features.assault.tasks.ChunkSaveTask;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Queue;
import java.util.function.Predicate;

public class WarfareRepair {
    private final Plugin plugin;
    private final WarfareUtils warfareUtils;

    public WarfareRepair(Plugin plugin) {
        this.plugin = plugin;
        warfareUtils = new WarfareUtils();
    }

    public void saveRegionRepairState(World world, Assault assault) {
        File saveDirectory = new File(plugin.getDataFolder(),
                "AssaultSnapshots/" + assault.getRegionName().replaceAll("´\\s+", "_"));

        Queue<Chunk> chunks = getChunksInRegion(assault.getRegionName(), world);

        ChunkSaveTask saveTask = new ChunkSaveTask(assault, chunks, saveDirectory);
        saveTask.runTaskTimer(MovecraftWarfare.getInstance(), 2, Config.AssaultChunkSavePeriod);
    }

    public boolean repairRegionRepairState(World world, String regionName, @Nullable Player player) {
        if (world == null || regionName == null)
            return false;

        if (!MovecraftWorldGuard.getInstance().getWGUtils().regionExists(regionName, world))
            return false;

        File saveDirectory = new File(plugin.getDataFolder(),
                "AssaultSnapshots/" + regionName.replaceAll("´\\s+", "_"));

        Queue<Chunk> chunks = getChunksInRegion(regionName, world);
        Predicate<MovecraftLocation> regionTester = MovecraftWorldGuard.getInstance().getWGUtils()
                .getIsInRegion(regionName, world);
        if (regionTester == null)
            return false;

        ChunkRepairTask repairTask = new ChunkRepairTask(regionName, chunks, saveDirectory, regionTester, player);
        repairTask.runTaskTimer(MovecraftWarfare.getInstance(), 2, Config.AssaultChunkRepairPeriod);
        return true;
    }

    @Nullable
    private Queue<Chunk> getChunksInRegion(String regionName, World w) {
        return MovecraftWorldGuard.getInstance().getWGUtils().getChunksInRegion(regionName, w);
    }

    public WarfareUtils getWarfareUtils() {
        return warfareUtils;
    }
}
