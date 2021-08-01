package net.countercraft.movecraft.warfare.events;

import net.countercraft.movecraft.warfare.assault.Assault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AssaultBroadcastEvent extends AssaultEvent {
    public enum Type {
        PRESTART,
        START,
        WIN,
        LOSE,
        OWNER_FAIL,
        BEACON_FAIL,
        REPAIR_FAIL,
        REPAIR_FINISHED
    }
    private static final HandlerList HANDLERS = new HandlerList();
    private final String broadcast;
    private final Type type;

    public AssaultBroadcastEvent(@NotNull Assault assault, @NotNull String broadcast, @NotNull Type type) {
        super(assault);
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
