package net.countercraft.movecraft.warfare.bar;

import net.countercraft.movecraft.warfare.bar.config.PlayerManager;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.Assault;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultLoseEvent;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultPreStartEvent;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultStartEvent;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultWinEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public class AssaultBarManager extends BukkitRunnable implements Listener {
    private final Map<Assault, BossBar> bossBars = new HashMap<>();
    private final PlayerManager manager;

    public AssaultBarManager(PlayerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        for (Map.Entry<Assault, BossBar> entry : bossBars.entrySet()) {
            Assault assault = entry.getKey();
            BossBar bossBar = entry.getValue();
            switch (assault.getStage().get()) {
                case PREPARATION:
                    long elapsed = Duration.between(assault.getStartTime(), LocalDateTime.now()).toMillis();
                    bossBar.setProgress(Math.min(elapsed / (Config.AssaultDelay * 1000.0), 1.0));
                    bossBar.setTitle(assault.getRegionName() + ": " + Math.round(((Config.AssaultDelay * 1000.0) - elapsed) / 1000.0));
                    break;
                case IN_PROGRESS:
                    bossBar.setProgress(Math.min((double) assault.getDamages() / assault.getMaxDamages(), 1.0));
                    bossBar.setTitle(assault.getRegionName() + ": " + String.format("%,d", assault.getDamages()) + "/" + String.format("%,d", assault.getMaxDamages()));
                    break;
            }

            Location location = assault.getHitBox().getMidPoint().toBukkit(assault.getWorld());
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld() != assault.getWorld()) {
                    bossBar.removePlayer(player);
                    continue;
                }

                if (!manager.getAssaultBarSetting(player)) {
                    bossBar.removePlayer(player);
                    continue;
                }

                if (player.getLocation().distanceSquared(location) > 1000 * 1000) {
                    bossBar.removePlayer(player);
                    continue;
                }

                bossBar.addPlayer(player);
            }
        }
    }

    @EventHandler
    public void onAssaultPreStart(@NotNull AssaultPreStartEvent e) {
        bossBars.put(e.getAssault(), Bukkit.createBossBar(e.getAssault().getRegionName(), BarColor.YELLOW, BarStyle.SOLID));
    }

    @EventHandler
    public void onAssaultStart(@NotNull AssaultStartEvent e) {
        bossBars.get(e.getAssault()).setColor(BarColor.RED);
    }

    @EventHandler
    public void onAssaultWin(@NotNull AssaultWinEvent e) {
        remove(e.getAssault());
    }

    @EventHandler
    public void onAssaultLose(@NotNull AssaultLoseEvent e) {
        remove(e.getAssault());
    }

    private void remove(Assault assault) {
        BossBar bossBar = bossBars.remove(assault);
        bossBar.setVisible(false);
        bossBar.removeAll();
    }
}
