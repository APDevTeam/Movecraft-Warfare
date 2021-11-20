package net.countercraft.movecraft.warfare.siege;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.warfare.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.events.SiegeLoseEvent;
import net.countercraft.movecraft.warfare.events.SiegeWinEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SiegeProgressTask extends SiegeTask {

    public SiegeProgressTask(Siege siege) {
        super(siege);
    }

    //every 20 ticks = 1 second
    public void run() {
        int timeLeft = (int) (siege.getDuration() - ((System.currentTimeMillis() - siege.getStartTime()) / 1000));
        if (!siege.isJustCommenced() && timeLeft % Config.SiegeTaskSeconds != 0) {
            return;
        }
        siege.setJustCommenced(false);
        @Nullable Player siegeLeader = Movecraft.getInstance().getServer().getPlayer(siege.getPlayerUUID());
        @Nullable Craft siegeCraft = CraftManager.getInstance().getCraftByPlayer(siegeLeader);

        if(timeLeft > 10) {
            if(leaderPilotingShip(siegeCraft) && MovecraftWorldGuard.getInstance().getWGUtils().craftFullyInRegion(siege.getAttackRegion(), siegeLeader.getWorld(), siegeCraft)) {
                MovecraftLocation mid = siegeCraft.getHitBox().getMidPoint();
                String broadcast = String.format(
                        I18nSupport.getInternationalisedString("Siege - Flagship In Box"),
                        siege.getName(),
                        siegeCraft.getType().getStringProperty(CraftType.NAME),
                        siegeCraft.getOrigBlockCount(),
                        siegeLeader.getDisplayName(), mid.getX(), mid.getY(), mid.getZ())
                        + SiegeUtils.formatMinutes(timeLeft);
                Bukkit.getServer().broadcastMessage(broadcast);

                SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.PROGRESS_IN_BOX);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
            else {
                String broadcast = String.format(
                        I18nSupport.getInternationalisedString("Siege - Flagship Not In Box"),
                        siege.getName(), getSiegeLeaderName(siegeLeader))
                        + SiegeUtils.formatMinutes(timeLeft);
                Bukkit.getServer().broadcastMessage(broadcast);

                SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.PROGRESS_NOT_IN_BOX);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        }
        else {
            for (Player p : Bukkit.getOnlinePlayers()){
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1,0);
            }
            endSiege(siegeCraft, siegeLeader);
        }
    }

    private void endSiege(@Nullable Craft siegeCraft, @Nullable Player siegeLeader) {
        if (leaderPilotingShip(siegeCraft) && siegeLeader != null) {
            if(MovecraftWorldGuard.getInstance().getWGUtils().craftFullyInRegion(siege.getAttackRegion(), siegeCraft.getW(), siegeCraft)) {
                String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege Success"),
                        siege.getName(), siegeLeader.getDisplayName());
                Bukkit.getServer().broadcastMessage(broadcast);

                winSiege(siegeLeader);

                SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.WIN);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
            else {
                failSiege(siegeLeader);
            }
        }
        else {
            failSiege(siegeLeader);
        }
        siege.setStage(Siege.Stage.INACTIVE);
    }

    private void winSiege(@NotNull Player siegeLeader) {
        Bukkit.getPluginManager().callEvent(new SiegeWinEvent(siege));
        MovecraftWorldGuard.getInstance().getWGUtils().clearAndSetOwnership(siege.getCaptureRegion(), siegeLeader.getWorld(), siege.getPlayerUUID());
        processCommands(siegeLeader.getName(), true);
    }

    private void failSiege(@Nullable Player siegeLeader) {
        Bukkit.getPluginManager().callEvent(new SiegeLoseEvent(siege));
        String name = getSiegeLeaderName(siegeLeader);
        String broadcast = String.format(I18nSupport.getInternationalisedString("Siege - Siege Failure"),
                siege.getName(), name);
        Bukkit.getServer().broadcastMessage(broadcast);

        processCommands(name, false);

        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.LOSE);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    private void processCommands(@NotNull String siegeLeader, boolean win) {
        List<String> commands = win ? siege.getCommandsOnWin() : siege.getCommandsOnLose();
        for (String command : commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command
                    .replaceAll("%r", siege.getCaptureRegion())
                    .replaceAll("%c", "" + siege.getCost())
                    .replaceAll("%l", siegeLeader));
        }
    }

    private boolean leaderPilotingShip(@Nullable Craft siegeCraft) {
        if (siegeCraft == null)
            return false;
        else
            return siege.getCraftsToWin().contains(siegeCraft.getType().getStringProperty(CraftType.NAME));
    }

    @NotNull
    private String getSiegeLeaderName(@Nullable Player siegeLeader) {
        String name;
        if(siegeLeader == null) {
            name = Bukkit.getOfflinePlayer(siege.getPlayerUUID()).getName();

            if(name == null)
                name = "null"; // Note, there's not a better way to deal with this sadly
        }
        else
            name = siegeLeader.getDisplayName();

        return name;
    }
}
