package net.countercraft.movecraft.warfare.listener;

import net.countercraft.movecraft.events.TypesReloadedEvent;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TypesReloadedListener implements Listener {
    @EventHandler
    public void typesReloadedListener(TypesReloadedEvent e) {
        MovecraftWarfare.getInstance().reloadTypes();
    }
}
