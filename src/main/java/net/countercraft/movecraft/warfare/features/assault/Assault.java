package net.countercraft.movecraft.warfare.features.assault;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import net.countercraft.movecraft.warfare.features.Warfare;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;

/**
 * Represents an assault
 */
public class Assault extends Warfare {
    public enum Stage {
        IN_PROGRESS, PREPARATION, INACTIVE
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
        this.maxDamages = maxDamages;
        this.hitBox = hitBox;
        startTime = LocalDateTime.now();
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
        if (!AssaultUtils.saveInfoFile(this))
            return false;
    
        MovecraftLocation found = findBeacon();
        if (found == null)
            return false; // can't find a position
        int beaconX = found.getX();
        int beaconY = found.getY();
        int beaconZ = found.getZ();

        // now make the beacon
        makeBeaconBase(beaconX, beaconY++, beaconZ);
        makeBeaconCore(beaconX, beaconY++, beaconZ);
        makeBeaconTop(beaconX, beaconY++, beaconZ);
        makeBeaconSign(beaconX, beaconY, beaconZ);
        return true;
    }

    private boolean isValidBeaconPlacement(int beaconX, int beaconY, int beaconZ) {
        for (int x = beaconX; x < beaconX + 5; x++) {
            for (int y = beaconY; y < beaconY + 4; y++) {
                for (int z = beaconZ; z < beaconZ + 5; z++) {
                    if (!world.getBlockAt(x, y, z).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Find a position for the repair beacon
    private MovecraftLocation findBeaconY(int beaconX, int beaconZ) {
        for (int beaconY = hitBox.getMinY(); beaconY < hitBox.getMaxY() - 3; beaconY++) {
            if (isValidBeaconPlacement(beaconX, beaconY, beaconZ))
                return new MovecraftLocation(beaconX, beaconY, beaconZ);
        }
        return null; // Unable to find a clear location
    }

    private MovecraftLocation findBeacon() {
        for (int radius = 0; radius < hitBox.getXLength() + hitBox.getZLength(); radius++) {
            for (int deltaX = 0; deltaX < radius + 1 && deltaX < hitBox.getXLength(); deltaX++) {
                int deltaZ = radius - deltaX;
                if (deltaZ >= hitBox.getZLength())
                    continue;

                MovecraftLocation loc = findBeaconY(hitBox.getMinX() + deltaX, hitBox.getMinZ() + deltaZ);
                if (loc != null)
                    return loc;
            }
        }
        return null;
    }

    // A 3x3 base of bedrock
    private void makeBeaconBase(int beaconX, int y, int beaconZ) {
        for (int x = beaconX + 1; x < beaconX + 4; x++)
            for (int z = beaconZ + 1; z < beaconZ + 4; z++)
                world.getBlockAt(x, y, z).setType(Material.BEDROCK);
    }

    // A 5x5 layer with walls of bedrock and core of iron
    private void makeBeaconCore(int beaconX, int y, int beaconZ) {
        for (int x = beaconX; x < beaconX + 5; x++)
            for (int z = beaconZ; z < beaconZ + 5; z++)
                if (x == beaconX || z == beaconZ || x == beaconX + 4 || z == beaconZ + 4)
                    world.getBlockAt(x, y, z).setType(Material.BEDROCK);
                else
                    world.getBlockAt(x, y, z).setType(Material.IRON_BLOCK);
    }

    // A 3x3 layer with walls of bedrock and a core of beacon, with a cap of bedrock
    private void makeBeaconTop(int beaconX, int y, int beaconZ) {
        for (int x = beaconX + 1; x < beaconX + 4; x++)
            for (int z = beaconZ + 1; z < beaconZ + 4; z++)
                if (x == beaconX + 2 && z == beaconZ + 2)
                    world.getBlockAt(x, y, z).setType(Material.BEACON);
                else
                    world.getBlockAt(x, y, z).setType(Material.BEDROCK);
        world.getBlockAt(beaconX + 2, ++y, beaconZ + 2).setType(Material.BEDROCK);
    }

    // Make the beacon sign
    private void makeBeaconSign(int beaconX, int beaconY, int beaconZ) {
        // Create sign
        world.getBlockAt(beaconX + 2, beaconY, beaconZ + 1).setType(Material.OAK_WALL_SIGN);
        Sign s = (Sign) world.getBlockAt(beaconX + 2, beaconY, beaconZ + 1).getState();

        // Put header, region name, damages and local date time on sign
        s.setLine(0, RegionDamagedSign.HEADER);
        s.setLine(1, I18nSupport.getInternationalisedString("Region Name") + ":" + getRegionName());
        s.setLine(2, I18nSupport.getInternationalisedString("Damages") + ":" + getMaxDamages());
        s.setLine(3, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startTime));
        s.update();
    }
}
