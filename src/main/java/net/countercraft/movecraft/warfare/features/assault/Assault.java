package net.countercraft.movecraft.warfare.features.assault;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import net.countercraft.movecraft.warfare.features.Warfare;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

/**
 * Represents an assault
 */
public class Assault extends Warfare {
    public enum Stage {
        IN_PROGRESS, PREPERATION, INACTIVE
    }
    public enum SavedState {
        UNSAVED, SAVED, FAILED
    }

    private final String regionName;
    private final UUID starter;
    private final LocalDateTime startTime;
    private long damages;
    private final long maxDamages;
    private final World world;
    private final SolidHitBox hitBox;
    private final AtomicReference<Stage> stage = new AtomicReference<>(Stage.INACTIVE);
    private final AtomicReference<SavedState> savedCorrectly = new AtomicReference<>(SavedState.UNSAVED);

    public Assault(String regionName, UUID starter, World world, long maxDamages, SolidHitBox hitBox) {
        this.regionName = regionName;
        this.starter = starter;
        this.world = world;
        this.startTime = LocalDateTime.now();
        this.maxDamages = maxDamages;
        this.hitBox = hitBox;
    }

    public SolidHitBox getHitBox() {
        return hitBox;
    }

    public World getWorld() {
        return world;
    }

    public LocalDateTime getStartTime() {
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
        return starter;
    }

    public String getRegionName() {
        return regionName;
    }

    public AtomicReference<Stage> getStage() {
        return stage;
    }

    public AtomicReference<SavedState> getSavedCorrectly() {
        return savedCorrectly;
    }

    public boolean makeBeacon() {
        // first, find a height position for the repair beacon
        int beaconX = hitBox.getMinX();
        int beaconZ = hitBox.getMinZ();
        int beaconY;
        for (beaconY = hitBox.getMaxY(); beaconY > hitBox.getMinY(); beaconY--) {
            if (world.getBlockAt(beaconX, beaconY, beaconZ).getType().isSolid()) {
                beaconY++;
                break;
            }
        }
        if (beaconY > hitBox.getMaxY() - 5)
            return false; // no vertical room for beacon
        
        // next, find a position for the beacon that is empty
        int x, y, z;
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

        // now make the beacon

        // A 3x3 base of bedrock
        y = beaconY;
        for (x = beaconX + 1; x < beaconX + 4; x++)
            for (z = beaconZ + 1; z < beaconZ + 4; z++)
                world.getBlockAt(x, y, z).setType(Material.BEDROCK);

        // A 5x5 layer with walls of bedrock and core of iron
        y++;
        for (x = beaconX; x < beaconX + 5; x++)
            for (z = beaconZ; z < beaconZ + 5; z++)
                if (x == beaconX || z == beaconZ || x == beaconX + 4 || z == beaconZ + 4)
                    world.getBlockAt(x, y, z).setType(Material.BEDROCK);
                else
                    world.getBlockAt(x, y, z).setType(Material.IRON_BLOCK);

        // A 3x3 layer with walls of bedrock and a core of beacon
        y++;
        for (x = beaconX + 1; x < beaconX + 4; x++)
            for (z = beaconZ + 1; z < beaconZ + 4; z++)
                world.getBlockAt(x, y, z).setType(Material.BEDROCK);
        world.getBlockAt(beaconX + 2, y, beaconZ + 2).setType(Material.BEACON);

        // A 1x1 layer of bedrock
        y++;
        world.getBlockAt(beaconX + 2, y, beaconZ + 2).setType(Material.BEDROCK);

        // Finally, the sign
        world.getBlockAt(beaconX + 2, beaconY + 3, beaconZ + 1).setType(Material.OAK_WALL_SIGN);
        Sign s = (Sign) world.getBlockAt(beaconX + 2, beaconY + 3, beaconZ + 1).getState();
        s.setLine(0, RegionDamagedSign.HEADER);
        s.setLine(1, I18nSupport.getInternationalisedString("Region Name") + ":" + getRegionName());
        s.setLine(2, I18nSupport.getInternationalisedString("Damages") + ":" + getMaxDamages());
        s.setLine(3, I18nSupport.getInternationalisedString("Region Owner") + ":" + MovecraftWorldGuard.getInstance().getWGUtils().getRegionOwnerList(regionName, world));
        s.update();
        return true;
    }
}
