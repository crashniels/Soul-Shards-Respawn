package info.tehnut.soulshards.core.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;

public class CageBornTagHandler {
    private static TrackedData<Boolean> _cageBornTag;

    public static void setCageBornTag(LivingEntity livingEntity, Boolean value){
        TrackedData<Boolean> cageBornTag = getTrackedDataCageBorn();
        livingEntity.getDataTracker().set(cageBornTag, value);
        setTrackedDataCageBorn(cageBornTag);
    }

    public static boolean getCageBornTag(LivingEntity livingEntity){
        TrackedData<Boolean> cageBornTag = getTrackedDataCageBorn();
        return livingEntity.getDataTracker().get(cageBornTag);
    }

    public static TrackedData<Boolean> getTrackedDataCageBorn(){
        return _cageBornTag;
    }

    public static void setTrackedDataCageBorn(TrackedData<Boolean> data){
        _cageBornTag = data;
    }
}
