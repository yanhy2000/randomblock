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

    public static Set<Block> protectedBlockSet = new HashSet<>();
    public static Set<TagKey<Block>> protectedTags = new HashSet<>();

    public static void reload() {
        protectedBlocks = BlockConfig.loadProtectedBlocks();
        randomBlocks = BlockConfig.loadRandomBlocks();
        blockWeights = BlockConfig.loadBlockWeights();
        protectedBlockSet.clear();
        protectedTags.clear();

        for (String blockId : protectedBlocks) {
            if (blockId.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(blockId.substring(1));
                if (tagId != null) {
                    protectedTags.add(TagKey.of(Registries.BLOCK.getKey(), tagId));
                }
            } else {
                Block block = Registries.BLOCK.get(Identifier.tryParse(blockId));
                protectedBlockSet.add(block);
            }
        }
        initWeightedRandomBlocks();
    }

    public static boolean isProtected(BlockState state) {
        return protectedBlockSet.contains(state.getBlock()) ||
                protectedTags.stream().anyMatch(state::isIn);
    }

    private static void initWeightedRandomBlocks() {
        weightedRandomBlocks = new ArrayList<>();
        final int DEFAULT_WEIGHT = 50;// 默认权重
        for (String blockId : randomBlocks) {
            Block block = Registries.BLOCK.get(Identifier.tryParse(blockId));
            int weight = blockWeights.getOrDefault(blockId, DEFAULT_WEIGHT);
            for (int i = 0; i < weight; i++) {
                weightedRandomBlocks.add(block);
            }
        }
    }
}