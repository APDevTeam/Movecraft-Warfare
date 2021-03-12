package net.countercraft.movecraft.warfare.assault;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.siege.Siege;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class AssaultUtils {
    public static boolean areDefendersOnline(String regionName, World w) {
        int numOnline = 0;

        numOnline += ownersOnline(regionName, w);
        if(Config.AssaultRequiredOwnersOnline > 0 && numOnline < Config.AssaultRequiredOwnersOnline) {
            return false;
        }

        numOnline += membersOnline(regionName, w);
        return numOnline >= Config.AssaultRequiredDefendersOnline;
    }

    private static int ownersOnline(String regionName, World w) {
        int numOnline = 0;
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if(owners == null)
            return 0;

        for(UUID playerID : owners) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    private static int membersOnline(String regionName, World w) {
        int numOnline = 0;
        Set<UUID> members = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDMembers(regionName, w);
        if(members == null)
            return 0;

        for(UUID playerID : members) {
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
        if(owners == null)
            return 0.0;

        double total = 0.0;
        for (UUID playerID : owners) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP), Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultOwnerWeightPercent / 100.0);
    }

    private static double getMemberBalance(String regionName, World w) {
        Set<UUID> members = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDMembers(regionName, w);
        if(members == null)
            return 0.0;

        double total = 0.0;
        for (UUID playerID : members) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP), Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultMemberWeightPercent / 100.0);
    }

    public static boolean ownsRegions(Player p) {
        return MovecraftWorldGuard.getInstance().getWGUtils().ownsAssaultableRegion(p);
    }

    public static boolean isMember(Player p, @NotNull ProtectedRegion r) {
        LocalPlayer lp = Movecraft.getInstance().getWorldGuardPlugin().wrapPlayer(p);
        return r.isMember(lp) || r.isOwner(lp);
    }

    public static boolean canAssault(@NotNull ProtectedRegion region) {
        // a region can only be assaulted if it disables TNT, this is to prevent child regions or sub regions from being assaulted
        // regions with no owners can not be assaulted
        if (region.getFlag(DefaultFlag.TNT) != StateFlag.State.DENY || region.getOwners().size() == 0)
            return false;

        if(Config.SiegeEnable) {
            for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
                // siegable regions can not be assaulted
                if (region.getId().equalsIgnoreCase(siege.getAttackRegion()) || region.getId().equalsIgnoreCase(siege.getCaptureRegion())) {
                    return false;
                }
            }
        }

        // TODO: This is 100% broken, instead we need to use a file to store the last assault data.
        /*{
            Assault assault = null;
            for (Assault tempAssault : MovecraftWarfare.getInstance().getAssaultManager().getAssaults()) {
                if (tempAssault.getRegion().equals(region)) {
                    assault = tempAssault;
                    break;
                }
            }
            if (assault != null) {
                long startTime = assault.getStartTime();
                long curtime = System.currentTimeMillis();
                if (curtime - startTime < Config.AssaultCooldownHours * (60 * 60 * 1000)) {
                    return false;
                }
            }
        }*/
        return true;
    }

    @NotNull
    public static String getRegionOwnerList(ProtectedRegion tRegion) {
        StringBuilder output = new StringBuilder();
        if (tRegion == null)
            return "";
        boolean first = true;
        if (tRegion.getOwners().getUniqueIds().size() > 0) {
            for (UUID uid : tRegion.getOwners().getUniqueIds()) {
                if (!first)
                    output.append(",");
                else
                    first = false;
                OfflinePlayer offP = Bukkit.getOfflinePlayer(uid);
                if (offP.getName() == null)
                    output.append(uid.toString());
                else
                    output.append(offP.getName());
            }
        }
        if (tRegion.getOwners().getPlayers().size() > 0) {
            for (String player : tRegion.getOwners().getPlayers()) {
                if (!first)
                    output.append(",");
                else
                    first = false;
                output.append(player);
            }
        }
        return output.toString();
    }
}