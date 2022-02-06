package net.countercraft.movecraft.warfare.features.siege;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.warfare.localisation.I18nSupport;

public class SiegeUtils {
    @NotNull
    public static String formatMinutes(long seconds) {
        if (seconds < 60) {
            return I18nSupport.getInternationalisedString("Siege - Ending Soon");
        }

        long minutes = seconds / 60;
        if (minutes == 1) {
            return I18nSupport.getInternationalisedString("Siege - Ending In 1 Minute");
        } else {
            return String.format(I18nSupport.getInternationalisedString("Siege - Ending In X Minutes"), minutes);
        }
    }

    @NotNull
    public static String getSiegeLeaderName(@NotNull OfflinePlayer player) {
        String name;
        if (player.getPlayer() != null)
            return player.getPlayer().getDisplayName(); // Player is online, display their display name
    
        name = player.getName(); // Player is offline, try their cached name
        if(name == null)
            return "null"; // Player has no cached name, use a null
        return name; // Player hs a cached name, use it
    }
}
