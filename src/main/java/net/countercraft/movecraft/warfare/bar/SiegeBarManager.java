package net.countercraft.movecraft.warfare.bar;

import net.countercraft.movecraft.warfare.bar.config.PlayerManager;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.warfare.features.siege.events.*;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SiegeBarManager extends BukkitRunnable implements Listener {
    private final Map<Siege, BossBar> bossBars = new HashMap<>();
    private final PlayerManager manager;

    public SiegeBarManager(PlayerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Map.Entry<Siege, BossBar> entry : bossBars.entrySet()) {
            Siege siege = entry.getKey();
            BossBar bossBar = entry.getValue();
            long elapsed;
            switch (siege.getStage().get()) {
                case PREPARATION:
                    elapsed = Duration.between(siege.getStartTime(), LocalDateTime.now()).toMillis();
                    bossBar.setProgress(Math.min(elapsed / (siege.getConfig().getDelayBeforeStart() * 1000.0), 1.0));
                    bossBar.setTitle(siege.getName() + ": " + String.format("%,d", Math.round(((siege.getConfig().getDelayBeforeStart() * 1000.0) - elapsed) / 1000.0)));
                    break;
                case IN_PROGRESS:
                    elapsed = (Duration.between(siege.getStartTime(), LocalDateTime.now()).toMillis() - (siege.getConfig().getDelayBeforeStart() * 1000L));
                    bossBar.setProgress(elapsed / ((siege.getConfig().getDuration() - siege.getConfig().getSuddenDeathDuration()) * 1000.0));
                    bossBar.setTitle(siege.getName() + ": " + String.format("%,d", Math.round((((siege.getConfig().getDuration() - siege.getConfig().getSuddenDeathDuration()) * 1000.0) - elapsed) / 1000.0)));
                    break;
                case SUDDEN_DEATH:
                    elapsed = (Duration.between(siege.getStartTime(), LocalDateTime.now()).toMillis() - ((siege.getConfig().getDelayBeforeStart() + siege.getConfig().getDuration() - siege.getConfig().getSuddenDeathDuration()) * 1000L));
                    bossBar.setProgress(elapsed / 1000.0 / siege.getConfig().getSuddenDeathDuration());
                    bossBar.setTitle(siege.getName() + ": " + String.format("%,d", Math.round(((siege.getConfig().getSuddenDeathDuration() * 1000.0) - elapsed) / 1000.0)));
                    break;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!manager.getSiegeBarSetting(player)) {
                    bossBar.removePlayer(player);
                }
                else {
                    bossBar.addPlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void onSiegePreStart(@NotNull SiegePreStartEvent e) {
        bossBars.put(e.getSiege(), Bukkit.createBossBar(e.getSiege().getName(), BarColor.GREEN, BarStyle.SOLID));
    }

    @EventHandler
    public void onSiegeStart(@NotNull SiegeStartEvent e) {
        bossBars.get(e.getSiege()).setColor(BarColor.YELLOW);
    }

    @EventHandler
    public void onSiegeSuddenDeathStart(@NotNull SiegeSuddenDeathStartEvent e) {
        bossBars.get(e.getSiege()).setColor(BarColor.RED);
    }

    @EventHandler
    public void onSiegeWin(@NotNull SiegeWinEvent e) {
        remove(e.getSiege());
    }

    @EventHandler
    public void onSiegeLose(@NotNull SiegeLoseEvent e) {
        remove(e.getSiege());
    }

    @EventHandler
    public void onSiegeCancel(@NotNull SiegeCancelEvent e) {
        remove(e.getSiege());
    }

    private void remove(Siege siege) {
        BossBar bossBar = bossBars.remove(siege);
        bossBar.setVisible(false);
        bossBar.removeAll();
    }
}
