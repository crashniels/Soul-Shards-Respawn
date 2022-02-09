package info.tehnut.soulshards;

import com.google.gson.reflect.TypeToken;
import dev.architectury.platform.Platform;
import info.tehnut.soulshards.core.EventHandler;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.util.JsonUtil;

import java.io.File;

public class SoulShards {

    public static final String MOD_ID = "soulshards";

    public static final ConfigSoulShards CONFIG = JsonUtil.fromJson(TypeToken.get(ConfigSoulShards.class),
            new File(Platform.getConfigFolder().toFile(), MOD_ID + "/" + MOD_ID + ".json"), new ConfigSoulShards());


    public static void init() {
        Tier.readTiers();
        ConfigSoulShards.handleMultiblock();
        RegistrarSoulShards.BLOCKS.register();
        RegistrarSoulShards.ITEMS.register();
        RegistrarSoulShards.BLOCK_ENTITY_TYPES.register();
        RegistrarSoulShards.ENCHANTMENTS.register();
        EventHandler.init();
    }

}
