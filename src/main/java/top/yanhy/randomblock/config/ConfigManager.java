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
    public static Set<Block> protectedBlockSet;
    public static Set<Long> processedChunks = new HashSet<>();

    public static void reload() {
        protectedBlocks = BlockConfig.loadProtectedBlocks();
        randomBlocks = BlockConfig.loadRandomBlocks();
        blockWeights = BlockConfig.loadBlockWeights();
        // 初始化受保护方块的 Set
        protectedBlockSet = new HashSet<>();
        for (String blockId : protectedBlocks) {
            if (blockId.startsWith("#")) continue; // 跳过标签
            Block block = Registries.BLOCK.get(Identifier.tryParse(blockId));
            protectedBlockSet.add(block);
        }
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
        // 先检查快速匹配
        if (protectedBlockSet.contains(state.getBlock())) return true;

        // 再检查标签
        for (String tagName : protectedBlocks) {
            if (tagName.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(tagName.substring(1));
                if (tagId != null && state.isIn(TagKey.of(Registries.BLOCK.getKey(), tagId))) {
                    return true;
                }
            }
        }
        return false;
    }
}
