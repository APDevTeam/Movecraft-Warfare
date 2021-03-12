package net.countercraft.movecraft.warfare.assault;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.sign.RegionDamagedSign;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an assault
 */
public class Assault {
    public static class SavedState {
        public static final int UNSAVED = 0;
        public static final int SAVED = 1;
        public static final int FAILED = -1;
    }

    private final String regionName;
    private final UUID starterUUID;
    private final long startTime;
    private long damages;
    private final long maxDamages;
    private final World world;
    private final MovecraftLocation minPos, maxPos;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger savedCorrectly = new AtomicInteger(SavedState.UNSAVED);

    public Assault(String regionName, Player starter, World world, long startTime, long maxDamages, MovecraftLocation minPos, MovecraftLocation maxPos) {
        this.regionName = regionName;
        starterUUID = starter.getUniqueId();
        this.world = world;
        this.startTime = startTime;
        this.maxDamages = maxDamages;
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    public MovecraftLocation getMaxPos() {
        return maxPos;
    }

    public MovecraftLocation getMinPos() {
        return minPos;
    }

    public World getWorld() {
        return world;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDamages() {
        return damages;
    }

    public void setDamages(long damages) {
        this.damages = damages;
    }

    public long getMaxDamages() {
        return maxDamages;
    }

    public UUID getStarterUUID() {
        return starterUUID;
    }


    public String getRegionName() {
        return regionName;
    }

    public AtomicBoolean getRunning() {
        return running;
    }


    public boolean makeBeacon() {
        //first, find a position for the repair beacon
        int beaconX = minPos.getX();
        int beaconZ = minPos.getZ();
        int beaconY;
        for(beaconY = 255; beaconY > 0; beaconY--) {
            if(world.getBlockAt(beaconX, beaconY, beaconZ).getType().isOccluding()) {
                beaconY++;
                break;
            }
        }
        if(beaconY > 250)
            return false;

        int x, y, z;
        for (x = beaconX; x < beaconX + 5; x++)
            for (z = beaconZ; z < beaconZ + 5; z++)
                if (!world.isChunkLoaded(x >> 4, z >> z))
                    world.loadChunk(x >> 4, z >> 4);
        boolean empty = false;
        while (!empty && beaconY < 250) {
            empty = true;
            beaconY++;
            for (x = beaconX; x < beaconX + 5; x++) {
                for (y = beaconY; y < beaconY + 4; y++) {
                    for (z = beaconZ; z < beaconZ + 5; z++) {
                        if (!world.getBlockAt(x, y, z).isEmpty())
                            empty = false;
                    }
                }
            }
        }

        //now make the beacon
        y = beaconY;
        for (x = beaconX + 1; x < beaconX + 4; x++)
            for (z = beaconZ + 1; z < beaconZ + 4; z++)
                world.getBlockAt(x, y, z).setType(Material.BEDROCK);
        y = beaconY + 1;
        for (x = beaconX; x < beaconX + 5; x++)
            for (z = beaconZ; z < beaconZ + 5; z++)
                if (x == beaconX || z == beaconZ || x == beaconX + 4 || z == beaconZ + 4)
                    world.getBlockAt(x, y, z).setType(Material.BEDROCK);
                else
                    world.getBlockAt(x, y, z).setType(Material.IRON_BLOCK);
        y = beaconY + 2;
        for (x = beaconX + 1; x < beaconX + 4; x++)
            for (z = beaconZ + 1; z < beaconZ + 4; z++)
                world.getBlockAt(x, y, z).setType(Material.BEDROCK);

        world.getBlockAt(beaconX + 2, beaconY + 2, beaconZ + 2).setType(Material.BEACON);
        world.getBlockAt(beaconX + 2, beaconY + 3, beaconZ + 2).setType(Material.BEDROCK);
        // finally the sign on the beacon
        world.getBlockAt(beaconX + 2, beaconY + 3, beaconZ + 1).setType(Material.WALL_SIGN);
        Sign s = (Sign) world.getBlockAt(beaconX + 2, beaconY + 3, beaconZ + 1).getState();
        s.setLine(0, RegionDamagedSign.HEADER);
        s.setLine(1, I18nSupport.getInternationalisedString("Region Name") + ":" + getRegionName());
        s.setLine(2, I18nSupport.getInternationalisedString("Damages") + ":" + getMaxDamages());
        s.setLine(3, I18nSupport.getInternationalisedString("Region Owner") + ":" + MovecraftWorldGuard.getInstance().getWGUtils().getRegionOwnerList(regionName, world));
        s.update();
        return true;
    }

    public AtomicInteger getSavedCorrectly() {
        return savedCorrectly;
    }
}
