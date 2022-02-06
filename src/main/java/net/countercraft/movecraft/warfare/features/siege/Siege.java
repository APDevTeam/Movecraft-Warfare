package net.countercraft.movecraft.warfare.features.siege;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.warfare.features.Warfare;

public class Siege extends Warfare {
    public enum Stage {
        IN_PROGRESS, PREPERATION, INACTIVE
    }

    @NotNull private String name;
    @NotNull private final SiegeConfig config;
    @NotNull private final AtomicReference<Stage> stage;
    @Nullable private LocalDateTime startTime;
    @Nullable private OfflinePlayer player;

    public Siege(@NotNull Pair<String, SiegeConfig> nameAndConfig) {
        name = nameAndConfig.getLeft();
        config = nameAndConfig.getRight();
        stage = new AtomicReference<>();
        stage.set(Stage.INACTIVE);
        startTime = null;
        player = null;
    }

    @NotNull
    public SiegeConfig getConfig() {
        return config;
    }

    @NotNull
    public AtomicReference<Stage> getStage() {
        return stage;
    }

    public void setStage(@NotNull Stage stage) {
        this.stage.set(stage);
    }

    @Nullable
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Nullable
    public OfflinePlayer getPlayer() {
        return player;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
