package net.countercraft.movecraft.warfare.utils;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.config.Config;
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
    private final ProtectedRegion region;
    private final Queue<Chunk> chunks;
    private final File saveDirectory;
    private final Predicate<MovecraftLocation> regionTester;
    private final Player player;


    public ChunkRepairTask(ProtectedRegion region, Queue<Chunk> chunks, File saveDirectory, Predicate<MovecraftLocation> regionTester, @Nullable Player player) {
        this.region = region;
        this.chunks = chunks;
        this.saveDirectory = saveDirectory;
        this.regionTester = regionTester;
        this.player = player;
    }

    @Override
    public void run() {
        for(int i = 0; i < Config.AssaultChunkRepairPerTick; i++) {
            Chunk c = chunks.poll();
            if(c == null) {
                if(chunks.size() == 0) {
                    if(player == null)
                        Bukkit.broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Repair Finished"), region.getId()));
                    else
                        player.sendMessage(String.format(I18nSupport.getInternationalisedString("Assault - Repair Finished"), region.getId()));
                    this.cancel();
                }
                else {
                    Bukkit.getServer().broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), region.getId()));
                    this.cancel();
                }
                return;
            }

            if (!MovecraftRepair.getInstance().getWEUtils().repairChunk(c, saveDirectory, regionTester)) {
                this.cancel();
                return;
            }
        }
    }
}
