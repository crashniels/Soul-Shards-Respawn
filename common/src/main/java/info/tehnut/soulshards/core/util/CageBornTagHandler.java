package info.tehnut.soulshards.core.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.entity.LivingEntity;

public class CageBornTagHandler {
    @ExpectPlatform
    public static void setCageBornTag(LivingEntity livingEntity, Boolean value){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean getCageBornTag(LivingEntity livingEntity){
        throw new AssertionError();
    }
}
