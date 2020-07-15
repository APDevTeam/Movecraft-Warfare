package net.countercraft.movecraft.warfare.config;

import java.util.HashSet;

public class Config {
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
    public static HashSet<Integer> AssaultDestroyableBlocks;
    public static int AssaultDamagesPerBlock;

    public static boolean SiegeEnable;
}
