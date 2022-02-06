package net.countercraft.movecraft.warfare.features.siege;

import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;

public class Siege {
    public enum Stage {
        IN_PROGRESS, PREPERATION, INACTIVE
    }

    @NotNull private final SiegeConfig config;
    @NotNull private final AtomicReference<Stage> stage;

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

    public String toString() {
        return config.getName();
    }
}
