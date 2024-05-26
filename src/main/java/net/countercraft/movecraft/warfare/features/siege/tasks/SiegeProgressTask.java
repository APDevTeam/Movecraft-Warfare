package net.countercraft.movecraft.warfare.features.siege.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
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

        if (timeLeft < 0) {
            // Siege is done!
            endSiege();
            return;
        }
        // Siege is still in progress

        // Check if it's time to begin sudden death
        if (!siege.isSuddenDeathActive() && timeLeft < siege.getConfig().getSuddenDeathDuration()) {
            String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Sudden Death"),
                    siege.getName(), (timeLeft+2)/60);
            Bukkit.getServer().broadcastMessage(broadcast);
            SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.SUDDEN_DEATH);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!siege.leaderIsInControl()) {
                endSiege();
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0.25F);
                }
                siege.setSuddenDeathActive(true);
            }
        }

        if (timeLeft % Config.SiegeTaskSeconds != 0)
            return;

        var broadcast = getBroadcast(timeLeft);
        Bukkit.getServer().broadcastMessage(broadcast.getLeft());
        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast.getLeft(), broadcast.getRight());
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    private Pair<String, SiegeBroadcastEvent.Type> getBroadcast(long timeLeft) {
        OfflinePlayer siegeLeader = siege.getPlayer();
        @Nullable
        Player player = siegeLeader.getPlayer();
        if (player == null) {
            // Player is offline, try Bukkit's name cache
            String name = siegeLeader.getName();
            if (name == null) {
                name = "null";
            }
            return notInBox(timeLeft, name);
        }

        @Nullable
        Craft siegeCraft = CraftManager.getInstance().getCraftByPlayer(player);
        if (siegeCraft == null) {
            // Not piloting a craft
            return notInBox(timeLeft, player.getDisplayName());
        }

        if (!siege.getConfig().getCraftsToWin().contains(siegeCraft.getType().getStringProperty(CraftType.NAME))) {
            // Wrong type of craft
            return notInBox(timeLeft, player.getDisplayName());
        }

        if (!MovecraftWorldGuard.getInstance().getWGUtils().craftFullyInRegion(siege.getConfig().getAttackRegion(),
                player.getWorld(), siegeCraft)) {
            // Not fully in region
            return notInBox(timeLeft, player.getDisplayName());
        }

        // Craft fully in region
        MovecraftLocation mid = siegeCraft.getHitBox().getMidPoint();
        String broadcast = String.format(
                I18nSupport.getInternationalisedString("Siege - Flagship In Box"),
                siege.getName(),
                siegeCraft.getType().getStringProperty(CraftType.NAME),
                siegeCraft.getOrigBlockCount(),
                player.getDisplayName(), mid.getX(), mid.getY(), mid.getZ()) + SiegeUtils.formatMinutes(timeLeft);
        return new Pair<String, SiegeBroadcastEvent.Type>(broadcast, SiegeBroadcastEvent.Type.PROGRESS_IN_BOX);
    }

    private Pair<String, SiegeBroadcastEvent.Type> notInBox(long timeLeft, String siegeLeaderName) {
        return new Pair<>(
                String.format(I18nSupport.getInternationalisedString("Siege - Flagship Not In Box"), siege.getName(),
                        siegeLeaderName) + SiegeUtils.formatMinutes(timeLeft),
                SiegeBroadcastEvent.Type.PROGRESS_NOT_IN_BOX);
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
