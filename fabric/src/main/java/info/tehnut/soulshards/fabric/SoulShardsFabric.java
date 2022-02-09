package info.tehnut.soulshards.fabric;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.core.EventHandler;
import net.fabricmc.api.ModInitializer;

public class SoulShardsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SoulShards.init();
    }

}
