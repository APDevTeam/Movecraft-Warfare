package net.countercraft.movecraft.warfare.events;

import net.countercraft.movecraft.warfare.siege.Siege;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
