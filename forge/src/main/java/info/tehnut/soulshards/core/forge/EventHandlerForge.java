package info.tehnut.soulshards.core.forge;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandlerForge {

    @SubscribeEvent
    public static void onAnvil(AnvilUpdateEvent event) {
        if (!SoulShards.CONFIG.getBalance().allowShardCombination())
            return;

        if (event.getLeft().getItem() instanceof ItemSoulShard && event.getRight().getItem() instanceof ItemSoulShard) {
            Binding left = ((ItemSoulShard) event.getLeft().getItem()).getBinding(event.getLeft());
            Binding right = ((ItemSoulShard) event.getRight().getItem()).getBinding(event.getRight());

            if (left == null || right == null)
                return;

            if (left.getBoundEntity() != null && left.getBoundEntity().equals(right.getBoundEntity())) {
                ItemStack output = new ItemStack(RegistrarSoulShards.SOUL_SHARD.get());
                ((ItemSoulShard) output.getItem()).updateBinding(output, left.addKills(right.getKills()));
                event.setOutput(output);
                event.setCost(left.getTier().getIndex() * 6);
            }
        }
    }

    @SubscribeEvent
    public static void dropExperience(LivingExperienceDropEvent event) {
        if (!SoulShards.CONFIG.getBalance().shouldDropExperience() && event.getEntityLiving().getPersistentData().getBoolean("cageBorn"))
            event.setCanceled(true);
    }

}
