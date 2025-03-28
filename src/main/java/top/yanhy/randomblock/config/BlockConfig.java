package top.yanhy.randomblock.config;

import com.google.gson.Gson;
import net.minecraft.util.Identifier;
import java.io.InputStreamReader;
import java.util.*;

public class BlockConfig {
    private static final Gson GSON = new Gson();

    public static List<String> loadProtectedBlocks() {
        return loadJsonConfig("config/protected_blocks.json", List.class);
    }

    public static List<String> loadRandomBlocks() {
        return loadJsonConfig("config/random_blocks.json", List.class);
    }

    public static Map<String, Integer> loadBlockWeights() {
        return loadJsonConfig("config/block_weights.json", Map.class);
    }

    private static <T> T loadJsonConfig(String path, Class<T> type) {
        try (InputStreamReader reader = new InputStreamReader(
                BlockConfig.class.getClassLoader().getResourceAsStream(path))) {
            return GSON.fromJson(reader, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + path, e);
        }
    }
}