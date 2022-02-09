package info.tehnut.soulshards.core.util.forge;

import net.minecraft.entity.LivingEntity;

public class CageBornTagHandlerImpl {
    public static void setCageBornTag(LivingEntity livingEntity, Boolean value) {
        livingEntity.getPersistentData().putBoolean("cageBorn", true);
    }

    public static boolean getCageBornTag(LivingEntity livingEntity) {
        return livingEntity.getPersistentData().getBoolean("cageBorn");
    }
}
