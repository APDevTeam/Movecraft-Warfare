package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.config.Config;
import org.bukkit.Chunk;
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

    public Clipboard loadRegionRepairStateClipboard(String s, World world) {
        File dataDirectory = new File(plugin.getDataFolder(), "AssaultSnapshots");
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
