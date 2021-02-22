package net.countercraft.movecraft.warfare.events;

import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.HandlerList;
import net.countercraft.movecraft.warfare.siege.Siege;

/**
 * Fires when a Siege starts.
 */
public class SiegeStartEvent extends SiegeEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private String cancelReason;

    public SiegeStartEvent(@NotNull Siege siege) {
        super(siege);
        cancelled = false;
        cancelReason = I18nSupport.getInternationalisedString("Siege - Default Siege Start Cancel Reason");
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean newState, @NotNull String cancelReason) {
        setCancelled(newState);
        this.cancelReason = cancelReason;
    }

    @Override
    public void setCancelled(boolean newState) {
        cancelled = newState;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
