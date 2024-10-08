package net.countercraft.movecraft.warfare.features.assault.tasks;

import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.Assault;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultBroadcastEvent;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultStartEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class AssaultBeginTask extends BukkitRunnable {
    private final Player player;
    private final Assault assault;
    private final long start;

    public AssaultBeginTask(Player player, Assault assault) {
        this.player = player;
        this.assault = assault;
        start = System.currentTimeMillis();
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - start < Config.AssaultDelay * 1000L)
            return;

        cancel();
        if (assault.getSavedCorrectly().get() != Assault.SavedState.SAVED) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Repair - Could not save file"));
            return;
        }

        AssaultStartEvent assaultStartEvent = new AssaultStartEvent(assault);
        Bukkit.getPluginManager().callEvent(assaultStartEvent);

        if (assaultStartEvent.isCancelled()) {
            if (player.isOnline())
                player.sendMessage(MOVECRAFT_COMMAND_PREFIX + assaultStartEvent.getCancelReason());

            return;
        }

        assault.getStage().set(Assault.Stage.IN_PROGRESS);

        String broadcast = String.format(I18nSupport.getInternationalisedString("Assault - Assault Begun"),
                assault.getRegionName(), player.getDisplayName());
        Bukkit.getServer().broadcastMessage(broadcast);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0.25F);
        }

        MovecraftWorldGuard.getInstance().getWGUtils().setTNTAllow(assault.getRegionName(), assault.getWorld());

        AssaultBroadcastEvent event = new AssaultBroadcastEvent(assault, broadcast, AssaultBroadcastEvent.Type.START);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
