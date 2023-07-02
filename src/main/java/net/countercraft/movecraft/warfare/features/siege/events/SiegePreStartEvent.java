package net.countercraft.movecraft.warfare.features.siege.events;

import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.features.siege.Siege;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fires after Siege validation, before the Siege Preparation stage.
 */
public class SiegePreStartEvent extends SiegeEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player siegeLeader;
    private boolean cancelled;
    private String cancelReason;

    public SiegePreStartEvent(@NotNull Siege siege, @NotNull Player siegeLeader) {
        super(siege);
        this.siegeLeader = siegeLeader;
        cancelled = false;
        cancelReason = I18nSupport.getInternationalisedString("Event - Default Cancel Reason");
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

    public Player getSiegeLeader() {
        return siegeLeader;
    }
}
