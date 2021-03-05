package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
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

    public void saveRegionRepairState(World world, Assault assault) {
        File saveDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots/" + assault.getRegion().getId().replaceAll("´\\s+", "_"));

        Queue<Chunk> chunks = getChunksInRegion(assault.getRegion(), world);

        ChunkSaveTask saveTask = new ChunkSaveTask(assault, chunks, saveDirectory);
        saveTask.runTaskTimer(MovecraftWarfare.getInstance(), 2, Config.AssaultChunkSavePeriod);
    }

    public boolean repairRegionRepairState(World world, String regionName, @Nullable Player player) {
        if (world == null || regionName == null)
            return false;

        ProtectedRegion region = Movecraft.getInstance().getWorldGuardPlugin().getRegionManager(world).getRegion(regionName);
        if(region == null)
            return false;

        File saveDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots/" + regionName.replaceAll("´\\s+", "_"));

        Queue<Chunk> chunks = getChunksInRegion(region, world);
        Predicate<MovecraftLocation> regionTester = new IsInRegion(region);

        ChunkRepairTask repairTask = new ChunkRepairTask(region, chunks, saveDirectory, regionTester, player);
        repairTask.runTaskTimer(MovecraftWarfare.getInstance(), 2, Config.AssaultChunkRepairPeriod);
        return true;
    }

    private Queue<Chunk> getChunksInRegion(ProtectedRegion region, World world) {
        Queue<Chunk> chunks = new LinkedList();
        for(int x = (int) Math.floor(region.getMinimumPoint().getBlockX() / 16.0); x < Math.floor(region.getMaximumPoint().getBlockX() / 16.0) + 1; x++) {
            for(int z = (int) Math.floor(region.getMinimumPoint().getBlockZ() / 16.0); z < Math.floor(region.getMaximumPoint().getBlockZ() / 16.0) + 1; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }
}
