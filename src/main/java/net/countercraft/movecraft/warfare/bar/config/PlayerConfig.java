package net.countercraft.movecraft.warfare.bar.config;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerConfig {
    @Nullable
    private UUID owner;
    private boolean assaultBarSetting = true;
    private boolean siegeBarSetting = true;

    public PlayerConfig() {
    }

    public PlayerConfig(UUID owner) {
        this.owner = owner;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public boolean getAssaultBarSetting() {
        return assaultBarSetting;
    }

    public void toggleAssaultBarSetting() {
        assaultBarSetting = !assaultBarSetting;
    }

    public boolean getSiegeBarSetting() {
        return siegeBarSetting;
    }

    public void toggleSiegeBarSetting() {
        siegeBarSetting = !siegeBarSetting;
    }
}
