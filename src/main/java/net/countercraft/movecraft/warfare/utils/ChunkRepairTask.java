package net.countercraft.movecraft.warfare.utils;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Queue;
import java.util.function.Predicate;

public class ChunkRepairTask extends BukkitRunnable {
    private final Assault a;
    private final Queue<Chunk> chunks;
    private final File saveDirectory;
    private final Predicate<MovecraftLocation> regionTester;

    public ChunkRepairTask(@NotNull Assault a, Queue<Chunk> chunks, File saveDirectory, Predicate<MovecraftLocation> regionTester) {
        this.a = a;
        this.chunks = chunks;
        this.saveDirectory = saveDirectory;
        this.regionTester = regionTester;
    }

    @Override
    public void run() {
        if(a.getRepairedCorrectly().get() != Assault.RepairedState.UNREPAIRED)
            return;

        long start = System.currentTimeMillis();

        while(System.currentTimeMillis() - start < Config.AssaultChunkRepairTime) {
            Chunk c = chunks.poll();
            if(c == null) {
                if(chunks.size() == 0) {
                    MovecraftWarfare.getInstance().getLogger().info("Finished");
                    a.getSavedCorrectly().set(Assault.RepairedState.REPAIRED);
                    this.cancel();
                }
                else {
                    MovecraftWarfare.getInstance().getLogger().info("Failed");
                    a.getSavedCorrectly().set(Assault.RepairedState.FAILED);
                    Bukkit.getServer().broadcastMessage(String.format(I18nSupport.getInternationalisedString("Assault - Repair Failed"), a.getRegionName()));
                    this.cancel();
                }
                return;
            }

            MovecraftWarfare.getInstance().getLogger().info("Repairing: " + c);
            if (!MovecraftRepair.getInstance().getWEUtils().repairChunk(c, saveDirectory, regionTester)) {
                MovecraftWarfare.getInstance().getLogger().info("Failed on: " + c);
                a.getRepairedCorrectly().set(Assault.RepairedState.FAILED);
                return;
            }
        }
        MovecraftWarfare.getInstance().getLogger().info("Continuing");
    }
}
