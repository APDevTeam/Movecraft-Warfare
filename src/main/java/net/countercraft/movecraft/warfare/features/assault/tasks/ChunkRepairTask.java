package net.countercraft.movecraft.warfare.features.assault.tasks;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.AssaultRepair;
import net.countercraft.movecraft.warfare.features.assault.events.AssaultBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Queue;
import java.util.function.Predicate;

public class ChunkRepairTask extends BukkitRunnable {
    private final String regionName;
    private final Queue<Chunk> chunks;
    private final File saveDirectory;
    private final Predicate<MovecraftLocation> regionTester;
    private final Player player;

    public ChunkRepairTask(String regionName, Queue<Chunk> chunks, File saveDirectory,
            Predicate<MovecraftLocation> regionTester, @Nullable Player player) {
        this.regionName = regionName;
        this.chunks = chunks;
        this.saveDirectory = saveDirectory;
        this.regionTester = regionTester;
        this.player = player;
    }

    @Override
    public void run() {
        for (int i = 0; i < Config.AssaultChunkRepairPerTick; i++) {
            Chunk c = chunks.poll();
            if (c == null) {
                if (!chunks.isEmpty()) {
                    if (player == null) {
                        String broadcast = String.format(
                                I18nSupport.getInternationalisedString("Assault - Repair Finished"), regionName);
                        Bukkit.broadcastMessage(broadcast);

                        // Note: there is no assault to pass here...
                        AssaultBroadcastEvent event = new AssaultBroadcastEvent(null, broadcast,
                                AssaultBroadcastEvent.Type.REPAIR_FINISHED);
                        Bukkit.getServer().getPluginManager().callEvent(event);
                    } else
                        player.sendMessage(String.format(
                                I18nSupport.getInternationalisedString("Assault - Repair Finished"), regionName));
                    cancel();
                } else {
                    String broadcast = String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"),
                            regionName);
                    Bukkit.getServer().broadcastMessage(broadcast);

                    // Note: there is no assault to pass here...
                    AssaultBroadcastEvent event = new AssaultBroadcastEvent(null, broadcast,
                            AssaultBroadcastEvent.Type.REPAIR_FAIL);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    cancel();
                }
                return;
            }

            if (!MovecraftWarfare.getInstance().getAssaultManager().getRepairUtils().getWarfareUtils().repairChunk(c,
                    saveDirectory, regionTester)) {
                cancel();
                return;
            }
        }
    }
}
