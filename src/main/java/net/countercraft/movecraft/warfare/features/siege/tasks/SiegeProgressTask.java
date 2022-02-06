package net.countercraft.movecraft.warfare.features.siege.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.warfare.features.siege.SiegeUtils;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeLoseEvent;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeWinEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

public class SiegeProgressTask extends SiegeTask {
    public SiegeProgressTask(Siege siege) {
        super(siege);
    }

    @Override
    public void run() {
        Duration timePassed = Duration.between(siege.getStartTime(), LocalDateTime.now());
        long timeLeft = siege.getConfig().getDuration() - timePassed.getSeconds();
        if (timeLeft % Config.SiegeTaskSeconds != 0)
            return;

        if (timeLeft < 0) {
            // Siege is done!
            endSiege();
        }
        else {
            // Siege is still in progress
        }
    }

    private void endSiege() {
        siege.getStage().set(Siege.Stage.INACTIVE);
        if (siege.getPlayer() == null) {
            throw new IllegalStateException();
        }
        if (siege.getPlayer().getPlayer() == null) {
            failSiege(null); // Player is offline or unavailable
            return;
        }
        Player player = siege.getPlayer().getPlayer();
        PlayerCraft craft = CraftManager.getInstance().getCraftByPlayer(player);
        if (craft == null) {
            failSiege(player); // Player is not piloting
            return;
        }
        if (!siege.getConfig().getCraftsToWin().contains(craft.getType().getStringProperty(CraftType.NAME))) {
            failSiege(player); // Player is not piloting the correct craft type
            return;
        }
        if (!MovecraftWorldGuard.getInstance().getWGUtils().craftFullyInRegion(
                siege.getConfig().getAttackRegion(),
                craft.getWorld(),
                craft)) {
            failSiege(player); // Player's craft is not in the attack region
            return;
        }

        winSiege(player);
    }

    private void failSiege(@Nullable Player player) {
        Bukkit.getPluginManager().callEvent(new SiegeLoseEvent(siege));
        String name;
        if (player != null)
            name = player.getDisplayName();
        else
            name = SiegeUtils.getSiegeLeaderName(siege.getPlayer());

        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege Failure"),
            siege.getName(), name);
        Bukkit.getServer().broadcastMessage(broadcast);
        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.LOSE);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (player != null)
            processCommands(player.getName(), siege.getConfig().getCommandsOnLose());
        else {
            name = siege.getPlayer().getName();
            if (name == null) {
                throw new IllegalArgumentException("Failed to run siege loss commands for "
                    + siege.getPlayer().getUniqueId());
            }
            processCommands(name, siege.getConfig().getCommandsOnLose());
        }
    }

    private void winSiege(@NotNull Player player) {
        Bukkit.getPluginManager().callEvent(new SiegeWinEvent(siege));

        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege Success"),
                        siege.getName(), player.getDisplayName());
        Bukkit.getServer().broadcastMessage(broadcast);
        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.WIN);
        Bukkit.getServer().getPluginManager().callEvent(event);

        MovecraftWorldGuard.getInstance().getWGUtils().clearAndSetOwnership(
            siege.getConfig().getCaptureRegion(),
            player.getWorld(),
            player.getUniqueId());

        processCommands(player.getName(), siege.getConfig().getCommandsOnWin());
    }

    private void processCommands(@NotNull String name, @NotNull List<String> commands) {
        for (String command : commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command
                    .replaceAll("%r", siege.getConfig().getCaptureRegion())
                    .replaceAll("%c", "" + siege.getConfig().getCost())
                    .replaceAll("%l", name));
        }
    }
}
