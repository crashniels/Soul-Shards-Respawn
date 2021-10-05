package info.tehnut.soulshards.forge;

import info.tehnut.soulshards.core.RegistrarSoulShards;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;

public class SoulShardsForgeClient {

    public static void initClient() {
        RenderLayers.setRenderLayer(RegistrarSoulShards.SOUL_CAGE.get(), RenderLayer.getCutout());
    }

}
