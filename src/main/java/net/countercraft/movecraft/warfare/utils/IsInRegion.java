package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.MovecraftLocation;

import java.util.function.Predicate;

public class IsInRegion implements Predicate<MovecraftLocation> {
    private final ProtectedRegion region;

    public IsInRegion(ProtectedRegion region) {
        this.region = region;
    }

    public boolean test(MovecraftLocation location) {
        return region.contains(location.getX(), location.getY(), location.getZ());
    }
}