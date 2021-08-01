package net.countercraft.movecraft.warfare.siege;

import net.countercraft.movecraft.warfare.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.events.SiegeStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class SiegePreparationTask extends SiegeTask {


    public SiegePreparationTask(Siege siege) {
        super(siege);
    }

    @Override
    public void run() {
        int timePassed = ((int)(System.currentTimeMillis() - siege.getStartTime())); //time passed in milliseconds
        int timePassedInSeconds = timePassed / 1000;
        if (timePassedInSeconds >= siege.getDelayBeforeStart()){
            SiegeStartEvent siegeStartEvent = new SiegeStartEvent(siege);
            Bukkit.getPluginManager().callEvent(siegeStartEvent);

            if (siegeStartEvent.isCancelled()) {
                Bukkit.getPlayer(siege.getPlayerUUID()).sendMessage(MOVECRAFT_COMMAND_PREFIX + siegeStartEvent.getCancelReason());
                siege.setStage(SiegeStage.INACTIVE);
                return;
            }

            siege.setJustCommenced(true);
            siege.setStage(SiegeStage.IN_PROGRESS);
        }
        if ((siege.getDelayBeforeStart() - timePassedInSeconds) % Config.SiegeTaskSeconds != 0 || timePassed < 3000){
             return;
        }
        int timeLeft = siege.getDelayBeforeStart() - timePassedInSeconds;
        broadcastSiegePreparation(Bukkit.getPlayer(siege.getPlayerUUID()), siege.getName(), timeLeft);
    }

    private void broadcastSiegePreparation(@Nullable Player player, String siegeName, int timeLeft) {
        String playerName = "";
        if (player != null){
            playerName = player.getDisplayName();
        }

        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege About To Begin"),
                playerName, siegeName)+ SiegeUtils.formatMinutes(timeLeft);
        Bukkit.getServer().broadcastMessage(broadcast);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0);
        }
        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.PREPARATION);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
