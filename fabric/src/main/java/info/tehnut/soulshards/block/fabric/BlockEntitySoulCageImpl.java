package info.tehnut.soulshards.block.fabric;

import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.block.BlockEntitySoulCage;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.fabric.core.util.CageBornTagHandler;
import info.tehnut.soulshards.fabric.api.CageSpawnEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class BlockEntitySoulCageImpl {

    public static void spawnEntities(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        Binding binding = BlockEntitySoulCage.getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityType<?> entityType = Registry.ENTITY_TYPE.get(binding.getBoundEntity());
        IShardTier tier = binding.getTier();
        spawnLoop:
        for (int i = 0; i < tier.getSpawnAmount(); i++) {
            for (int attempts = 0; attempts < 5; attempts++) {

                double x = blockPos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * 4.0D;
                double y = blockPos.getY() + world.random.nextInt(3) - 1;
                double z = blockPos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * 4.0D;
                BlockPos spawnAt = new BlockPos(x, y, z);

                LivingEntity entityLiving = (LivingEntity) entityType.create(world);
                if (entityLiving == null)
                    continue;

                if (tier.checkLight() && !canSpawnInLight(entityLiving, spawnAt, world))
                    continue;

                entityLiving.refreshPositionAndAngles(spawnAt, world.random.nextFloat() * 360F, 0F);
                CageBornTagHandler.setCageBornTag(entityLiving, true);

                if (entityLiving.isAlive() && !hasReachedSpawnCap(entityLiving, blockPos, world) && !isColliding(entityLiving, world)) {
                    if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !entityLiving.canUsePortals()) // canUsePortals -> isNonBoss
                        continue;

                    ActionResult result = CageSpawnEvent.CAGE_SPAWN.invoker().onCageSpawn(binding, BlockEntitySoulCage.getInventory().getStack(0), entityLiving);
                    if (result == ActionResult.FAIL)
                        continue spawnLoop;

                    world.spawnEntity(entityLiving);
                    if (entityLiving instanceof MobEntity)
                        ((MobEntity) entityLiving).initialize((ServerWorldAccess) world, world.getLocalDifficulty(blockPos), SpawnReason.SPAWNER, null, null);
                    break;
                }
            }
        }
    }

    private static boolean canSpawnInLight(LivingEntity entityLiving, BlockPos pos, World world) {
        return !(entityLiving instanceof Monster) || world.getLightLevel(LightType.BLOCK, pos) <= 8;
    }

    private static boolean hasReachedSpawnCap(LivingEntity living, BlockPos blockPos, World world) {
        Box box = new Box(blockPos.getX() - 16, blockPos.getY() - 16, blockPos.getZ() - 16, blockPos.getX() + 16, blockPos.getY() + 16, blockPos.getZ() + 16);

        int mobCount = world.getEntitiesByClass(living.getClass(), box, e -> e != null &&
                CageBornTagHandler.getCageBornTag(living)).size();
        return mobCount >= SoulShards.CONFIG.getBalance().getSpawnCap();
    }

    private static boolean isColliding(LivingEntity entity, World world) {
        return world.isSpaceEmpty(entity.getBoundingBox()) && world.getEntitiesByClass(LivingEntity.class, entity.getBoundingBox(), e -> true).isEmpty();
    }

}
