package info.tehnut.soulshards.block;

import dev.architectury.event.EventResult;
import dev.architectury.registry.registries.Registries;
import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.CageSpawnEvent;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.core.util.CageBornTagHandler;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class BlockEntitySoulCage extends BlockEntity {

    private static Inventory inventory;
    private static boolean active;

    public BlockEntitySoulCage(BlockPos blockPos, BlockState blockState) {
        super(RegistrarSoulShards.SOUL_CAGE_TE.get(), blockPos, blockState);

        BlockEntitySoulCage.inventory = new SimpleInventory(1){
            @Override
            public boolean isValid(int slot, ItemStack stack) {
                if (!(stack.getItem() instanceof ItemSoulShard))
                    return false;

                Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
                return binding != null && binding.getBoundEntity() != null && SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity());
            }
        };
    }
    
    public static void tick(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        TypedActionResult<Binding> result = canSpawn(world, blockPos, blockState, blockEntity);
        if (result.getResult() != ActionResult.SUCCESS) {
            if (active) {
                setState(false, world, blockPos, blockState);
                world.updateNeighbors(blockPos, blockState.getBlock());
            }
            return;
        }

        if (!active) {
            setState(true, world, blockPos, blockState);
            world.updateNeighbors(blockPos, blockState.getBlock());
        }

        if (world.getTime() % result.getValue().getTier().getCooldown() == 0)
            spawnEntities(world, blockPos, blockState, blockEntity);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (tag.contains("shard"))
            inventory.setStack(0, ItemStack.fromNbt(tag.getCompound("shard")));
        BlockEntitySoulCage.active = tag.getBoolean("active");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        ItemStack shardStack = inventory.getStack(0);
        if (!shardStack.isEmpty())
            tag.put("shard", shardStack.writeNbt(new NbtCompound()));
        tag.putBoolean("active", active);

        return super.writeNbt(tag);
    }

    private static void spawnEntities(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        Binding binding = BlockEntitySoulCage.getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityType<?> entityType = Registries.get(SoulShards.MOD_ID).get(Registry.ENTITY_TYPE).get(binding.getBoundEntity());
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

                    EventResult result = CageSpawnEvent.EVENT.invoker().onCageSpawn(binding, BlockEntitySoulCage.getInventory().getStack(0), entityLiving);
                    if (result == EventResult.pass())
                        continue spawnLoop;

                    world.spawnEntity(entityLiving);
                    if (entityLiving instanceof MobEntity)
                        ((MobEntity) entityLiving).initialize((ServerWorldAccess) world, world.getLocalDifficulty(blockPos), SpawnReason.SPAWNER, null, null);
                    break;
                }
            }
        }
    }

    private static TypedActionResult<Binding> canSpawn(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        // TODO mojang pls
        //if (!world.getServer().getWorld(world.getRegistryKey()).getGameRules().getBoolean(SoulShards.allowCageSpawns))
        //    return new TypedActionResult<>(ActionResult.FAIL, null);

        if (blockState.getBlock() != RegistrarSoulShards.SOUL_CAGE.get())
            return new TypedActionResult<>(ActionResult.FAIL, null);

        ItemStack shardStack = inventory.getStack(0);
        if (shardStack.isEmpty() || !(shardStack.getItem() instanceof ItemSoulShard))
            return new TypedActionResult<>(ActionResult.FAIL, null);

        Binding binding = ((ItemSoulShard) shardStack.getItem()).getBinding(shardStack);
        if (binding == null || binding.getBoundEntity() == null)
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        IShardTier tier = binding.getTier();
        if (tier.getSpawnAmount() == 0)
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (SoulShards.CONFIG.getBalance().requireOwnerOnline() && !ownerOnline(world))
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getBalance().requireRedstoneSignal()) {
            if (blockState.get(BlockSoulCage.POWERED) && tier.checkRedstone())
                return new TypedActionResult<>(ActionResult.FAIL, binding);
        } else if (!blockState.get(BlockSoulCage.POWERED))
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        if (tier.checkPlayer() && world.getClosestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 16, false) == null)
            return new TypedActionResult<>(ActionResult.FAIL, binding);

        return new TypedActionResult<>(ActionResult.SUCCESS, binding);
    }

    public static Binding getBinding() {
        ItemStack stack = inventory.getStack(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return null;

        return ((ItemSoulShard) stack.getItem()).getBinding(stack);
    }

    public static void setState(boolean active, World world, BlockPos blockPos, BlockState blockState) {
        if (!(blockState.getBlock() instanceof BlockSoulCage))
            return;

        world.setBlockState(blockPos, blockState.with(BlockSoulCage.ACTIVE, active));
        BlockEntitySoulCage.active = active;
    }

    public static boolean ownerOnline(World world) {
        Binding binding = getBinding();
        //noinspection ConstantConditions
        return binding != null && binding.getOwner() != null && world.getServer().getPlayerManager().getPlayer(binding.getOwner()) == null;
    }

    public static Inventory getInventory() {
        return inventory;
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
