package eatAWESOME.pixelmonpokedexrewards.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eatAWESOME.pixelmonpokedexrewards.PixelmonPokedexRewards;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.minecraftforge.fml.loading.FMLPaths;

public class ConfigLoader {
    private static final String CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("PixelmonPokedexRewards.json").toString();
    private static final String DEFAULT_CONFIG_PATH = "/config/PixelmonPokedexRewards.json";
    private static JsonObject configData;

    public static void loadConfig() {
        File configFile = new File(CONFIG_PATH);
        if (!configFile.exists()) {
            System.out.println("[PixelmonPokedexRewards] Config not found. Creating default config...");
            copyDefaultConfig(configFile);
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
            JsonParser parser = new JsonParser();
            configData = (JsonObject) parser.parse(reader);
            System.out.println("[PixelmonPokedexRewards] Config loaded successfully!");
        } catch (Exception e) {
            System.err.println("[PixelmonPokedexRewards] Failed to load config: " + e.getMessage());
            configData = new JsonObject();
        }
    }

    private static void copyDefaultConfig(File configFile) {
        try (InputStream in = PixelmonPokedexRewards.class.getResourceAsStream(DEFAULT_CONFIG_PATH);
             FileOutputStream out = new FileOutputStream(configFile)) {
            if (in == null) {
                System.err.println("[PixelmonPokedexRewards] ERROR: Default config file is missing from resources!");
                return;
            }
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("[PixelmonPokedexRewards] Default config created successfully!");
        } catch (IOException e) {
            System.err.println("[PixelmonPokedexRewards] Failed to copy default config: " + e.getMessage());
        }
    }
    
    public static JsonObject getReward(int pokedexCompletion) {
        return configData.has(String.valueOf(pokedexCompletion)) ? configData.getAsJsonObject(String.valueOf(pokedexCompletion)) : null;
    }
}