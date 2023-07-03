package net.countercraft.movecraft.warfare.features.assault;

import net.countercraft.movecraft.repair.MovecraftRepair;
import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.countercraft.movecraft.warfare.features.siege.Siege;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AssaultUtils {
    public static boolean areDefendersOnline(String regionName, World w) {
        int numOnline = 0;

        numOnline += ownersOnline(regionName, w);
        if (Config.AssaultRequiredOwnersOnline > 0 && numOnline < Config.AssaultRequiredOwnersOnline) {
            return false;
        }

        numOnline += membersOnline(regionName, w);
        return numOnline >= Config.AssaultRequiredDefendersOnline;
    }

    private static int ownersOnline(String regionName, World w) {
        int numOnline = 0;
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if (owners == null)
            return 0;

        for (UUID playerID : owners) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    private static int membersOnline(String regionName, World w) {
        int numOnline = 0;
        Set<UUID> members = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDMembers(regionName, w);
        if (members == null)
            return 0;

        for (UUID playerID : members) {
            if (Bukkit.getPlayer(playerID) != null)
                numOnline++;
        }
        return numOnline;
    }

    public static double getCostToAssault(String regionName, World w) {
        return getAssaultBalance(regionName, w) * Config.AssaultCostPercent;
    }

    public static double getMaxDamages(String regionName, World w) {
        return getAssaultBalance(regionName, w) * Config.AssaultDamagesCapPercent;
    }

    private static double getAssaultBalance(String regionName, World w) {
        return getOwnerBalance(regionName, w) + getMemberBalance(regionName, w);
    }

    private static double getOwnerBalance(String regionName, World w) {
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if (owners == null)
            return 0.0;

        double total = 0.0;
        for (UUID playerID : owners) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP),
                        Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultOwnerWeightPercent / 100.0);
    }

    private static double getMemberBalance(String regionName, World w) {
        Set<UUID> members = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDMembers(regionName, w);
        if (members == null)
            return 0.0;

        double total = 0.0;
        for (UUID playerID : members) {
            OfflinePlayer offP = Bukkit.getOfflinePlayer(playerID);
            if (offP.getName() != null)
                total += Math.min(MovecraftRepair.getInstance().getEconomy().getBalance(offP),
                        Config.AssaultMaxBalance);
        }
        return total * (Config.AssaultMemberWeightPercent / 100.0);
    }

    public static boolean ownsRegions(Player p) {
        return MovecraftWorldGuard.getInstance().getWGUtils().ownsAssaultableRegion(p);
    }

    public static boolean isMember(String regionName, World w, Player p) {
        return MovecraftWorldGuard.getInstance().getWGUtils().isMember(regionName, w, p);
    }

    public static boolean canAssault(String regionName, World w) {
        if (!MovecraftWorldGuard.getInstance().getWGUtils().regionExists(regionName, w))
            return false;

        // a region can only be assaulted if it disables TNT, this is to prevent child
        // regions or sub regions from being assaulted
        if (!MovecraftWorldGuard.getInstance().getWGUtils().isTNTDenied(regionName, w))
            return false;

        // regions with no owners can not be assaulted
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(regionName, w);
        if (owners == null || owners.size() == 0)
            return false;

        List<AssaultData> data = AssaultUtils.retrieveInfoFile(regionName, w.getName());
        if (data != null && data.size() > 0) {
            LocalDateTime lastStartTime = data.get(0).getStartTime();
            if (lastStartTime != null) {
                // We have had a previous assault, check the time
                Duration delta = Duration.between(lastStartTime, LocalDateTime.now());
                if (delta.toHours() < Config.AssaultCooldownHours)
                    return false;
            }
        }

        if (!Config.SiegeEnable)
            return true;

        for (Siege siege : MovecraftWarfare.getInstance().getSiegeManager().getSieges()) {
            // siege-able regions can not be assaulted
            if (regionName.equalsIgnoreCase(siege.getConfig().getAttackRegion())
                    || regionName.equalsIgnoreCase(siege.getConfig().getCaptureRegion()))
                return false;
        }
        return true;
    }

    private static File getInfoFile(String regionName, String worldName) {
        File saveDirectory = new File(MovecraftWarfare.getInstance().getDataFolder(),
                "AssaultSnapshots/" + worldName + "/" + regionName.replaceAll("´\\s+", "_"));
        if (!saveDirectory.exists())
            saveDirectory.mkdirs();
        return new File(saveDirectory, "info.json");
    }

    private static Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.serializeNulls();
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        return builder.create();
    }

    public static boolean saveInfoFile(Assault assault) {
        Set<UUID> owners = MovecraftWorldGuard.getInstance().getWGUtils().getUUIDOwners(assault.getRegionName(),
                assault.getWorld());
        List<AssaultData> data = retrieveInfoFile(assault.getRegionName(), assault.getWorld().getName());
        data.add(0, new AssaultData(owners, assault.getStartTime()));

        Gson gson = buildGson();
        String str = null;
        try {
            str = gson.toJson(data);
        } catch (JsonIOException e) {
            e.printStackTrace();
            return false;
        }

        File file = getInfoFile(assault.getRegionName(), assault.getWorld().getName());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Nullable
    public static List<AssaultData> retrieveInfoFile(String regionName, String worldName) {
        File file = getInfoFile(regionName, worldName);

        Gson gson = buildGson();
        List<AssaultData> data = null;
        try {
            data = gson.fromJson(new FileReader(file), new TypeToken<LinkedList<AssaultData>>(){}.getType());
        } catch (FileNotFoundException ignored) {
        } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

        @Override
        public JsonElement serialize(LocalDateTime source, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(FORMAT.format(source));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), FORMAT);
        }
    }
}
