package net.countercraft.movecraft.warfare.commands;

import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.utils.WarfareRepair;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.countercraft.movecraft.utils.ChatUtils.ERROR_PREFIX;
import static net.countercraft.movecraft.utils.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class AssaultRepairCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!command.getName().equalsIgnoreCase("assaultrepair"))
            return false;

        if (!Config.AssaultEnable) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - Disabled"));
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - No Region Specified"));
            return true;
        }
        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("AssaultInfo - Must Be Player"));
            return true;
        }

        Player player = (Player) commandSender;
        if (!player.hasPermission("movecraft.assault.adminrepair")) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        if(MovecraftWorldGuard.getInstance().getWGUtils().regionExists(args[0], player.getWorld())) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - Region Not Found"));
            return true;
        }

        if (!WarfareRepair.getInstance().repairRegionRepairState(player.getWorld(), args[0], player))
            Bukkit.getServer().broadcastMessage(ERROR_PREFIX + String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), args[0].toUpperCase()));

        return true;
    }
}
