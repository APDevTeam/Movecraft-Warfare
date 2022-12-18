package net.countercraft.movecraft.warfare.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.warfare.features.assault.Assault;

public class AssaultWinEvent extends AssaultEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public AssaultWinEvent(@NotNull Assault assault) {
        super(assault);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
