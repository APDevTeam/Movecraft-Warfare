package net.countercraft.movecraft.warfare.features.assault;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class AssaultUtils {
    public static boolean areDefendersOnline(String regionName, World w) {
        int numOnline = 0;

        numOnline += ownersOnline(regionName, w);
        if (Config.AssaultRequiredOwnersOnline > 0 && numOnline < Config.AssaultRequiredOwnersOnline) {
            return false;
        }

        numOnline += membersOnline(regionName, w);
        return numOnline >= Config.AssaultRequiredDefendersOnline;
    }

    private static int ownersOnline(String regionName, World w) {
        int numOnline = 0;
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if (owners == null)
            return 0;

        for (UUID playerID : owners) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    private static int membersOnline(String regionName, World w) {
        int numOnline = 0;
        Set<UUID> members = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDMembers(regionName, w);
        if (members == null)
            return 0;

        for (UUID playerID : members) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    public static double getCostToAssault(String regionName, World w) {
        return getAssaultBalance(regionName, w) * Config.AssaultCostPercent;
    }

    public static double getMaxDamages(String regionName, World w) {
        return getAssaultBalance(regionName, w) * Config.AssaultDamagesCapPercent;
    }

    private static double getAssaultBalance(String regionName, World w) {
        return getOwnerBalance(regionName, w) + getMemberBalance(regionName, w);
    }

    private static double getOwnerBalance(String regionName, World w) {
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if (owners == null)
            return 0.0;

        double total = 0.0;
        for (UUID playerID : owners) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP),
                        Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultOwnerWeightPercent / 100.0);
    }

    private static double getMemberBalance(String regionName, World w) {
        Set<UUID> members = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDMembers(regionName, w);
        if (members == null)
            return 0.0;

        double total = 0.0;
        for (UUID playerID : members) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP),
                        Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultMemberWeightPercent / 100.0);
    }

    public static boolean ownsRegions(Player p) {
        return MovecraftWorldGuard.getInstance().getWGUtils().ownsAssaultableRegion(p);
    }

    public static boolean isMember(String regionName, World w, Player p) {
        return MovecraftWorldGuard.getInstance().getWGUtils().isMember(regionName, w, p);
    }

    public static boolean canAssault(String regionName, World w) {
        if (!MovecraftWorldGuard.getInstance().getWGUtils().regionExists(regionName, w))
            return false;

        // a region can only be assaulted if it disables TNT, this is to prevent child
        // regions or sub regions from being assaulted
        if (!MovecraftWorldGuard.getInstance().getWGUtils().isTNTDenied(regionName, w))
            return false;

        // regions with no owners can not be assaulted
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if (owners == null || owners.size() == 0)
            return false;

        // TODO: We need to use a file to store the last assault

        if (!Config.SiegeEnable)
            return true;

        for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            // siegable regions can not be assaulted
            if (regionName.equalsIgnoreCase(siege.getConfig().getAttackRegion())
                    || regionName.equalsIgnoreCase(siege.getConfig().getCaptureRegion()))
                return false;
        }
        return true;
    }
}