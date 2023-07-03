package net.countercraft.movecraft.warfare.features.assault;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;

public class AssaultData {
    @Nullable
    private UUID starter = null;
    @Nullable
    private Long maxDamages = null;
    @Nullable
    private Long damages = null;
    @Nullable
    private LocalDateTime startTime = null;
    @Nullable
    private Set<UUID> owners = null;

    public AssaultData() {

    }

    public AssaultData(Assault assault) {
        starter = assault.getStarterUUID();
        maxDamages = assault.getMaxDamages();
        damages = assault.getDamages();
        startTime = assault.getStartTime();
        owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(assault.getRegionName(),
                assault.getWorld());
    }

    public UUID getStarter() {
        return starter;
    }

    public Long getMaxDamages() {
        return maxDamages;
    }

    public Long getDamages() {
        return damages;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Set<UUID> getOwners() {
        return owners;
    }
}
