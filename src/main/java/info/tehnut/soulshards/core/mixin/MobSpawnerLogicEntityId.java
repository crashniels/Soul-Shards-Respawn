package info.tehnut.soulshards.core.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

@Mixin(MobSpawnerLogic.class)
public interface MobSpawnerLogicEntityId {
    @Invoker("getEntityId")
    Identifier getEntityIdentifier(@Nullable World world, BlockPos blockPos);
}