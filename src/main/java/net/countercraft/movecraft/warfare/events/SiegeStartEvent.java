package net.countercraft.movecraft.warfare.events;

import org.jetbrains.annotations.NotNull;
import org.bukkit.event.HandlerList;
import net.countercraft.movecraft.warfare.siege.Siege;

public class SiegeStartEvent extends SiegeEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public SiegeStartEvent(@NotNull Siege siege) {
        super(siege);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
