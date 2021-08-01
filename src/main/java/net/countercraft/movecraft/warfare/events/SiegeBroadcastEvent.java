package net.countercraft.movecraft.warfare.events;

import net.countercraft.movecraft.warfare.siege.Siege;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SiegeBroadcastEvent extends SiegeEvent {
    public enum Type {
        PRESTART,
        PREPARATION,
        PROGRESS_IN_BOX,
        PROGRESS_NOT_IN_BOX,
        WIN,
        LOSE
    }
    private static final HandlerList HANDLERS = new HandlerList();
    private final String broadcast;
    private final Type type;

    public SiegeBroadcastEvent(@NotNull Siege siege, @NotNull String broadcast, @NotNull Type type) {
        super(siege);
        this.broadcast = broadcast;
        this.type = type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public String getBroadcast() {
        return broadcast;
    }

    @NotNull
    public Type getType() {
        return type;
    }
}
