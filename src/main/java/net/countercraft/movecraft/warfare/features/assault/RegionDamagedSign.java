package net.countercraft.movecraft.warfare.features.assault;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

public class RegionDamagedSign implements Listener {
    public static final String HEADER = ChatColor.RED + "REGION DAMAGED!";

    @EventHandler
    public void onSignClick(@NotNull PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.LEFT_CLICK_BLOCK))
            return;

        Block block = e.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign))
            return;
        Sign sign = (Sign) block.getState();
        if (!sign.getLine(0).equals(HEADER))
            return;
        e.setCancelled(true);
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK))
            return;

        String regionName = sign.getLine(1).substring(sign.getLine(1).indexOf(":") + 1);
        long damages = Long.parseLong(sign.getLine(2).substring(sign.getLine(2).indexOf(":") + 1));
        Player player = e.getPlayer();
        if (!MovecraftRepair.getInstance().getEconomy().has(player, damages)) {
            player.sendMessage(I18nSupport.getInternationalisedString("Economy - Not Enough Money"));
            return;
        }

        // Queue up the region repair
        if (!MovecraftWarfare.getInstance().getAssaultManager().getRepairUtils()
                .repairRegionRepairState(e.getClickedBlock().getWorld(), regionName, player)) {
            player.sendMessage(
                    String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), regionName));
            return;
        }
        player.sendMessage(I18nSupport.getInternationalisedString("Assault - Repairing Region"));
        MovecraftRepair.getInstance().getEconomy().withdrawPlayer(player, damages);

        // Re-add the owners
        List<AssaultData> data = AssaultUtils.retrieveInfoFile(regionName, sign.getWorld().getName());
        if (data == null || data.size() == 0) {
            player.sendMessage(
                    String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), regionName));
            return;
        }
        Set<UUID> ownerSet = data.get(0).getOwners();
        if (ownerSet != null && !ownerSet.isEmpty()) {
            if (!MovecraftWorldGuard.getInstance().getWGUtils().addOwners(regionName, sign.getWorld(), ownerSet)) {
                String broadcast = String.format(I18nSupport.getInternationalisedString("Assault - Owners Failed"),
                        regionName);
                Bukkit.getServer().broadcastMessage(broadcast);

                // Note: there is no assault to pass here...
                AssaultBroadcastEvent broadcastEvent = new AssaultBroadcastEvent(null, broadcast,
                        AssaultBroadcastEvent.Type.OWNER_FAIL);
                Bukkit.getServer().getPluginManager().callEvent(broadcastEvent);
            }
        }

        // Clear the beacon
        for (int x = sign.getX() - 2; x <= sign.getX() + 2; x++) {
            for (int y = sign.getY() - 3; y <= sign.getY(); y++) {
                for (int z = sign.getZ() - 1; z <= sign.getZ() + 3; z++) {
                    Block b = sign.getWorld().getBlockAt(x, y, z);
                    if (b.getType() == Material.BEDROCK || b.getType() == Material.BEACON
                            || b.getType() == Material.IRON_BLOCK || Tag.SIGNS.isTagged(b.getType())) {
                        Movecraft.getInstance().getWorldHandler().setBlockFast(b.getLocation(),
                                Material.AIR.createBlockData());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign))
            return;

        Sign s = (Sign) e.getBlock().getState();
        if (s.getLine(0).equals(HEADER))
            e.setCancelled(true);
    }
}
