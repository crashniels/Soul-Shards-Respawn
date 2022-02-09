package info.tehnut.soulshards.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import info.tehnut.soulshards.api.IBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public interface BindingEvent {

    Event<NewBinding> NEW_BINDINGS = EventFactory.createEventResult();

    Event<GainSouls> GAIN_SOULS = EventFactory.createEventResult();

    Event<GetEntityName> GET_ENTITY_ID = EventFactory.createEventResult();

    interface NewBinding {
        /**
         * This event is fired if a new {@link IBinding} is needed.
         *
         * @param entity    The entity to get the
         * @param binding   The {@link IBinding} obtained from the Soul Shard.
         */
        TypedActionResult<IBinding> onNewBinding(LivingEntity entity, IBinding binding);
    }

    interface GainSouls {
        /**
         * This event is fired after an entity dies.
         *
         * @param entity    The entity which just died.
         * @param binding   The {@link IBinding} obtained from the Soul Shard.
         * @param amount    The amount of souls which will be added.
         */
        int getGainedSouls(LivingEntity entity, IBinding binding, int amount);
    }

    interface GetEntityName {
        /**
         * This event is fired
         *
         * @param entity        The entity itself.
         * @param currentName   The current registry id of the entity.
         */
        Identifier getEntityName(LivingEntity entity, Identifier currentName);
    }

}
