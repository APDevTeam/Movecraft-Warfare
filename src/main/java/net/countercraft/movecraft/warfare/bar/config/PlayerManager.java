package net.countercraft.movecraft.warfare.bar.config;

import net.countercraft.movecraft.warfare.MovecraftWarfare;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.countercraft.movecraft.warfare.MovecraftWarfare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class PlayerManager implements Listener {
    private final Map<Player, PlayerConfig> cache = new WeakHashMap<>();

    @Nullable
    public boolean getAssaultBarSetting(Player player) {
        var config = cache.get(player);
        if (config == null)
            return true;

        return config.getAssaultBarSetting();
    }

    public void toggleAssaultBarSetting(Player player) {
        var config = cache.get(player);
        if (config == null) {
            config = loadPlayer(player);
            cache.put(player, config);
        }

        config.toggleAssaultBarSetting();
    }

    @Nullable
    public boolean getSiegeBarSetting(Player player) {
        var config = cache.get(player);
        if (config == null)
            return true;

        return config.getSiegeBarSetting();
    }

    public void toggleSiegeBarSetting(Player player) {
        var config = cache.get(player);
        if (config == null) {
            config = loadPlayer(player);
            cache.put(player, config);
        }

        config.toggleSiegeBarSetting();
    }

    private void savePlayer(Player player) {
        var config = cache.get(player);
        if (config == null)
            return;

        Gson gson = buildGson();
        String str = null;
        try {
            str = gson.toJson(config);
        } catch (JsonIOException e) {
            e.printStackTrace();
            return;
        }

        File file = getFile(player.getUniqueId());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(str);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    @NotNull
    private PlayerConfig loadPlayer(@NotNull Player player) {
        File file = getFile(player.getUniqueId());
        if (!file.exists() || !file.isFile() || !file.canRead())
            return new PlayerConfig(player.getUniqueId());

        Gson gson = buildGson();
        PlayerConfig config = null;
        try {
            config = gson.fromJson(new FileReader(file), new TypeToken<PlayerConfig>() {
            }.getType());
        } catch (FileNotFoundException ignored) {
            return new PlayerConfig(player.getUniqueId());
        } catch (JsonSyntaxException | JsonIOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private File getFile(UUID owner) {
        return new File(
                MovecraftWarfare.getInstance().getDataFolder().getAbsolutePath() + "/userdata/" + owner + ".json");
    }

    private static Gson buildGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.serializeNulls();
        return builder.create();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        Player player = e.getPlayer();
        var config = loadPlayer(player);
        cache.put(player, config);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        Player player = e.getPlayer();
        savePlayer(player);
        cache.remove(player);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPluginDisable(@NotNull PluginDisableEvent e) {
        if (e.getPlugin() != MovecraftWarfare.getInstance())
            return;

        for (Player p : cache.keySet()) {
            savePlayer(p);
        }
        cache.clear();
    }
}
