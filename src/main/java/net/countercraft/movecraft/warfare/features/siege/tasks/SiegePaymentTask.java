package net.countercraft.movecraft.warfare.features.siege.tasks;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

public class SiegePaymentTask extends SiegeTask {
    private static final WeakHashMap<Siege, LocalDateTime> lastUpdates = new WeakHashMap<>();

    public SiegePaymentTask(Siege siege) {
        super(siege);
    }

    @Override
    public void run() {
        // If we have a last update record, check that it's from more than a day ago, else return
        if (lastUpdates.containsKey(siege)) {
            LocalDateTime lastUpdate = lastUpdates.get(siege);
            if (!LocalDateTime.now().minusDays(1).isAfter(lastUpdate))
                return;
        }

        // Build up a UUID set of owners of the capture region
        Set<UUID> ownerSet = null;
        for(World w : MovecraftWarfare.getInstance().getServer().getWorlds()) {
            ownerSet = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(
                siege.getConfig().getCaptureRegion(), w);
            if (ownerSet != null)
                break;
        }
        if(ownerSet == null)
            return;

        // Build up a set of offline players from the UUID set
        Set<OfflinePlayer> owners = new HashSet<>();
        for(UUID uuid : ownerSet) {
            owners.add(MovecraftWarfare.getInstance().getServer().getOfflinePlayer(uuid));
        }

        // Calculate the share of income to be paid out, and pay out the owners
        double share = (double) siege.getConfig().getDailyIncome() / owners.size();
        for (OfflinePlayer player : owners) {
            MovecraftRepair.getInstance().getEconomy().depositPlayer(player, share);
            MovecraftWarfare.getInstance().getLogger().info(String.format(
                I18nSupport.getInternationalisedString("Siege - Ownership Payout Console"),
                player.getName(),
                share,
                siege.getConfig().getName()
            ));
        }

        // Record the last update time
        lastUpdates.put(siege, LocalDateTime.now());
    }
}
