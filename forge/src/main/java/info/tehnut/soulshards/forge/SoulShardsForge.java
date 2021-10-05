package info.tehnut.soulshards.forge;

import dev.architectury.platform.forge.EventBuses;
import info.tehnut.soulshards.SoulShards;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SoulShards.MOD_ID)
public class SoulShardsForge {

    public SoulShardsForge() {
        EventBuses.registerModEventBus(SoulShards.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SoulShards.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
    }

    @SubscribeEvent
    public void setupClient(FMLClientSetupEvent event) {
        SoulShardsForgeClient.initClient();
    }
    
}
