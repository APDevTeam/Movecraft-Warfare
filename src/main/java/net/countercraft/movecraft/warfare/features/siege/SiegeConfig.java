package net.countercraft.movecraft.warfare.features.siege;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SiegeConfig {
    @NotNull private final String attackRegion;
    @NotNull private final String captureRegion;
    @NotNull private final List<Integer> daysOfWeek;
    @NotNull private final List<String> craftsToWin, commandsOnStart, commandsOnWin, commandsOnLose;
    private final int scheduleStart, scheduleEnd, delayBeforeStart, duration, dailyIncome, cost, suddenDeathDuration;
    private final boolean doubleCostPerOwnedSiegeRegion;

    public SiegeConfig(ConfigurationSection config) {
        if(!config.isString("SiegeRegion"))
            throw new IllegalArgumentException("Invalid SiegeRegion.");
        attackRegion = config.getString("SiegeRegion");

        if(!config.isString("RegionToControl"))
            throw new IllegalArgumentException("Invalid RegionToControl.");
        captureRegion = config.getString("RegionToControl");

        if(!config.contains("DaysOfTheWeek"))
            throw new IllegalArgumentException("Invalid DaysOfTheWeek.");
        daysOfWeek = config.getIntegerList("DaysOfTheWeek");

        if(!config.contains("CraftsToWin"))
            throw new IllegalArgumentException("Invalid CraftsToWin.");
        craftsToWin = config.getStringList("CraftsToWin");

        commandsOnStart = config.getStringList("CommandsOnStart");
        commandsOnWin = config.getStringList("CommandsOnWin");
        commandsOnLose = config.getStringList("CommandsOnLose");

        if(!config.isInt("ScheduleStart"))
            throw new IllegalArgumentException("Invalid ScheduleStart");
        scheduleStart = config.getInt("ScheduleStart");

        if(!config.isInt("ScheduleEnd"))
            throw new IllegalArgumentException("Invalid ScheduleEnd");
        scheduleEnd = config.getInt("ScheduleEnd");

        if(!config.isInt("DelayBeforeStart"))
            throw new IllegalArgumentException("Invalid DelayBeforeStart");
        delayBeforeStart = config.getInt("DelayBeforeStart");

        if(!config.isInt("SiegeDuration"))
            throw new IllegalArgumentException("Invalid SiegeDuration");
        duration = config.getInt("SiegeDuration");

        if(!config.isInt("DailyIncome"))
            throw new IllegalArgumentException("Invalid DailyIncome");
        dailyIncome = config.getInt("DailyIncome");

        if(!config.isInt("CostToSiege"))
            throw new IllegalArgumentException("Invalid CostToSiege");
        cost = config.getInt("CostToSiege");

        if(!config.isBoolean("DoubleCostPerOwnedSiegeRegion"))
            throw new IllegalArgumentException("Invalid DoubleCostPerOwnedSiegeRegion");
        doubleCostPerOwnedSiegeRegion = config.getBoolean("DoubleCostPerOwnedSiegeRegion");

        // Since this is a new addition to the siege file and optional,
        // get it if present but ignore it if not
        if (config.contains("SiegeSuddenDeathDuration")) {
            if(!config.isInt("SiegeSuddenDeathDuration"))
                throw new IllegalArgumentException("Invalid SiegeSuddenDeathDuration");
            suddenDeathDuration = config.getInt("SiegeSuddenDeathDuration");
        } else {
            suddenDeathDuration = 0;
        }
    }

    @NotNull
    public String getCaptureRegion() {
        return captureRegion;
    }

    @NotNull
    public String getAttackRegion() {
        return attackRegion;
    }

    @NotNull
    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    @NotNull
    public List<String> getCraftsToWin() {
        return craftsToWin;
    }

    @NotNull
    public List<String> getCommandsOnStart() {
        return commandsOnStart;
    }

    @NotNull
    public List<String> getCommandsOnWin() {
        return commandsOnWin;
    }

    @NotNull
    public List<String> getCommandsOnLose() {
        return commandsOnLose;
    }

    public int getScheduleStart() {
        return scheduleStart;
    }

    public int getScheduleEnd() {
        return scheduleEnd;
    }

    public int getDelayBeforeStart() {
        return delayBeforeStart;
    }

    public int getDuration() {
        return duration;
    }

    public int getDailyIncome() {
        return dailyIncome;
    }

    public int getCost() {
        return cost;
    }

    public int getSuddenDeathDuration() {
        return suddenDeathDuration;
    }

    public boolean isDoubleCostPerOwnedSiegeRegion() {
        return doubleCostPerOwnedSiegeRegion;
    }
}