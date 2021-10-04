package info.tehnut.soulshards;

import com.google.gson.reflect.TypeToken;
import dev.architectury.injectables.annotations.ExpectPlatform;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.util.JsonUtil;

import java.io.File;
import java.nio.file.Path;

public class SoulShards {

    public static final String MOD_ID = "soulshards";

    @ExpectPlatform
    public static Path getConfigDirectory() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    public static final ConfigSoulShards CONFIG = JsonUtil.fromJson(TypeToken.get(ConfigSoulShards.class),
            new File(getConfigDirectory().toFile(), MOD_ID + "/" + MOD_ID + ".json"), new ConfigSoulShards());


    public static void init() {
        Tier.readTiers();
        ConfigSoulShards.handleMultiblock();
        RegistrarSoulShards.BLOCKS.register();
        RegistrarSoulShards.ITEMS.register();
        RegistrarSoulShards.BLOCK_ENTITIES.register();
        RegistrarSoulShards.ENCHANTMENTS.register();
        EventHandler.init();
    }

}
