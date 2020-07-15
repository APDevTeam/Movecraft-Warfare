package net.countercraft.movecraft.warfare.events;

import org.jetbrains.annotations.NotNull;
import org.bukkit.event.Event;
import net.countercraft.movecraft.warfare.siege.Siege;

public abstract class SiegeEvent extends Event {
    @NotNull protected final Siege siege;

    public SiegeEvent(@NotNull Siege siege) {
        this.siege = siege;
    }

    @NotNull
    public final Siege getSiege() {
        return siege;
    }
}