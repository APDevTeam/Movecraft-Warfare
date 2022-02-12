package net.countercraft.movecraft.warfare.features.assault;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.utils.WarfareRepair;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

public class RegionDamagedSign implements Listener {
    public static final String HEADER = ChatColor.RED + "REGION DAMAGED!";

    @EventHandler
    public void onSignClick(@NotNull PlayerInteractEvent e) {
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            e.setCancelled(true);
            return;
        }
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;

        Block block = e.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign))
            return;
        Sign sign = (Sign) block.getState();
        if (!sign.getLine(0).equals(HEADER))
            return;
        e.setCancelled(true);

        String regionName = sign.getLine(1).substring(sign.getLine(1).indexOf(":") + 1);
        long damages = Long.parseLong(sign.getLine(2).substring(sign.getLine(2).indexOf(":") + 1));
        Player player = e.getPlayer();
        if (!MovecraftRepair.getInstance().getEconomy().has(player, damages)) {
            player.sendMessage(I18nSupport.getInternationalisedString("Economy - Not Enough Money"));
            return;
        }

        // Queue up the region repair
        if (!WarfareRepair.getInstance().repairRegionRepairState(e.getClickedBlock().getWorld(), regionName, player)) {
            player.sendMessage(String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), regionName));
            return;
        }
        player.sendMessage(I18nSupport.getInternationalisedString("Assault - Repairing Region"));
        MovecraftRepair.getInstance().getEconomy().withdrawPlayer(player, damages);

        // Re-add the owners
        String[] owners = sign.getLine(3).substring(sign.getLine(3).indexOf(":") + 1).split(",");
        Set<String> ownerSet = new HashSet<>(Arrays.asList(owners));
        if(!MovecraftWorldGuard.getInstance().getWGUtils().addOwners(regionName, sign.getWorld(), ownerSet)) {
            String broadcast = String.format(I18nSupport.getInternationalisedString("Assault - Owners Failed"), regionName);
            Bukkit.getServer().broadcastMessage(broadcast);

            // Note: there is no assault to pass here...
            AssaultBroadcastEvent broadcastEvent = new AssaultBroadcastEvent(null, broadcast, AssaultBroadcastEvent.Type.OWNER_FAIL);
            Bukkit.getServer().getPluginManager().callEvent(broadcastEvent);
        }

        // Clear the beacon
        int minX = sign.getX() - 2;
        int minY = sign.getY() - 3;
        int minZ = sign.getZ() - 1;
        int maxX = sign.getX() + 2;
        int maxY = sign.getY();
        int maxZ = sign.getZ() + 3;
        for (int x = minX ; x <= maxX ; x++) {
            for (int y = minY ; y <= maxY ; y++) {
                for (int z = minZ; z <= maxZ ; z++) {
                    Block b = sign.getWorld().getBlockAt(x,y,z);
                    if (b.getType() == Material.BEDROCK || b.getType() == Material.BEACON
                            || b.getType() == Material.IRON_BLOCK) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }
}
