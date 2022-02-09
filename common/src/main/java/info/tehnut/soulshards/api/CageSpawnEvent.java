package info.tehnut.soulshards.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import info.tehnut.soulshards.api.IBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface CageSpawnEvent {

    Event<CageSpawn> EVENT = EventFactory.createEventResult();

    interface CageSpawn {
        /**
         * This event is fired before a Soul Cage spawns an entity in the world.
         *
         * @param binding    The {@link IBinding} obtained from the Soul Shard.
         * @param shardStack The Soul Shard inserted into the Soul Cage.
         * @param toSpawn    The entity which is about to be placed in the world.
         */
        EventResult onCageSpawn(IBinding binding, ItemStack shardStack, LivingEntity toSpawn);
    }

}
