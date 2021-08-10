package info.tehnut.soulshardsrespawn.core;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.block.BlockSoulCage;
import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import info.tehnut.soulshardsrespawn.core.util.EnchantmentSoulStealer;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import info.tehnut.soulshardsrespawn.item.ItemVileSword;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = SoulShards.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(SoulShards.MODID)
public class RegistrarSoulShards {

    public static final Block SOUL_CAGE = new BlockSoulCage();

    @ObjectHolder("soul_cage")
    public static final BlockEntityType<TileEntitySoulCage> SOUL_CAGE_TE = BlockEntityType.Builder.of(TileEntitySoulCage::new, SOUL_CAGE).build(null);

    public static final Item SOUL_SHARD = new ItemSoulShard();
    public static final Item VILE_SWORD = new ItemVileSword();
    public static final Item CORRUPTED_ESSENCE = new Item(new Item.Properties().tab(SoulShards.TAB_SS));
    public static final Item CORRUPTED_INGOT = new Item(new Item.Properties().tab(SoulShards.TAB_SS));
    public static final Item VILE_DUST = new Item(new Item.Properties().tab(SoulShards.TAB_SS));

    public static final Enchantment SOUL_STEALER = new EnchantmentSoulStealer();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                SOUL_CAGE.setRegistryName("soul_cage")
        );
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        event.getRegistry().register(
                SOUL_CAGE_TE.setRegistryName(SoulShards.MODID, "soul_cage")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        Tier.readTiers();

        event.getRegistry().registerAll(
                new BlockItem(SOUL_CAGE, new Item.Properties().tab(SoulShards.TAB_SS)).setRegistryName(SOUL_CAGE.getRegistryName()),
                VILE_SWORD.setRegistryName("vile_sword"),
                SOUL_SHARD.setRegistryName("soul_shard"),
                CORRUPTED_ESSENCE.setRegistryName("corrupted_essence"),
                CORRUPTED_INGOT.setRegistryName("corrupted_ingot"),
                VILE_DUST.setRegistryName("vile_dust")
        );
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().registerAll(
                new EnchantmentSoulStealer().setRegistryName("soul_stealer")
        );
    }
}
