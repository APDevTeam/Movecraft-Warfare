package net.countercraft.movecraft.warfare.assault;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.warfare.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
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

    private static int ownersOnline(ProtectedRegion region) {
        int numOnline = 0;
        Set<UUID> owners = region.getOwners().getUniqueIds();
        for(UUID playerID : owners) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    private static int membersOnline(ProtectedRegion region) {
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

    private static double getOwnerBalance(ProtectedRegion tRegion) {
        HashSet<UUID> players = new HashSet<>();
        players.addAll(tRegion.getOwners().getUniqueIds());
        double total = 0.0;
        for (UUID playerID : players) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                if (Movecraft.getInstance().getEconomy().getBalance(offP) > Config.AssaultMaxBalance)
                    total += Config.AssaultMaxBalance;
                else
                    total += Movecraft.getInstance().getEconomy().getBalance(offP);
        }
        return total * (Config.AssaultOwnerWeightPercent / 100.0);
    }

    private static double getMemberBalance(ProtectedRegion tRegion) {
        HashSet<UUID> players = new HashSet<>();
        players.addAll(tRegion.getMembers().getUniqueIds());
        double total = 0.0;
        for (UUID playerID : players) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                if (Movecraft.getInstance().getEconomy().getBalance(offP) > Config.AssaultMaxBalance)
                    total += Config.AssaultMaxBalance;
                else
                    total += Movecraft.getInstance().getEconomy().getBalance(offP);
        }
        return total * (Config.AssaultMemberWeightPercent / 100.0);
    }
}