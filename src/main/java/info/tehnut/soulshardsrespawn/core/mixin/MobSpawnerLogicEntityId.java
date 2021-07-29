package info.tehnut.soulshardsrespawn.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(BaseSpawner.class)
public interface MobSpawnerLogicEntityId {
    @Invoker("getEntityId")
    ResourceLocation getEntityIdentifier(@Nullable Level world, BlockPos blockPos);
}
