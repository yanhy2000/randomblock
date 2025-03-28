package top.yanhy.randomblock.util;

import net.minecraft.block.Block;
import top.yanhy.randomblock.config.ConfigManager;

import java.util.List;
import java.util.Random;

public class WeightedRandomizer {
    private static final Random RANDOM = new Random();

    public static Block getRandomBlock() {
        List<Block> blocks = ConfigManager.weightedRandomBlocks;
        return blocks.get(RANDOM.nextInt(blocks.size()));
    }
}