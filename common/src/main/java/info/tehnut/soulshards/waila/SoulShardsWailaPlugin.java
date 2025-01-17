package info.tehnut.soulshards.waila;
/*
import info.tehnut.soulshards.block.TileEntitySoulCage;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.fabric.core.util.CageBornTagHandler;
import mcp.mobius.waila.api.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class SoulShardsWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        registrar.registerComponentProvider(new IEntityComponentProvider() {
            @Override
            public void appendBody(List<Text> tooltip, IEntityAccessor accessor, IPluginConfig config) {
                //if (accessor.getEntity().getDataTracker().get(MixinEntityLiving.cageBornTag))
                if(CageBornTagHandler.getCageBornTag((LivingEntity) accessor.getEntity()))
                    tooltip.add(new TranslatableText("tooltip.soulshards.cage_born"));
            }
        }, TooltipPosition.BODY, LivingEntity.class);

        registrar.registerBlockDataProvider((data, player, world, blockEntity) -> {
            Binding binding = ((TileEntitySoulCage) blockEntity).getBinding();
            if (binding != null)
                data.put("binding", binding.serializeNBT());
        }, TileEntitySoulCage.class);

        registrar.registerComponentProvider(new IComponentProvider() {
            @Override
            public void appendBody(List<Text> tooltip, IDataAccessor accessor, IPluginConfig config) {
                if (!accessor.getServerData().contains("binding")){
                    tooltip.add(new TranslatableText("tooltip.soulshards.unbound"));
                    return;
                }
                    
                Binding binding = new Binding(accessor.getServerData().getCompound("binding"));

                if (binding.getBoundEntity() != null) {
                    EntityType entityEntry = Registry.ENTITY_TYPE.get(binding.getBoundEntity());
                    if (entityEntry != null)
                        tooltip.add(new TranslatableText("tooltip.soulshards.bound", entityEntry.getName()));
                    else
                        tooltip.add(new TranslatableText("tooltip.soulshards.bound", binding.getBoundEntity().toString()).setStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.RED))));
                }
                //else {tooltip.add(new TranslatableText("tooltip.soulshards.unbound"));}

                tooltip.add(new TranslatableText("tooltip.soulshards.tier", binding.getTier().getIndex()));
            }
        }, TooltipPosition.BODY, TileEntitySoulCage.class);
    }
}
*/