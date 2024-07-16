package net.countercraft.movecraft.warfare.localisation;

import net.countercraft.movecraft.warfare.MovecraftWarfare;
import net.countercraft.movecraft.warfare.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

public class I18nSupport {
    private static Properties languageFile;

    public static void init() {
        languageFile = new Properties();

        File langDirectory = new File(MovecraftWarfare.getInstance().getDataFolder().getAbsolutePath() + "/localisation");
        if (!langDirectory.exists()) {
            langDirectory.mkdirs();
        }

        InputStream stream = null;
        try {
            stream = new FileInputStream(langDirectory.getAbsolutePath()+"/mcwlang_" + Config.Locale + ".properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (stream == null) {
            MovecraftWarfare.getInstance().getLogger().log(Level.SEVERE, "Critical Error in localisation system!");
            MovecraftWarfare.getInstance().getServer().shutdown();
        }

        try {
            languageFile.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String get(String key) {
        String ret = languageFile.getProperty(key);
        if (ret != null) {
            return ret;
        } else {
            return key;
        }
    }

    @Deprecated(forRemoval = true)
    public static String getInternationalisedString(String key) {
        return get(key);
    }

    @Contract("_ -> new")
    public static @NotNull TextComponent getInternationalisedComponent(String key){
        return Component.text(get(key));
    }
}
