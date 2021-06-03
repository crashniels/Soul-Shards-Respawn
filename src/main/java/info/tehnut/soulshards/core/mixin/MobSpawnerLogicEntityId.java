package info.tehnut.soulshards.core.mixin;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobSpawnerLogic.class)
public interface MobSpawnerLogicEntityId {
    @Invoker("getEntityId")
    Identifier getEntityIdentifier(@Nullable World world, BlockPos blockPos);
}