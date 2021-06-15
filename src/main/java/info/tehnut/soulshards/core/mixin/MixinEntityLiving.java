package info.tehnut.soulshards.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.tehnut.soulshards.core.EventHandler;
import info.tehnut.soulshards.core.util.CageBornTagHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntity.class)
public class MixinEntityLiving {
    // FIX PLS THX
    
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void registerDataTracker(CallbackInfo callbackInfo) {
        TrackedData<Boolean> _cageBornTag = CageBornTagHandler.getTrackedDataCageBorn();
        _cageBornTag = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CageBornTagHandler.setTrackedDataCageBorn(_cageBornTag);
    }
    
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        TrackedData<Boolean> _cageBornTag = CageBornTagHandler.getTrackedDataCageBorn();
        entity.getDataTracker().startTracking(_cageBornTag, false);
        CageBornTagHandler.setTrackedDataCageBorn(_cageBornTag);
    }
    
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathEvent(DamageSource source, CallbackInfo callbackInfo) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity)
            return;

        EventHandler.onEntityDeath(entity, source);
    }
}
