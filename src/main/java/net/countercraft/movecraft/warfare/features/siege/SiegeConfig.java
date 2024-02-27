package net.countercraft.movecraft.warfare.features.siege;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import net.countercraft.movecraft.util.Pair;

public class SiegeConfig {
    @NotNull private final String attackRegion;
    @NotNull private final String captureRegion;
    @NotNull private final List<Integer> daysOfWeek;
    @NotNull private final List<String> craftsToWin, commandsOnStart, commandsOnWin, commandsOnLose;
    private final int scheduleStart, scheduleEnd, delayBeforeStart, duration, suddenDeathDuration, dailyIncome, cost;
    private final boolean doubleCostPerOwnedSiegeRegion;

    public static Pair<String, SiegeConfig> load(Map.Entry<String, Map<String, ?>> config) {
        Map<String,Object> siegeMap = (Map<String, Object>) config.getValue();
        return new Pair<>(config.getKey(), new SiegeConfig(
            (String) siegeMap.get("SiegeRegion"),
            (String) siegeMap.get("RegionToControl"),
            (List<Integer>) siegeMap.get("DaysOfTheWeek"),
            (List<String>) siegeMap.get("CraftsToWin"),
            (List<String>) siegeMap.get("CommandsOnStart"),
            (List<String>) siegeMap.get("CommandsOnWin"),
            (List<String>) siegeMap.get("CommandsOnLose"),
            (int) siegeMap.get("ScheduleStart"),
            (int) siegeMap.get("ScheduleEnd"),
            (int) siegeMap.get("DelayBeforeStart"),
            (int) siegeMap.get("SiegeDuration"),
            (int) siegeMap.get("DailyIncome"),
            (int) siegeMap.get("CostToSiege"),
            (int) siegeMap.getOrDefault("SuddenDeathDuration", 0),
            (boolean) siegeMap.get("DoubleCostPerOwnedSiegeRegion")
        ));
    }

    private SiegeConfig(
            @NotNull String attackRegion, @NotNull String captureRegion,
            @NotNull List<Integer> daysOfWeek,
            @NotNull List<String> craftsToWin, @NotNull List<String> commandsOnStart,
            @NotNull List<String> commandsOnWin, @NotNull List<String> commandsOnLose,
            int scheduleStart, int scheduleEnd, int delayBeforeStart, int duration, int dailyIncome, int cost, int suddenDeathDuration,
            boolean doubleCostPerOwnedSiegeRegion) {
        this.attackRegion = attackRegion;
        this.captureRegion = captureRegion;
        this.daysOfWeek = daysOfWeek;
        this.craftsToWin = craftsToWin;
        this.commandsOnStart = commandsOnStart == null ? new ArrayList<>() : commandsOnStart;
        this.commandsOnWin = commandsOnWin == null ? new ArrayList<>() : commandsOnWin;
        this.commandsOnLose = commandsOnLose == null ? new ArrayList<>() : commandsOnLose;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.delayBeforeStart = delayBeforeStart;
        this.duration = duration;
        this.dailyIncome = dailyIncome;
        this.cost = cost;
        this.suddenDeathDuration = suddenDeathDuration;
        this.doubleCostPerOwnedSiegeRegion = doubleCostPerOwnedSiegeRegion;
    }

    @NotNull
    public String getCaptureRegion() {
        return captureRegion;
    }

    @NotNull
    public String getAttackRegion() {
        return attackRegion;
    }

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

    public int getSuddenDeathDuration() {return suddenDeathDuration;}

    public boolean isDoubleCostPerOwnedSiegeRegion() {
        return doubleCostPerOwnedSiegeRegion;
    }
}