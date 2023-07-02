package net.countercraft.movecraft.warfare.features.assault.commands;

import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;
import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class AssaultRepairCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!command.getName().equalsIgnoreCase("assaultrepair"))
            return false;

        if (!Config.AssaultEnable) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - Disabled"));
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - No Region Specified"));
            return true;
        }
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("AssaultInfo - Must Be Player"));
            return true;
        }

        Player player = (Player) commandSender;
        if (!player.hasPermission("movecraft.assault.adminrepair")) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        String regionName = args[0];
        if (MovecraftWorldGuard.getInstance().getWGUtils().regionExists(regionName, player.getWorld())) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - Region Not Found"));
            return true;
        }

        if (!MovecraftWarfare.getInstance().getAssaultManager().getRepairUtils()
                .repairRegionRepairState(player.getWorld(), regionName, player)) {
            String broadcast = ERROR_PREFIX + " " + String
                    .format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), regionName.toUpperCase());
            Bukkit.getServer().broadcastMessage(broadcast);

            // Note: there is no assault to pass here...
            AssaultBroadcastEvent event = new AssaultBroadcastEvent(null, broadcast,
                    AssaultBroadcastEvent.Type.REPAIR_FAIL);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }

        // TODO: Re-add owners

        return true;
    }
}
