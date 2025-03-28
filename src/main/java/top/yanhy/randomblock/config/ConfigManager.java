package top.yanhy.randomblock.config;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.*;

public class ConfigManager {
    public static List<String> protectedBlocks;
    public static List<String> randomBlocks;
    public static Map<String, Integer> blockWeights;
    public static List<Block> weightedRandomBlocks;

    public static void reload() {
        protectedBlocks = BlockConfig.loadProtectedBlocks();
        randomBlocks = BlockConfig.loadRandomBlocks();
        blockWeights = BlockConfig.loadBlockWeights();
        initWeightedRandomBlocks();
    }

    private static void initWeightedRandomBlocks() {
        weightedRandomBlocks = new ArrayList<>();
        for (String blockId : randomBlocks) {
            // 使用新的方式获取 Block 对象
            Block block = Registries.BLOCK.get(Identifier.tryParse(blockId));
            int weight = blockWeights.getOrDefault(blockId, 1);
            for (int i = 0; i < weight; i++) {
                weightedRandomBlocks.add(block);
            }
        }
    }

    public static boolean isProtected(BlockState state) {
        // 获取 Block 的 ID
        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();

        return protectedBlocks.contains(blockId) ||
                protectedBlocks.stream().anyMatch(tag ->
                        tag.startsWith("#") && state.isIn(TagKey.of(Registries.BLOCK.getKey(), Identifier.tryParse(tag.substring(1))))
                );
    }
}
