package info.tehnut.soulshardsrespawn.item;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import info.tehnut.soulshardsrespawn.api.ISoulShard;
import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import info.tehnut.soulshardsrespawn.core.mixin.MobSpawnerLogicEntityId;
import net.minecraft.client.renderer.item.ItemProperties;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

public class ItemSoulShard extends Item implements ISoulShard {

    public ItemSoulShard() {
        super(new Properties().tab(SoulShards.TAB_SS));

        ItemProperties.registerGeneric(new ResourceLocation(SoulShards.MODID, "bound"), (stack, worldIn, entityIn, a) -> getBinding(stack) != null ? 1.0F : 0.0F);
        ItemProperties.registerGeneric(new ResourceLocation(SoulShards.MODID, "tier"), (stack, world, entity, a) -> {
            Binding binding = getBinding(stack);
            if (binding == null)
                return 0F;

            return Float.parseFloat("0." + Tier.INDEXED.indexOf(binding.getTier()));
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.PASS;

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ItemStack stack = context.getPlayer().getItemInHand(context.getHand());
        Binding binding = getBinding(stack);
        if (binding == null)
            return InteractionResult.PASS;

        if (state.getBlock() instanceof SpawnerBlock) {
            if (!SoulShards.CONFIG.getBalance().allowSpawnerAbsorption()) {
                context.getPlayer().displayClientMessage(new TranslatableComponent("chat.soulshards.absorb_disabled"), true);
                return InteractionResult.PASS;
            }

            if (binding.getKills() >= Tier.maxKills)
                return InteractionResult.PASS;

            SpawnerBlockEntity mobSpawner = (SpawnerBlockEntity) context.getLevel().getBlockEntity(context.getClickedPos());
            if (mobSpawner == null)
                return InteractionResult.PASS;

            try {
                ResourceLocation entityId = ((MobSpawnerLogicEntityId) mobSpawner.getSpawner()).getEntityIdentifier(context.getLevel(), context.getClickedPos());
                if (!SoulShards.CONFIG.getEntityList().isEnabled(entityId))
                    return InteractionResult.PASS;

                if (entityId == null || binding.getBoundEntity() == null || !binding.getBoundEntity().equals(entityId))
                    return InteractionResult.FAIL;

                updateBinding(stack, binding.addKills(SoulShards.CONFIG.getBalance().getAbsorptionBonus()));
                context.getLevel().destroyBlock(context.getClickedPos(), false);
                return InteractionResult.SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (state.getBlock() == RegistrarSoulShards.SOUL_CAGE) {
            if (binding.getBoundEntity() == null)
                return InteractionResult.FAIL;

            TileEntitySoulCage cage = (TileEntitySoulCage) context.getLevel().getBlockEntity(context.getClickedPos());
            if (cage == null)
                return InteractionResult.PASS;

            IItemHandler itemHandler = cage.getInventory();
            if (itemHandler != null && itemHandler.getStackInSlot(0).isEmpty()) {
                ItemHandlerHelper.insertItem(itemHandler, stack.copy(), false);
                cage.setChanged();
                cage.setState(true, context.getLevel(), context.getClickedPos(), state);
                context.getPlayer().setItemInHand(context.getHand(), ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (!allowdedIn(group))
            return;

        items.add(new ItemStack(this));
        for (IShardTier tier : Tier.INDEXED) {
            ItemStack stack = new ItemStack(this);
            Binding binding = new Binding(null, tier.getKillRequirement());
            updateBinding(stack, binding);
            items.add(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        Binding binding = getBinding(stack);
        if (binding == null)
            return;

        if (binding.getBoundEntity() != null) {
            EntityType<?> entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
            if (entityEntry != null)
                tooltip.add(new TranslatableComponent("tooltip.soulshards.bound", entityEntry.getRegistryName()));
        }

        tooltip.add(new TranslatableComponent("tooltip.soulshards.tier", binding.getTier().getIndex()));
        tooltip.add(new TranslatableComponent("tooltip.soulshards.kills", binding.getKills()));
        if (flag.isAdvanced() && binding.getOwner() != null)
            tooltip.add(new TranslatableComponent("tooltip.soulshards.owner", binding.getOwner().toString()));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        Binding binding = getBinding(stack);
        return super.getDescriptionId(stack) + (binding == null || binding.getBoundEntity() == null ? "_unbound" : "");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        Binding binding = getBinding(stack);
        return binding != null && binding.getKills() >= Tier.maxKills;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        Binding binding = getBinding(stack);
        return SoulShards.CONFIG.getClient().displayDurabilityBar() && binding != null && binding.getKills() < Tier.maxKills;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        Binding binding = getBinding(stack);
        if (binding == null)
            return 1.0D;

        return 1.0D - ((double) binding.getKills() / (double) Tier.maxKills);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getBinding(stack) == null ? 64 : 1;
    }

    @Nullable
    @Override
    public Binding getBinding(ItemStack stack) {
        return Binding.fromNBT(stack);
    }

    public float getBindingFloatValue(ItemStack stack) {
        Binding binding = getBinding(stack);
        if(binding == null) {
            return 0F;
        }
        return Float.parseFloat("0." + Tier.INDEXED.indexOf(binding.getTier()));
    }

    public void updateBinding(ItemStack stack, Binding binding) {
        if (!stack.hasTag())
            stack.setTag(new CompoundTag());

        stack.getTag().put("binding", binding.serializeNBT());
    }
}
