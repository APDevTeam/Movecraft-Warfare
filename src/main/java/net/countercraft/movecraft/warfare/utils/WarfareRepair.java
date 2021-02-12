package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.mapUpdater.MapUpdateManager;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.repair.mapUpdater.WE6UpdateCommand;
import net.countercraft.movecraft.warfare.config.Config;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

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

        HashSet<Chunk> chunks = new HashSet<>();
        for(int x = Math.floorDiv(region.getMinimumPoint().getBlockX(), 16); x <= Math.floorDiv(region.getMaximumPoint().getBlockX(), 16) + 1; x++) {
            for(int z = Math.floorDiv(region.getMinimumPoint().getBlockZ(), 16); z <= Math.floorDiv(region.getMaximumPoint().getBlockZ(), 16) + 1; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }

        for(Chunk c : chunks) {
            if(!MovecraftRepair.getInstance().getWEUtils().saveChunk(c, saveDirectory, Config.AssaultDestroyableBlocks))
                return false;
        }
        return true;
    }

    public boolean repairRegionRepairState(World w, String regionName) {
        if (w == null || regionName == null)
            return false;

        Clipboard clipboard = loadRegionRepairStateClipboard(regionName, w);
        if (clipboard == null) {
            return false;
        }
        int minx = clipboard.getMinimumPoint().getBlockX();
        int miny = clipboard.getMinimumPoint().getBlockY();
        int minz = clipboard.getMinimumPoint().getBlockZ();
        int maxx = clipboard.getMaximumPoint().getBlockX();
        int maxy = clipboard.getMaximumPoint().getBlockY();
        int maxz = clipboard.getMaximumPoint().getBlockZ();
        for (int x = minx; x < maxx; x++) {
            for (int y = miny; y < maxy; y++) {
                for (int z = minz; z < maxz; z++) {
                    Vector ccloc = new Vector(x, y, z);
                    BaseBlock bb = clipboard.getBlock(ccloc);
                    if (!bb.isAir()) { // most blocks will be air, quickly move on to the next. This loop will run 16 million times, needs to be fast
                        if (Config.AssaultDestroyableBlocks.contains(bb.getType())) {
                            if (!w.getChunkAt(x >> 4, z >> 4).isLoaded())
                                w.loadChunk(x >> 4, z >> 4);
                            if (w.getBlockAt(x, y, z).isEmpty() || w.getBlockAt(x, y, z).isLiquid()) {
                                MovecraftLocation moveloc = new MovecraftLocation(x, y, z);
                                WE6UpdateCommand updateCommand = new WE6UpdateCommand(bb, w, moveloc, Material.getMaterial(bb.getType()), (byte) bb.getData());
                                MapUpdateManager.getInstance().scheduleUpdate(updateCommand);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private Clipboard loadRegionRepairStateClipboard(String s, World world) {
        File dataDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots");
        //        File saveDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots/" + regionName.replaceAll("´\\s+", "_"));
        File file = new File(dataDirectory, s + ".schematic"); // The schematic file
        com.sk89q.worldedit.world.World weWorld = new BukkitWorld(world);
        WorldData worldData = weWorld.getWorldData();
        try {
            return ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(file)).read(worldData);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
