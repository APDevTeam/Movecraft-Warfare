package net.countercraft.movecraft.warfare.features.siege.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.warfare.features.siege.Siege;

/**
 * Fires when somebody loses a siege.
 */
public class SiegeLoseEvent extends SiegeEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SiegeLoseEvent(@NotNull Siege siege) {
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
