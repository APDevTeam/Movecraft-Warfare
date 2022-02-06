package net.countercraft.movecraft.warfare.features.siege;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Siege {
    public enum Stage {
        IN_PROGRESS, PREPERATION, INACTIVE
    }

    @NotNull private final SiegeConfig config;
    @NotNull private final AtomicReference<Stage> stage;
    @Nullable private LocalDateTime startTime;
    @Nullable private OfflinePlayer player;

    public Siege(@NotNull SiegeConfig config) {
        this.config = config;
        stage = new AtomicReference<>();
        stage.set(Stage.INACTIVE);
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

    public String toString() {
        return config.getName();
    }
}
