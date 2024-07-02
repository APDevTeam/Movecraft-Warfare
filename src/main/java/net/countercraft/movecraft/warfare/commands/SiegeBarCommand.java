package net.countercraft.movecraft.warfare.commands;

import net.countercraft.movecraft.warfare.bar.config.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.localisation.I18nSupport;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class SiegeBarCommand implements CommandExecutor {
    @NotNull
    private final PlayerManager manager;

    public SiegeBarCommand(@NotNull PlayerManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Only Players may use this command");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("movecraft.warfare.siegebar")) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + net.countercraft.movecraft.localisation.I18nSupport
                    .getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        manager.toggleSiegeBarSetting(player);
        player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - Bar Set") + ": " + manager.getSiegeBarSetting(player));
        return true;
    }
}
