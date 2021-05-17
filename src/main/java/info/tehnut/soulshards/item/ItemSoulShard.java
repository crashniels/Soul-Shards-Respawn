package info.tehnut.soulshards.item;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.api.ISoulShard;
import info.tehnut.soulshards.block.TileEntitySoulCage;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.data.Tier;
import info.tehnut.soulshards.core.mixin.MobSpawnerLogicEntityId;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.fabricmc.fabric.mixin.object.builder.ModelPredicateProviderRegistryAccessor;
import java.util.List;

public class ItemSoulShard extends Item implements ISoulShard {

    public ItemSoulShard() {
        super(new Settings().maxCount(1).group(SoulShards.RE_SoulShards));
        
        ModelPredicateProviderRegistryAccessor.callRegister(new Identifier(SoulShards.MODID, "bound"), (stack, worldIn, entityIn) -> getBinding(stack) != null ? 1.0F : 0.0F);
        ModelPredicateProviderRegistryAccessor.callRegister(new Identifier(SoulShards.MODID, "tier"), (stack, world, entity) -> {
            Binding binding = getBinding(stack);
            if (binding == null)
                return 0F;

            return Float.parseFloat("0." + Tier.INDEXED.indexOf(binding.getTier()));
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        Binding binding = getBinding(context.getStack());
        if (binding == null)
            return ActionResult.PASS;

        if (state.getBlock() instanceof SpawnerBlock) {
            if (!SoulShards.CONFIG.getBalance().allowSpawnerAbsorption()) {
                if (context.getPlayer() != null)
                    context.getPlayer().sendMessage(new TranslatableText("chat.soulshards.absorb_disabled"), true);
                return ActionResult.PASS;
            }

            if (binding.getKills() > Tier.maxKills)
                return ActionResult.PASS;

            MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) context.getWorld().getBlockEntity(context.getBlockPos());
            if (spawner == null)
                return ActionResult.PASS;

            try {
                Identifier entityId = ((MobSpawnerLogicEntityId) spawner.getLogic()).getEntityIdentifier();
                if (!SoulShards.CONFIG.getEntityList().isEnabled(entityId))
                    return ActionResult.PASS;

                if (binding.getBoundEntity() == null || !binding.getBoundEntity().equals(entityId))
                    return ActionResult.FAIL;

                updateBinding(context.getStack(), binding.addKills(SoulShards.CONFIG.getBalance().getAbsorptionBonus()));
                context.getWorld().breakBlock(context.getBlockPos(), false);
                return ActionResult.SUCCESS;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (state.getBlock() == RegistrarSoulShards.SOUL_CAGE) {
            if (binding.getBoundEntity() == null)
                return ActionResult.FAIL;

            TileEntitySoulCage cage = (TileEntitySoulCage) context.getWorld().getBlockEntity(context.getBlockPos());
            if (cage == null)
                return ActionResult.PASS;

            ItemStack cageStack = cage.getInventory().getStack(0);
            if (cageStack.isEmpty() && cage.getInventory().isValid(0, context.getStack())) {
                cage.getInventory().setStack(0, context.getStack().copy());
                context.getStack().decrement(1);
                cage.markDirty();
                cage.setState(true);
                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext options) {
        Binding binding = getBinding(stack);
        if (binding == null)
            return;

        Style greyColor = Style.EMPTY.withColor(Formatting.GRAY);
        if (binding.getBoundEntity() != null) {
            EntityType entityEntry = Registry.ENTITY_TYPE.get(binding.getBoundEntity());
            if (entityEntry != null)
                tooltip.add(new TranslatableText("tooltip.soulshards.bound", entityEntry.getName()).setStyle(greyColor));
            else
                tooltip.add(new TranslatableText("tooltip.soulshards.bound", binding.getBoundEntity().toString()).setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }

        tooltip.add(new TranslatableText("tooltip.soulshards.tier", binding.getTier().getIndex()).setStyle(greyColor));
        tooltip.add(new TranslatableText("tooltip.soulshards.kills", binding.getKills()).setStyle(greyColor));
        if (options.isAdvanced() && binding.getOwner() != null)
            tooltip.add(new TranslatableText("tooltip.soulshards.owner", binding.getOwner().toString()).setStyle(greyColor));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> items) {
        if (!isIn(group))
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
    public String getTranslationKey(ItemStack stack) {
        Binding binding = getBinding(stack);
        return super.getTranslationKey(stack) + (binding == null || binding.getBoundEntity() == null ? "_unbound" : "");
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        Binding binding = getBinding(stack);
        return binding != null && binding.getKills() >= Tier.maxKills;
    }

    @Override
    public Binding getBinding(ItemStack stack) {
        return Binding.fromNBT(stack);
    }

    public void updateBinding(ItemStack stack, Binding binding) {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            stack.setTag(tag = new CompoundTag());

        tag.put("binding", binding.serializeNBT());
    }
}
