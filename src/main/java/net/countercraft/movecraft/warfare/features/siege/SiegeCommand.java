package net.countercraft.movecraft.warfare.features.siege;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.util.TopicPaginator;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.siege.events.SiegeBroadcastEvent;
import net.countercraft.movecraft.warfare.features.siege.events.SiegePreStartEvent;
import net.countercraft.movecraft.warfare.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class SiegeCommand implements TabExecutor {
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!command.getName().equalsIgnoreCase("siege"))
            return false;
        if (!Config.SiegeEnable || MovecraftWarfare.getInstance().getSiegeManager().getSieges().size() == 0) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - Siege Not Configured"));
            return true;
        }
        if (!commandSender.hasPermission("movecraft.siege")) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - No Argument"));
            return true;
        }

        if (args[0].equalsIgnoreCase("list"))
            return listCommand(commandSender, args);
        else if (args[0].equalsIgnoreCase("begin"))
            return beginCommand(commandSender);
        else if (args[0].equalsIgnoreCase("info"))
            return infoCommand(commandSender, args);
        else if (args[0].equalsIgnoreCase("time"))
            return timeCommand(commandSender);
        else if (args[0].equalsIgnoreCase("cancel"))
            return cancelCommand(commandSender);
        else if (args[0].equalsIgnoreCase("status"))
            return statusCommand(commandSender);

        commandSender.sendMessage(
                MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - Invalid Argument"));
        return true;
    }

    private boolean cancelCommand(CommandSender commandSender) {
        if (!commandSender.hasPermission("movecraft.siege.cancel")) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            if (siege.getStage().get() == Siege.Stage.INACTIVE)
                continue;

            if((commandSender instanceof Player)) {
                if (siege.getPlayer().equals((Player) commandSender) || commandSender.hasPermission("movecraft.siege.cancel.other")) {
                    cancelSiege(siege);
                } else {
                    commandSender.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                }
            } else {
                if (commandSender.hasPermission("movecraft.siege.cancel.other")) {
                    cancelSiege(siege);
                } else {
                    commandSender.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                }
            }
            return true;
        }
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - No Siege Active"));
        return false;
    }

    private void cancelSiege(Siege siege) {
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

        SiegeBroadcastEvent event = new SiegeBroadcastEvent(siege, broadcast, SiegeBroadcastEvent.Type.CANCEL);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    private boolean timeCommand(CommandSender commandSender) {
        if (!commandSender.hasPermission("movecraft.siege.time")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        int militaryTime = getMilitaryTime();
        commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + dayToString(getDayOfWeek()) + " - "
                + String.format("%02d", militaryTime / 100) + ":" + String.format("%02d", militaryTime % 100));
        return true;
    }

    private boolean infoCommand(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("movecraft.siege.info")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        if (args.length <= 1) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - Specify Region"));
            return true;
        }

        String siegeName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Siege siege = null;
        for (Siege searchSiege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            if (searchSiege.getName().equalsIgnoreCase(siegeName)) {
                siege = searchSiege;
                break;
            }
        }
        if (siege == null) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Siege - Siege Region Not Found"));
            return true;
        }

        commandSender.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "----- " + ChatColor.RESET + ChatColor.GOLD
                + siege.getName() + ChatColor.YELLOW + ChatColor.BOLD + " -----");
        ChatColor cost, start, end;

        if (commandSender instanceof Player) {
            cost = MovecraftRepair.getInstance().getEconomy().has((Player) commandSender, siege.getConfig().getCost())
                    ? ChatColor.GREEN
                    : ChatColor.RED;
        } else {
            cost = ChatColor.DARK_RED;
        }

        start = siege.getConfig().getScheduleStart() < getMilitaryTime() ? ChatColor.GREEN : ChatColor.RED;
        end = siege.getConfig().getScheduleEnd() > getMilitaryTime() ? ChatColor.GREEN : ChatColor.RED;

        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - Siege Cost") + cost
                + currencyFormat.format(siege.getConfig().getCost()));
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - Daily Income") + ChatColor.WHITE
                + currencyFormat.format(siege.getConfig().getDailyIncome()));
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - Day of Week")
                + daysOfWeekString(siege.getConfig().getDaysOfWeek()));
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - Start Time") + start
                + militaryTimeIntToString(siege.getConfig().getScheduleStart()) + " UTC");
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - End Time") + end
                + militaryTimeIntToString(siege.getConfig().getScheduleEnd()) + " UTC");
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - Duration") + ChatColor.WHITE
                + secondsIntToString(siege.getConfig().getDuration()));
        commandSender.sendMessage(I18nSupport.getInternationalisedString("Siege - Sudden Death Duration") + ChatColor.WHITE
                + secondsIntToString(siege.getConfig().getSuddenDeathDuration()));
        return true;
    }

    private String daysOfWeekString(@NotNull List<Integer> days) {
        String str = new String();
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i) == getDayOfWeek())
                str += ChatColor.GREEN;
            else
                str += ChatColor.RED;

            str += dayToString(days.get(i));

            if (i != days.size() - 1)
                str += ChatColor.WHITE + ", ";
        }
        return str;
    }

    @NotNull
    private String militaryTimeIntToString(int militaryTime) {
        return String.format("%02d", militaryTime / 100) + ":" + String.format("%02d", militaryTime % 100);
    }

    @NotNull
    private String secondsIntToString(int seconds) {
        return String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private boolean listCommand(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("movecraft.siege.list")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        SiegeManager siegeManager = MovecraftWarfare.getInstance().getSiegeManager();
        int page;
        try {
            if (args.length <= 1)
                page = 1;
            else
                page = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Paginator - Invalid Page") + " \"" + args[1] + "\"");
            return true;
        }

        TopicPaginator paginator = new TopicPaginator("Sieges");
        for (Siege siege : siegeManager.getSieges()) {
            paginator.addLine("- " + siege.getName());
        }
        if (!paginator.isInBounds(page)) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Paginator - Invalid Page") + " \"" + args[1] + "\"");
            return true;
        }
        for (String line : paginator.getPage(page))
            commandSender.sendMessage(line);
        return true;
    }

    private boolean beginCommand(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Siege - Must Be Player"));
            return true;
        }

        if (!commandSender.hasPermission("movecraft.siege.begin")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        SiegeManager siegeManager = MovecraftWarfare.getInstance().getSiegeManager();
        Player player = (Player) commandSender;
        for (Siege siege : siegeManager.getSieges()) {
            if (siege.getStage().get() != Siege.Stage.INACTIVE) {
                player.sendMessage(MOVECRAFT_COMMAND_PREFIX
                        + I18nSupport.getInternationalisedString("Siege - Siege Already Underway"));
                return true;
            }
        }
        Siege siege = getSiege(player, siegeManager);
        if (siege == null) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Siege - No Configuration Found"));
            return true;
        }

        long cost = calcSiegeCost(siege, siegeManager, player);
        if (!MovecraftRepair.getInstance().getEconomy().has(player, cost)) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + String.format(I18nSupport.getInternationalisedString("Siege - Insufficient Funds"), cost));
            return true;
        }

        int currMilitaryTime = getMilitaryTime();
        int dayOfWeek = getDayOfWeek();
        if (currMilitaryTime < siege.getConfig().getScheduleStart()
                || currMilitaryTime > siege.getConfig().getScheduleEnd()
                || !siege.getConfig().getDaysOfWeek().contains(dayOfWeek)) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Siege - Time Not During Schedule"));
            return true;
        }

        // check if piloting craft in siege region
        Craft siegeCraft = CraftManager.getInstance().getCraftByPlayer(player);
        if (siegeCraft == null) {
            player.sendMessage(
                    MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("You must be piloting a craft!"));
            return true;
        }
        if (!siege.getConfig().getCraftsToWin().contains(siegeCraft.getType().getStringProperty(CraftType.NAME))) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("You must be piloting a craft that can siege!"));
            return true;
        }
        if (!MovecraftWorldGuard.getInstance().getWGUtils().craftFullyInRegion(siege.getConfig().getAttackRegion(),
                siegeCraft.getWorld(), siegeCraft)) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("You must be piloting a craft in the siege region!"));
            return true;
        }

        SiegePreStartEvent siegePreStartEvent = new SiegePreStartEvent(siege, player);
        Bukkit.getPluginManager().callEvent(siegePreStartEvent);

        if (siegePreStartEvent.isCancelled()) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + siegePreStartEvent.getCancelReason());
            return true;
        }

        siege.start(player, cost);
        return true;
    }

    private Boolean statusCommand(CommandSender commandSender) {
        if (!commandSender.hasPermission("movecraft.siege.status")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX
                    + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }

        for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            if (siege.getStage().get() == Siege.Stage.INACTIVE)
                continue;

            Duration timePassed = Duration.between(siege.getStartTime(), LocalDateTime.now());

            StringBuilder message = new StringBuilder();
            if (siege.getStage().get() == Siege.Stage.PREPARATION) {
                long timeLeft = siege.getConfig().getDelayBeforeStart() - timePassed.getSeconds();
                message.append(String.format(I18nSupport.getInternationalisedString("Siege - Siege About To Begin"),
                        siege.getPlayer().getName(),
                        siege.getName()));
                message.append(SiegeUtils.formatMinutes(timeLeft));
                commandSender.sendMessage(message.toString());
                return true;
            }
            long timeLeft = siege.getConfig().getDuration() - timePassed.getSeconds();
            Craft siegeCraft = CraftManager.getInstance().getCraftByPlayer(siege.getPlayer().getPlayer());
            if (siege.leaderIsInControl()) {
                message.append(String.format(I18nSupport.getInternationalisedString("Siege - Flagship In Box"),
                        siege.getName(),
                        siegeCraft.getType().getStringProperty(CraftType.NAME),
                        siegeCraft.getOrigBlockCount(),
                        siege.getPlayer().getName(),
                        siegeCraft.getHitBox().getMidPoint().getX(),
                        siegeCraft.getHitBox().getMidPoint().getY(),
                        siegeCraft.getHitBox().getMidPoint().getZ()));
                message.append(SiegeUtils.formatMinutes(timeLeft));
            } else {
                message.append(String.format(I18nSupport.getInternationalisedString("Siege - Flagship Not In Box"),
                        siege.getName(),
                        siegeCraft.getType().getStringProperty(CraftType.NAME),
                        siegeCraft.getOrigBlockCount(),
                        siege.getPlayer().getName(),
                        siegeCraft.getHitBox().getMidPoint().getX(),
                        siegeCraft.getHitBox().getMidPoint().getY(),
                        siegeCraft.getHitBox().getMidPoint().getZ()));
                message.append(SiegeUtils.formatMinutes(timeLeft));
            }
            commandSender.sendMessage(message.toString());
            if (siege.isSuddenDeathActive()) {
                commandSender.sendMessage(String.format(I18nSupport.getInternationalisedString("Siege - Sudden Death"), siege.getPlayer().getName(), (timeLeft / 60) + 1));
            }
            return true;
        }
        commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX
                + I18nSupport.getInternationalisedString("Siege - No Siege Underway"));
        return true;
    }

    @Nullable
    private Siege getSiege(@NotNull Player player, SiegeManager siegeManager) {
        Set<String> regions = MovecraftWorldGuard.getInstance().getWGUtils().getRegions(player.getLocation());
        for (String region : regions) {
            for (Siege siege : siegeManager.getSieges()) {
                if (siege.getConfig().getAttackRegion().equalsIgnoreCase(region))
                    return siege;
            }
        }
        return null;
    }

    private long calcSiegeCost(@NotNull Siege siege, @NotNull SiegeManager siegeManager, Player player) {
        long cost = siege.getConfig().getCost();
        for (Siege tempSiege : siegeManager.getSieges()) {
            Set<UUID> regionOwners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(
                    tempSiege.getConfig().getCaptureRegion(), player.getWorld());
            if (regionOwners == null)
                continue;

            if (tempSiege.getConfig().isDoubleCostPerOwnedSiegeRegion() && regionOwners.contains(player.getUniqueId()))
                cost *= 2;
        }
        return cost;
    }

    private int getMilitaryTime() {
        Calendar rightNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        return hour * 100 + minute;
    }

    private int getDayOfWeek() {
        Calendar rightNow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return rightNow.get(Calendar.DAY_OF_WEEK);
    }

    private String dayToString(int day) {
        String output;
        switch (day) {
            case 1:
                output = "Siege - Sunday";
                break;
            case 2:
                output = "Siege - Monday";
                break;
            case 3:
                output = "Siege - Tuesday";
                break;
            case 4:
                output = "Siege - Wednesday";
                break;
            case 5:
                output = "Siege - Thursday";
                break;
            case 6:
                output = "Siege - Friday";
                break;
            case 7:
                output = "Siege - Saturday";
                break;
            default:
                output = "Invalid Day";
                break;
        }
        output = I18nSupport.getInternationalisedString(output);
        return output;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        final List<String> tabCompletions = new ArrayList<>();
        if (strings.length <= 1) {
            if (commandSender.hasPermission("movecraft.siege.info"))
                tabCompletions.add("info");
            if (commandSender.hasPermission("movecraft.siege.begin"))
                tabCompletions.add("begin");
            if (commandSender.hasPermission("movecraft.siege.list"))
                tabCompletions.add("list");
            if (commandSender.hasPermission("movecraft.siege.time"))
                tabCompletions.add("time");
            if (commandSender.hasPermission("movecraft.siege.cancel"))
                tabCompletions.add("cancel");
            if (commandSender.hasPermission("movecraft.siege.status"))
                tabCompletions.add("status");
        } else if (strings[0].equalsIgnoreCase("info")) {
            for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
                tabCompletions.add(siege.getName());
            }
        }
        if (strings.length == 0)
            return tabCompletions;

        final List<String> completions = new ArrayList<>();
        for (String completion : tabCompletions) {
            if (!completion.startsWith(strings[strings.length - 1]))
                continue;

            completions.add(completion);
        }
        return completions;
    }
}
