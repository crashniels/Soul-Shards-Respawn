package info.tehnut.soulshards.fabric.mixin;

import info.tehnut.soulshards.core.util.fabric.CageBornTagHandlerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntity.class)
public class MixinEntityLiving {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerDataTracker(CallbackInfo callbackInfo) {
        TrackedData<Boolean> _cageBornTag;
        _cageBornTag = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CageBornTagHandlerImpl.setTrackedDataCageBorn(_cageBornTag);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        TrackedData<Boolean> _cageBornTag = CageBornTagHandlerImpl.getTrackedDataCageBorn();
        entity.getDataTracker().startTracking(_cageBornTag, false);
        CageBornTagHandlerImpl.setTrackedDataCageBorn(_cageBornTag);
    }
    
}
