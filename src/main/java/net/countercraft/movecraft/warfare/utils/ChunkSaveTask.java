package net.countercraft.movecraft.warfare.utils;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.config.Config;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Queue;

public class ChunkSaveTask extends BukkitRunnable {
    private final Assault a;
    private final Queue<Chunk> chunks;
    private final File saveDirectory;

    public ChunkSaveTask(Assault a, Queue<Chunk> chunks, File saveDirectory) {
        this.a = a;
        this.chunks = chunks;
        this.saveDirectory = saveDirectory;
    }

    @Override
    public void run() {
        if(a.getSavedCorrectly().get() != Assault.SavedState.UNSAVED)
            return;

        long start = System.currentTimeMillis();

        while(System.currentTimeMillis() - start < Config.AssaultChunkSaveTime) {
            Chunk c = chunks.poll();
            if(c == null) {
                if(chunks.size() == 0) {
                    MovecraftWarfare.getInstance().getLogger().info("Finished");
                    a.getSavedCorrectly().set(Assault.SavedState.SAVED);
                    this.cancel();
                }
                else {
                    MovecraftWarfare.getInstance().getLogger().info("Failed");
                    a.getSavedCorrectly().set(Assault.SavedState.FAILED);
                }
                return;
            }

            MovecraftWarfare.getInstance().getLogger().info("Saving: " + c);
            if(!MovecraftRepair.getInstance().getWEUtils().saveChunk(c, saveDirectory, Config.AssaultDestroyableBlocks)) {
                MovecraftWarfare.getInstance().getLogger().info("Failed on: " + c);
                a.getSavedCorrectly().set(Assault.SavedState.FAILED);
                return;
            }
        }
        MovecraftWarfare.getInstance().getLogger().info("Continuing");
    }
}
