package net.countercraft.movecraft.warfare.features.siege.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.warfare.features.siege.Siege;

/**
 * Fires when somebody wins a siege.
 */
public class SiegeWinEvent extends SiegeEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SiegeWinEvent(@NotNull Siege siege) {
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
