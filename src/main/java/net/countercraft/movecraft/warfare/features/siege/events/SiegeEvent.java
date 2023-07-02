package net.countercraft.movecraft.warfare.features.siege.events;

import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.warfare.features.siege.Siege;

import org.bukkit.event.Event;

public abstract class SiegeEvent extends Event {
    @NotNull private final Siege siege;

    public SiegeEvent(@NotNull Siege siege) {
        this.siege = siege;
    }

    @NotNull
    public Siege getSiege() {
        return siege;
    }
}