package net.countercraft.movecraft.warfare.features.siege.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.warfare.features.siege.SiegeUtils;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeStartEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class SiegePreparationTask extends SiegeTask {
    public SiegePreparationTask(Siege siege) {
        super(siege);
    }

    @Override
    public void run() {
        // Check if the preparation is over
        if(siege.getStartTime() == null)
            throw new IllegalStateException();
        Duration timePassed = Duration.between(siege.getStartTime(), LocalDateTime.now());
        long timeLeft = siege.getConfig().getDelayBeforeStart() - timePassed.getSeconds();
        if (timeLeft <= 0) {
            // Fire off the start event
            SiegeStartEvent siegeStartEvent = new SiegeStartEvent(siege);
            Bukkit.getPluginManager().callEvent(siegeStartEvent);

            if (siegeStartEvent.isCancelled()) {
                // If the event is cancelled, notify the player if they are online
                Player player = siege.getPlayer().getPlayer();
                if(player != null) {
                    player.sendMessage(MOVECRAFT_COMMAND_PREFIX + siegeStartEvent.getCancelReason());
                }
                // Set the siege to inactive
                siege.setStage(Siege.Stage.INACTIVE);
                return;
            }

            // Else set the siege to in progress
            siege.setStage(Siege.Stage.IN_PROGRESS);
        }
        else if (timeLeft % Config.SiegeTaskSeconds != 0) {
            return; // Only send broadcasts every SiegeTaskSeconds seconds
        }

        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege About To Begin"),
                SiegeUtils.getSiegeLeaderName(siege.getPlayer()), siege.getName())
            + SiegeUtils.formatMinutes(timeLeft);
        Bukkit.getServer().broadcastMessage(broadcast);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0);
        }
        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.PREPARATION);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
