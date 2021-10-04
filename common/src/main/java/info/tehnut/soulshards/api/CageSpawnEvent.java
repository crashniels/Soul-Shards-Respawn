package info.tehnut.soulshards.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface CageSpawnEvent {

    Event<CageSpawnEvent> CAGE_SPAWN = EventFactory.createCompoundEventResult(CageSpawnEvent.class,
            (listeners) -> (binding, shardStack, toSpawn) -> {
                for (CageSpawnEvent event : listeners) {
                    ActionResult result = event.onCageSpawn(binding, shardStack, toSpawn);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    ActionResult onCageSpawn(IBinding binding, ItemStack shardStack, LivingEntity toSpawn);
}
