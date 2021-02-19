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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AssaultUtils {
    public static boolean areDefendersOnline(ProtectedRegion region) {
        int numOnline = 0;

        numOnline += ownersOnline(region);
        if(Config.AssaultRequiredOwnersOnline > 0 && numOnline < Config.AssaultRequiredOwnersOnline) {
            return false;
        }

        numOnline += membersOnline(region);
        return numOnline >= Config.AssaultRequiredDefendersOnline;
    }

    private static int ownersOnline(@NotNull ProtectedRegion region) {
        int numOnline = 0;
        Set<UUID> owners = region.getOwners().getUniqueIds();
        for(UUID playerID : owners) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    private static int membersOnline(@NotNull ProtectedRegion region) {
        int numOnline = 0;
        Set<UUID> members = region.getMembers().getUniqueIds();
        for(UUID playerID : members) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    public static double getCostToAssault(ProtectedRegion tRegion) {
        return getAssaultBalance(tRegion) * Config.AssaultCostPercent;
    }

    public static double getMaxDamages(ProtectedRegion tRegion) {
        return getAssaultBalance(tRegion) * Config.AssaultDamagesCapPercent;
    }

    private static double getAssaultBalance(ProtectedRegion tRegion) {
        return getOwnerBalance(tRegion) + getMemberBalance(tRegion);
    }

    private static double getOwnerBalance(@NotNull ProtectedRegion tRegion) {
        HashSet<UUID> players = new HashSet<>(tRegion.getOwners().getUniqueIds());
        double total = 0.0;
        for (UUID playerID : players) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP), Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultOwnerWeightPercent / 100.0);
    }

    private static double getMemberBalance(@NotNull ProtectedRegion tRegion) {
        HashSet<UUID> players = new HashSet<>(tRegion.getMembers().getUniqueIds());
        double total = 0.0;
        for (UUID playerID : players) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP), Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultMemberWeightPercent / 100.0);
    }

    public static boolean ownsRegions(Player p) {
        LocalPlayer lp = Movecraft.getInstance().getWorldGuardPlugin().wrapPlayer(p);
        Map<String, ProtectedRegion> allRegions = Movecraft.getInstance().getWorldGuardPlugin().getRegionManager(p.getWorld()).getRegions();
        for (ProtectedRegion iRegion : allRegions.values()) {
            if (iRegion.isOwner(lp) && iRegion.getFlag(DefaultFlag.TNT) == StateFlag.State.DENY) {
                return true;
            }
        }
        return false;
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