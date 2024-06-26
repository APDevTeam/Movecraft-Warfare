package net.countercraft.movecraft.warfare.config;

import org.bukkit.Material;

import java.util.EnumSet;

public class Config {
    // Localisation
    public static String Locale = "en";

    // Assault
    public static boolean AssaultEnable;
    public static double AssaultDamagesCapPercent;
    public static int AssaultCooldownHours;
    public static int AssaultDelay;
    public static int AssaultDuration;
    public static int AssaultRequiredDefendersOnline;
    public static int AssaultRequiredOwnersOnline;
    public static double AssaultCostPercent;
    public static double AssaultMaxBalance;
    public static double AssaultOwnerWeightPercent;
    public static double AssaultMemberWeightPercent;
    public static EnumSet<Material> AssaultDestroyableBlocks;
    public static int AssaultDamagesPerBlock;
    public static int AssaultChunkSavePerTick;
    public static int AssaultChunkRepairPerTick;
    public static int AssaultChunkSavePeriod;
    public static int AssaultChunkRepairPeriod;

    // Siege
    public static boolean SiegeEnable;
    public static int SiegeTaskSeconds = 600;
    public static boolean SiegeNoRetreat;
}
