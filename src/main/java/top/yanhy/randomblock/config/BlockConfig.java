package top.yanhy.randomblock.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class BlockConfig {
    private static final Gson GSON = new Gson();

    // 定义泛型类型
    private static final Type LIST_STRING_TYPE = new TypeToken<List<String>>() {}.getType();
    private static final Type MAP_STRING_INT_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();

    public static List<String> loadProtectedBlocks() {
        return loadJsonConfig("rb_config/protected_blocks.json", LIST_STRING_TYPE);
    }

    public static List<String> loadRandomBlocks() {
        return loadJsonConfig("rb_config/random_blocks.json", LIST_STRING_TYPE);
    }

    public static Map<String, Integer> loadBlockWeights() {
        return loadJsonConfig("rb_config/block_weights.json", MAP_STRING_INT_TYPE);
    }

    private static <T> T loadJsonConfig(String path, Type type) {
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(BlockConfig.class.getClassLoader().getResourceAsStream(path)))) {
            return GSON.fromJson(reader, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + path, e);
        }
    }
}