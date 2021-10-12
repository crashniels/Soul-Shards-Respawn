package info.tehnut.soulshards.core;

import dev.architectury.hooks.block.BlockEntityHooks;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.block.BlockEntitySoulCage;
import info.tehnut.soulshards.block.BlockSoulCage;
import info.tehnut.soulshards.core.util.EnchantmentSoulStealer;
import info.tehnut.soulshards.item.ItemSoulShard;
import info.tehnut.soulshards.item.ItemVileSword;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RegistrarSoulShards {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SoulShards.MOD_ID, Registry.ITEM_KEY);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(SoulShards.MOD_ID, Registry.BLOCK_KEY);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(SoulShards.MOD_ID, Registry.BLOCK_ENTITY_TYPE_KEY);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(SoulShards.MOD_ID, Registry.ENCHANTMENT_KEY);

    public static final ItemGroup SoulShardsIG = CreativeTabRegistry.create(
            new Identifier(SoulShards.MOD_ID, "soulshards"),
            () -> new ItemStack(RegistrarSoulShards.VILE_SWORD.get()));

    /*
        Register Items
    */
    public static final RegistrySupplier<Item> SOUL_SHARD = ITEMS.register("soul_shard", ItemSoulShard::new);
    public static final RegistrySupplier<Item> VILE_SWORD = ITEMS.register("vile_sword", ItemVileSword::new);
    public static final RegistrySupplier<Item> CORRUPTED_ESSENCE = ITEMS.register("corrupted_essence", () -> new Item(new Item.Settings().group(RegistrarSoulShards.SoulShardsIG)));
    public static final RegistrySupplier<Item> CORRUPTED_INGOT = ITEMS.register("corrupted_ingot", () -> new Item(new Item.Settings().group(RegistrarSoulShards.SoulShardsIG)));
    public static final RegistrySupplier<Item> VILE_DUST = ITEMS.register("vile_dust", () -> new Item(new Item.Settings().group(RegistrarSoulShards.SoulShardsIG)));

    /*
        Register Blocks
    */
    public static final RegistrySupplier<Block> SOUL_CAGE = BLOCKS.register("soul_cage", BlockSoulCage::new);

    /*
        Register BlockEntityTypes
    */
    public static final RegistrySupplier<BlockEntityType<?>> SOUL_CAGE_TE = BLOCK_ENTITY_TYPES.register("soul_cage",
            () -> BlockEntityHooks.builder(BlockEntitySoulCage::new, SOUL_CAGE.get()).build(null));

    /*
        Register Enchantments
    */
    public static final RegistrySupplier<Enchantment> SOUL_STEALER = ENCHANTMENTS.register("soul_stealer", EnchantmentSoulStealer::new);

    /*
        Register BlockItems
    */
    public static final RegistrySupplier<Item> SOUL_CAGE_BI = ITEMS.register("soul_cage", () -> new BlockItem(SOUL_CAGE.get(), new Item.Settings().group(RegistrarSoulShards.SoulShardsIG)));


}
