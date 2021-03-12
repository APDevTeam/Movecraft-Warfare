package net.countercraft.movecraft.warfare.siege;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.logging.Level;
import java.util.*;

public class SiegePaymentTask extends SiegeTask {

    public SiegePaymentTask(Siege siege) {
        super(siege);
    }

    @Override
    public void run() {
        // and now process payments every morning at 1:01 AM, as long as it has
        // been 23 hours after the last payout
        long secsElapsed = (System.currentTimeMillis() - siege.getLastPayout()) / 1000;

        if (secsElapsed > 23 * 60 * 60) {
            Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            int minute = rightNow.get(Calendar.MINUTE);
            if ((hour == 1) && (minute == 1)) {
                payRegion(siege.getCaptureRegion());
            }
        }
    }

    private void payRegion(String regionName) {
        HashSet<OfflinePlayer> owners = new HashSet<>();

        Set<UUID> ownerSet = null;
        for(World w : MovecraftWarfare.getInstance().getServer().getWorlds()) {
            ownerSet = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
            if (ownerSet != null)
                break;
        }
        if(ownerSet == null)
            return;

        for(UUID uuid : ownerSet) {
            owners.add(MovecraftWarfare.getInstance().getServer().getOfflinePlayer(uuid));
        }

        int share = siege.getDailyIncome() / owners.size();

        for (OfflinePlayer player : owners) {
            MovecraftRepair.getInstance().getEconomy().depositPlayer(player, share);
            MovecraftWarfare.getInstance().getLogger().log(Level.INFO, String.format(I18nSupport.getInternationalisedString("Siege - Ownership Payout Console"), player.getName(), share, siege.getName()));
        }
        siege.setLastPayout(System.currentTimeMillis());
    }
}
