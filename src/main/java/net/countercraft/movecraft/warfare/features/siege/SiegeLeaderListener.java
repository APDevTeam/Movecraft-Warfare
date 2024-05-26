package net.countercraft.movecraft.warfare.features.siege;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPreTranslateEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.util.ChatUtils;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import net.countercraft.movecraft.worldguard.utils.IsInRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public class SiegeLeaderListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftMovement(CraftPreTranslateEvent event) {
        Craft leaderCraft = event.getCraft();
        if (!(leaderCraft instanceof PlayerCraft) || !Config.SiegeEnable) return;
        Player player = ((PlayerCraft)leaderCraft).getPilot();
        Siege currentSiege = getSiegeByLeader(player);
        if (currentSiege == null) return;

        // Get the minimum and maximum positions
        MovecraftLocation newMinLocation = new MovecraftLocation(
                leaderCraft.getHitBox().getMinX(),
                leaderCraft.getHitBox().getMinY(),
                leaderCraft.getHitBox().getMinZ()
                ).translate(event.getDx(), event.getDy(),event.getDz());
        MovecraftLocation newMaxLocation = new MovecraftLocation(
                leaderCraft.getHitBox().getMaxX(),
                leaderCraft.getHitBox().getMaxY(),
                leaderCraft.getHitBox().getMaxZ()
                ).translate(event.getDx(),event.getDy(),event.getDz());

        event.setCancelled(processCraftMovement(leaderCraft,player,currentSiege,newMaxLocation,newMinLocation));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftRotation(CraftRotateEvent event) {
        Craft leaderCraft = event.getCraft();
        if (!(leaderCraft instanceof PlayerCraft) || !Config.SiegeEnable) return;
        Player player = ((PlayerCraft)leaderCraft).getPilot();
        Siege currentSiege = getSiegeByLeader(player);
        if (currentSiege == null) return;
        MovecraftLocation newMinLocation = new MovecraftLocation(
                event.getNewHitBox().getMinX(),
                event.getNewHitBox().getMinY(),
                event.getNewHitBox().getMinZ()
        );
        MovecraftLocation newMaxLocation = new MovecraftLocation(
                event.getNewHitBox().getMaxX(),
                event.getNewHitBox().getMaxY(),
                event.getNewHitBox().getMaxZ()
        );
        event.setCancelled(processCraftMovement(leaderCraft,player,currentSiege,newMaxLocation,newMinLocation));
    }
    @EventHandler
    public void onCraftSink (CraftSinkEvent event) {
        processCraftRemoval(event.getCraft());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftRelease (CraftReleaseEvent event) {
        processCraftRemoval(event.getCraft());
    }

    private void processCraftRemoval(Craft leaderCraft) {
        if (!(leaderCraft instanceof PlayerCraft) || !Config.SiegeEnable) return;
        Player player = ((PlayerCraft)leaderCraft).getPilot();
        Siege currentSiege = getSiegeByLeader(player);
        if (currentSiege == null) return;
        if (currentSiege.leaderIsInControl()) {
            String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Lost Control"),
                    player.getDisplayName(), currentSiege.getName());
            Bukkit.getServer().broadcastMessage(broadcast);
            SiegeBroadcastEvent event = new SiegeBroadcastEvent(currentSiege, broadcast, SiegeBroadcastEvent.Type.LOSE_CONTROL);
            Bukkit.getServer().getPluginManager().callEvent(event);
            currentSiege.setLeaderInControl(false);
            if (currentSiege.isSuddenDeathActive())
                endSiege(currentSiege);
        }
    }

    private boolean processCraftMovement(Craft leaderCraft, Player player, Siege currentSiege, MovecraftLocation newMaxLocation, MovecraftLocation newMinLocation) {
        IsInRegion regionTester = MovecraftWorldGuard.getInstance().getWGUtils().getIsInRegion(currentSiege.getConfig().getAttackRegion(), leaderCraft.getWorld());
        if (regionTester == null) return false;

        // Check to see that both corners are in the siege box
        if (!regionTester.test(newMaxLocation) || !regionTester.test(newMinLocation)) {
            if (currentSiege.leaderIsInControl() && Config.SiegeNoRetreat) {
                player.sendMessage(ChatUtils.MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - No Retreat"));
                return true;
            }
        } else if (!currentSiege.leaderIsInControl()) {
            String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Gained Control"),
                    player.getDisplayName(), currentSiege.getName(), leaderCraft.getType().getStringProperty(CraftType.NAME), leaderCraft.getOrigBlockCount(), leaderCraft.getHitBox().getMidPoint().toString());
            Bukkit.getServer().broadcastMessage(broadcast);
            SiegeBroadcastEvent event = new SiegeBroadcastEvent(currentSiege, broadcast, SiegeBroadcastEvent.Type.GAIN_CONTROL);
            Bukkit.getServer().getPluginManager().callEvent(event);
            currentSiege.setLeaderInControl(true);
        }
        return false;
    }

    private void endSiege(Siege siege) {
        String playerName = SiegeUtils.getSiegeLeaderName(siege.getPlayer());

        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege Failure"),
                siege.getName(), playerName);
        Bukkit.getServer().broadcastMessage(broadcast);

        siege.setStage(Siege.Stage.INACTIVE);

        playerName = siege.getPlayer().getName();
        if (playerName == null)
            playerName = "null";
        for (String command : siege.getConfig().getCommandsOnLose()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command
                    .replaceAll("%r", siege.getConfig().getCaptureRegion())
                    .replaceAll("%c", "" + siege.getConfig().getCost())
                    .replaceAll("%l", playerName));
        }

        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.LOSE);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @Nullable
    private Siege getSiegeByLeader (@NotNull Player leader) {
        for (Siege s : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            if (s.getStage().get().equals(Siege.Stage.INACTIVE)) continue;
            if (s.getPlayer() == null) continue;
            if (s.getPlayer().equals(leader)) {
                return s;
            } else {
                return null;
            }
        }
        return null;
    }
}
