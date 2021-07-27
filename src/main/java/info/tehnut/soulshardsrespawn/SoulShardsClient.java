package info.tehnut.soulshardsrespawn;

import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import net.minecraft.client.renderer.RenderType;

public class SoulShardsClient {

    public static void initClient() {
        RenderTypeLookup.setRenderLayer(RegistrarSoulShards.SOUL_CAGE, RenderType.cutout());
    }

}
