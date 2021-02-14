package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.function.Predicate;

public class IsInRegion implements Predicate<Location> {
    private final ProtectedRegion region;

    public IsInRegion(ProtectedRegion region) {
        this.region = region;
    }

    public boolean test(Location location) {
        return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}