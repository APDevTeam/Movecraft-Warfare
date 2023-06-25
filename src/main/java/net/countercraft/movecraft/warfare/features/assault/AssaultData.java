package net.countercraft.movecraft.warfare.features.assault;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class AssaultData {
    @Nullable
    private Set<UUID> owners = null;
    @Nullable
    private LocalDateTime startTime = null;

    public AssaultData() {

    }

    public AssaultData(Set<UUID> owners, LocalDateTime starTime) {
        this.owners = owners;
        this.startTime = starTime;
    }
}
