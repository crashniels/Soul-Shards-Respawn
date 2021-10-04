package info.tehnut.soulshards.fabric;

import info.tehnut.soulshards.SoulShards;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SoulShardsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        //SoulShards.init();
    }

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

}
