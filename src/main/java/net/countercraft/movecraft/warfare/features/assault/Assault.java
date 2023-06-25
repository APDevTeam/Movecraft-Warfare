package net.countercraft.movecraft.warfare.features.assault;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import net.countercraft.movecraft.warfare.MovecraftWarfare;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import net.countercraft.movecraft.warfare.features.Warfare;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

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
        var foundY = findBeaconY();
        if (!foundY.getLeft())
            return false; // can't find a position
        int beaconX = hitBox.getMinX();
        int beaconY = foundY.getRight();
        int beaconZ = hitBox.getMinZ();

        // now make the beacon
        makeBeaconBase(beaconX, beaconY++, beaconZ);
        makeBeaconCore(beaconX, beaconY++, beaconZ);
        makeBeaconTop(beaconX, beaconY++, beaconZ);
        makeBeaconSign(beaconX, beaconY, beaconZ);
        return makeBeaconFile();
    }

    // Find a position for the repair beacon
    private Pair<Boolean, Integer> findBeaconY() {
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
            return new Pair<>(false, beaconY); // no vertical room for beacon

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
        return new Pair<>(true, beaconY);
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

    // Make the info file
    private boolean makeBeaconFile() {
        // Create file path, get owners, save to data, convert to pretty JSON, and save to file
        File saveDirectory = new File(MovecraftWarfare.getInstance().getDataFolder(),
                "AssaultSnapshots/" + regionName.replaceAll("´\\s+", "_"));
        if (!saveDirectory.exists())
            saveDirectory.mkdirs();
        File file = new File(saveDirectory, "info.json");

        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, world);
        AssaultData data = new AssaultData(owners, startTime);

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        try {
			gson.toJson(data, new FileWriter(file));
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
            return false;
		}
        return true;
    }
}
