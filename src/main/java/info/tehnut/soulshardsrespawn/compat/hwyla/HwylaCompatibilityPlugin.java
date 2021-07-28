package info.tehnut.soulshardsrespawn.compat.hwyla;

import info.tehnut.soulshardsrespawn.block.TileEntitySoulCage;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import mcp.mobius.waila.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@WailaPlugin(id = "soulshards")
public class HwylaCompatibilityPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerEntityDataProvider((data, player, world, entity) ->
                data.putBoolean("cageBorn", entity.getPersistentData().contains("cageBorn")), LivingEntity.class);

        registrar.registerComponentProvider(new IEntityComponentProvider() {
            @Override
            public void appendBody(List<Component> tooltip, IEntityAccessor accessor, IPluginConfig config) {
                if (accessor.getServerData().getBoolean("cageBorn"))
                    tooltip.add(new TranslatableComponent("tooltip.soulshards.cage_born"));
            }
        }, TooltipPosition.BODY, LivingEntity.class);

        registrar.registerBlockDataProvider((data, player, world, blockEntity) -> {
            Binding binding = ((TileEntitySoulCage) blockEntity).getBinding();
            if (binding != null)
                data.put("binding", binding.serializeNBT());
        }, TileEntitySoulCage.class);

        registrar.registerComponentProvider(new IComponentProvider() {
            @Override
            public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
                if (!accessor.getServerData().contains("binding"))
                    return;

                Binding binding = new Binding(accessor.getServerData().getCompound("binding"));

                if (binding.getBoundEntity() != null) {
                    EntityType<?> entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
                    if (entityEntry != null)
                        tooltip.add(new TranslatableComponent("tooltip.soulshards.bound", entityEntry.getRegistryName()));
                    else
                        tooltip.add(new TranslatableComponent("tooltip.soulshards.bound", binding.getBoundEntity().toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }

                tooltip.add(new TranslatableComponent("tooltip.soulshards.tier", binding.getTier().getIndex()));
            }
        }, TooltipPosition.BODY, TileEntitySoulCage.class);
    }
}
