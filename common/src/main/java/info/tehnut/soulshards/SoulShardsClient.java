package info.tehnut.soulshards;

import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import net.minecraft.client.render.RenderLayer;

public class SoulShardsClient {
    public static void initClient() {
        RenderTypeRegistry.register(RenderLayer.getCutout(), RegistrarSoulShards.SOUL_CAGE.get());
    }
}
