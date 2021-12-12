package info.tehnut.soulshards;

import java.io.File;

import com.google.gson.reflect.TypeToken;

import info.tehnut.soulshards.core.ConfigSoulShards;
import info.tehnut.soulshards.core.EventHandler;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.util.JsonUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.Rule;

public class SoulShards implements ModInitializer {

    public static final String MODID = "soulshards";
    public static final ConfigSoulShards CONFIG = JsonUtil.fromJson(TypeToken.get(ConfigSoulShards.class), new File(FabricLoader.getInstance().getConfigDir().toFile(), MODID + "/" + MODID + ".json"), new ConfigSoulShards());
    public static Rule<GameRules.BooleanRule> allowCageSpawns;

    @Override
    public void onInitialize() {
        Tier.readTiers();
        ConfigSoulShards.handleMultiblock();
        RegistrarSoulShards.registerBlocks(Registry.BLOCK);
        RegistrarSoulShards.registerItems(Registry.ITEM);
        RegistrarSoulShards.registerEnchantments(Registry.ENCHANTMENT);
        EventHandler.init();
    }

    public static final ItemGroup RE_SoulShards = FabricItemGroupBuilder.build(
		new Identifier(SoulShards.MODID, "soulshards"),
		() -> new ItemStack(RegistrarSoulShards.VILE_SWORD));
}
