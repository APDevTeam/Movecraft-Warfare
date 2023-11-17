package net.countercraft.movecraft.warfare.features.siege;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.features.Warfare;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class Siege extends Warfare {
    public enum Stage {
        IN_PROGRESS, PREPARATION, INACTIVE
    }

    @NotNull private final String name;
    @NotNull private final SiegeConfig config;
    @NotNull private final AtomicReference<Stage> stage;
    @Nullable private LocalDateTime startTime;
    @Nullable private OfflinePlayer player;

    public Siege(@NotNull String siegeName, @NotNull SiegeConfig siegeConfig) {
        name = siegeName;
        config = siegeConfig;
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

    public void start(@NotNull Player player, long cost) {
        for (String startCommand : config.getCommandsOnStart()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    startCommand.replaceAll("%r", config.getAttackRegion()).replaceAll("%c", "" + cost));
        }
        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege About To Begin"),
                player.getDisplayName(), name) + SiegeUtils.formatMinutes(config.getDelayBeforeStart());
        Bukkit.getServer().broadcastMessage(broadcast);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 0.25F);
        }

        SiegeBroadcastEvent event = new SiegeBroadcastEvent(this, broadcast, SiegeBroadcastEvent.Type.PRESTART);
        Bukkit.getServer().getPluginManager().callEvent(event);

        MovecraftWarfare.getInstance().getLogger().info(String.format(
                I18nSupport.getInternationalisedString("Siege - Log Siege Start"),
                name, player.getName(), cost));
        MovecraftRepair.getInstance().getEconomy().withdrawPlayer(player, cost);
        startTime = LocalDateTime.now();
        this.player = player;
        setStage(Stage.PREPARATION);
    }

    public String toString() {
        return name;
    }
}
