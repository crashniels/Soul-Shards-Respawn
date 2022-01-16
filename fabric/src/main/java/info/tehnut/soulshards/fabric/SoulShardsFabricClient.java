package info.tehnut.soulshards.fabric;

import info.tehnut.soulshards.SoulShardsClient;
import net.fabricmc.api.ClientModInitializer;

public class SoulShardsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SoulShardsClient.initClient();
    }

}
