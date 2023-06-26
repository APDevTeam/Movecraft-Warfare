package net.countercraft.movecraft.warfare.features.assault.commands;

import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.Assault;
import net.countercraft.movecraft.warfare.features.assault.AssaultData;
import net.countercraft.movecraft.warfare.features.assault.AssaultUtils;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class AssaultInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, Command command, @NotNull String s,
            @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("assaultinfo")) {
            return false;
        }
        if (!Config.AssaultEnable) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - Disabled"));
            return true;
        }

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("AssaultInfo - Must Be Player"));
            return true;
        }
        Player player = (Player) commandSender;

        if (!player.hasPermission("movecraft.assaultinfo")) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        if (!MovecraftWorldGuard.getInstance().getWGUtils().isInRegion(player.getLocation())) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("AssaultInfo - No Region Found"));
            return true;
        }

        if (!AssaultUtils.ownsRegions(player)) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Assault - No Regions Owned"));
            return true;
        }

        HashSet<String> siegeRegions = new HashSet<>();
        for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            siegeRegions.add(siege.getConfig().getCaptureRegion().toUpperCase());
            siegeRegions.add(siege.getConfig().getAttackRegion().toUpperCase());
        }
        String assaultRegion = MovecraftWorldGuard.getInstance().getWGUtils().getAssaultableRegion(player.getLocation(),
                siegeRegions);
        if (assaultRegion == null) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("AssaultInfo - No Region Found"));
            return true;
        }

        boolean canBeAssaulted = true;
        List<String> lines = new ArrayList<>();
        String output = I18nSupport.getInternationalisedString("AssaultInfo - Name") + ": ";
        output += assaultRegion;
        lines.add(output);
        output = I18nSupport.getInternationalisedString("AssaultInfo - Owner") + ": ";
        output += MovecraftWorldGuard.getInstance().getWGUtils().getRegionOwnerList(assaultRegion, player.getWorld());
        lines.add(output);
        output = I18nSupport.getInternationalisedString("AssaultInfo - Cap") + ": ";
        double maxDamage = AssaultUtils.getMaxDamages(assaultRegion, player.getWorld());
        output += String.format("%.2f", maxDamage);
        lines.add(output);
        output = I18nSupport.getInternationalisedString("AssaultInfo - Cost") + ": ";
        double cost = AssaultUtils.getCostToAssault(assaultRegion, player.getWorld());
        output += String.format("%.2f", cost);
        lines.add(output);

        AssaultData data = AssaultUtils.retrieveInfoFile(assaultRegion);
        if (data != null) {
            LocalDateTime lastStartTime = data.getStartTime();
            if (lastStartTime != null) {
                // We have had a previous assault, check the time
                Duration delta = Duration.between(lastStartTime, LocalDateTime.now());
                if (delta.toHours() < Config.AssaultCooldownHours) {
                    canBeAssaulted = false;
                    lines.add("- " + I18nSupport.getInternationalisedString("AssaultInfo - Not Assaultable Damaged"));
                }
            }
        }
        if (!AssaultUtils.areDefendersOnline(assaultRegion, player.getWorld())) {
            canBeAssaulted = false;
            lines.add("- "
                    + I18nSupport.getInternationalisedString("AssaultInfo - Not Assaultable - Insufficient Defenders"));
        }
        if (AssaultUtils.isMember(assaultRegion, player.getWorld(), player)) {
            lines.add("- " + I18nSupport.getInternationalisedString("AssaultInfo - Not Assaultable - You Are Member"));
            canBeAssaulted = false;
        }
        if (canBeAssaulted) {
            lines.add("- " + I18nSupport.getInternationalisedString("AssaultInfo - Assaultable"));
        }
        player.sendMessage(lines.toArray(new String[1]));
        return true;
    }
}
