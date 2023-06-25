package net.countercraft.movecraft.warfare.features.assault.tasks;

import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.assault.Assault;
import net.countercraft.movecraft.warfare.utils.WarfareRepair;

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
        if (a.getSavedCorrectly().get() != Assault.SavedState.UNSAVED)
            return;

        for (int i = 0; i < Config.AssaultChunkSavePerTick; i++) {
            Chunk c = chunks.poll();
            if (c == null) {
                if (!chunks.isEmpty()) {
                    a.getSavedCorrectly().set(Assault.SavedState.SAVED);
                    cancel();
                } else {
                    a.getSavedCorrectly().set(Assault.SavedState.FAILED);
                }
                return;
            }

            if (!WarfareRepair.getInstance().getWarfareUtils().saveChunk(c, saveDirectory,
                    Config.AssaultDestroyableBlocks)) {
                a.getSavedCorrectly().set(Assault.SavedState.FAILED);
                return;
            }
        }
    }
}
