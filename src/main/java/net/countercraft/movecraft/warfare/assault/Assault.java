package net.countercraft.movecraft.warfare.assault;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an assault
 */
public class Assault {
    private final @NotNull ProtectedRegion region;
    private final UUID starterUUID;
    private long startTime;
    private long damages;
    private final long maxDamages;
    private final World world;
    private final Vector minPos, maxPos;
    private final AtomicReference<AssaultStage> stage = new AtomicReference<>(AssaultStage.PREPARATION);

    public Assault(@NotNull ProtectedRegion region, Player starter, World world, long maxDamages, Vector minPos, Vector maxPos) {
        this.region = region;
        starterUUID = starter.getUniqueId();
        this.world = world;
        this.startTime = 0L;
        this.maxDamages = maxDamages;
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    public Vector getMaxPos() {
        return maxPos;
    }

    public Vector getMinPos() {
        return minPos;
    }

    public World getWorld() {
        return world;
    }

    @NotNull
    public ProtectedRegion getRegion() {
        return region;
    }

    public long getStartTime() {
        return startTime;
    }

    void setStartTime(long startTime) {
        this.startTime = startTime;
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
        return region.getId();
    }

    public AtomicReference<AssaultStage> getStage() {
        return stage;
    }

    public boolean makeBeacon() {
        //first, find a position for the repair beacon
        int beaconX = minPos.getBlockX();
        int beaconZ = minPos.getBlockZ();
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
        s.setLine(0, ChatColor.RED + I18nSupport.getInternationalisedString("Region Damaged"));
        s.setLine(1, I18nSupport.getInternationalisedString("Region Name") + ":" + getRegionName());
        s.setLine(2, I18nSupport.getInternationalisedString("Damages") + ":" + getMaxDamages());
        s.setLine(3, I18nSupport.getInternationalisedString("Region Owner") + ":" + AssaultUtils.getRegionOwnerList(region));
        s.update();
        return true;
    }
}
