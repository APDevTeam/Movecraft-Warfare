package net.countercraft.movecraft.warfare.assault;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.events.AssaultStartEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static net.countercraft.movecraft.utils.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class AssaultBeginTask extends BukkitRunnable {
    private final Player player;
    private final Assault assault;

    public AssaultBeginTask(Player player, Assault assault) {
        this.player = player;
        this.assault = assault;
    }

    @Override
    public void run() {
        if(assault.getSavedCorrectly().get() != Assault.SavedState.SAVED) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Repair - Could not save file"));
            return;
        }

        AssaultStartEvent assaultStartEvent = new AssaultStartEvent(assault);
        Bukkit.getPluginManager().callEvent(assaultStartEvent);

        if (assaultStartEvent.isCancelled()) {
            if(player.isOnline())
                player.sendMessage(MOVECRAFT_COMMAND_PREFIX + assaultStartEvent.getCancelReason());

            return;
        }

        Bukkit.getServer().broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Assault Begun")
                , assault.getRegionName(), player.getDisplayName()));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0.25F);
        }

        MovecraftWarfare.getInstance().getAssaultManager().getAssaults().add(assault);
        assault.getRegion().setFlag(DefaultFlag.TNT, StateFlag.State.ALLOW);
    }
}
