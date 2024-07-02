package net.countercraft.movecraft.warfare.features.siege.events;

import net.countercraft.movecraft.warfare.features.siege.Siege;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fires when a Siege is cancelled.
 */
public class SiegeCancelEvent extends SiegeEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SiegeCancelEvent(@NotNull Siege siege) {
        super(siege);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
