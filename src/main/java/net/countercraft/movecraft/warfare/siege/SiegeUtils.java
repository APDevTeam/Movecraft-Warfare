package net.countercraft.movecraft.warfare.siege;

import net.countercraft.movecraft.warfare.localisation.I18nSupport;

public class SiegeUtils {
    public static String formatMinutes(int seconds) {
        if (seconds < 60) {
            return I18nSupport.getInternationalisedString("Siege - Ending Soon");
        }

        int minutes = seconds / 60;
        if (minutes == 1) {
            return I18nSupport.getInternationalisedString("Siege - Ending In 1 Minute");
        } else {
            return String.format(I18nSupport.getInternationalisedString("Siege - Ending In X Minutes"), minutes);
        }
    }
}
