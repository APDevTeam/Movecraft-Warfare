package net.countercraft.movecraft.warfare.sign;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.utils.WarfareRepair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class RegionDamagedSign implements Listener {
    public static final String HEADER = ChatColor.RED + "REGION DAMAGED!";

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK))
            return;
        if (event.getClickedBlock().getType() != Material.WALL_SIGN)
            return;
        Sign sign = (Sign) event.getClickedBlock().getState();
        if (!sign.getLine(0).equals(HEADER))
            return;

        event.setCancelled(true);

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        String regionName = sign.getLine(1).substring(sign.getLine(1).indexOf(":") + 1);
        long damages = Long.parseLong(sign.getLine(2).substring(sign.getLine(2).indexOf(":") + 1));
        String[] owners = sign.getLine(3).substring(sign.getLine(3).indexOf(":") + 1).split(",");
        if (!MovecraftRepair.getInstance().getEconomy().has(event.getPlayer(), damages)) {
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Economy - Not Enough Money"));
            return;
        }

        if (!WarfareRepair.getInstance().repairRegionRepairState(event.getClickedBlock().getWorld(), regionName, event.getPlayer())) {
            event.getPlayer().sendMessage(String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), regionName));
            return;
        }
        event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Assault - Repairing Region"));
        MovecraftRepair.getInstance().getEconomy().withdrawPlayer(event.getPlayer(), damages);
        World world = event.getClickedBlock().getWorld();
        ProtectedRegion aRegion = Movecraft.getInstance().getWorldGuardPlugin().getRegionManager(world).getRegion(regionName);
        DefaultDomain wgOwners = aRegion.getOwners();
        if(wgOwners == null) {
            Bukkit.getServer().broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Owners Failed"), regionName));
        }
        else {
            for (String ownerName : owners) {
                if (ownerName.length() > 16) {
                    wgOwners.addPlayer(UUID.fromString(ownerName));
                } else {
                    if (Bukkit.getPlayer(ownerName) != null) { //Cannot add names directly as bug will allow free assaults
                        wgOwners.addPlayer(Bukkit.getPlayer(ownerName).getUniqueId());
                    } else {
                        wgOwners.addPlayer(Bukkit.getOfflinePlayer(ownerName).getUniqueId());
                    }
                }
            }
        }
        //Clear the beacon
        int minX = sign.getX() - 2;
        int minY = sign.getY() - 3;
        int minZ = sign.getZ() - 1;
        int maxX = sign.getX() + 2;
        int maxY = sign.getY();
        int maxZ = sign.getZ() + 3;
        for (int x = minX ; x <= maxX ; x++){
            for (int y = minY ; y <= maxY ; y++){
                for (int z = minZ; z <= maxZ ; z++){
                    Block b = sign.getWorld().getBlockAt(x,y,z);
                    if (b.getType() == Material.BEDROCK || b.getType() == Material.BEACON || b.getType() == Material.IRON_BLOCK){
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }
}