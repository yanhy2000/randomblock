package top.yanhy.randomblock.util;

import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.util.math.ChunkPos;

public class ChunkStorage {
    private static DB levelDB;
    private static final String DB_NAME = "randomblock_processed_chunks";

    public static void initDB(File worldDir) {
        try {
            File dbDir = new File(worldDir, DB_NAME);
            Options options = new Options();
            options.createIfMissing(true);

            levelDB = factory.open(dbDir, options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize LevelDB", e);
        }
    }

    public static boolean isChunkProcessed(ChunkPos pos) {
        if (levelDB == null) return false;

        byte[] key = getChunkKey(pos);
        byte[] value = levelDB.get(key);
        return value != null && Arrays.equals(value, "1".getBytes());
    }

    public static void markChunkProcessed(ChunkPos pos) {
        if (levelDB != null) {
            levelDB.put(getChunkKey(pos), "1".getBytes());
        }
    }

    public static void closeDB() {
        try {
            if (levelDB != null) {
                levelDB.close();
                levelDB = null;
            }
        } catch (IOException e) {
            System.err.println("Failed to close LevelDB");
        }
    }

    // 生成区块存储键（格式: "x,z"）
    private static byte[] getChunkKey(ChunkPos pos) {
        return (pos.x + "," + pos.z).getBytes(StandardCharsets.UTF_8);
    }
}