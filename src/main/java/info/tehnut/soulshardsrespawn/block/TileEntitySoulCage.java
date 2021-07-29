package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.SoulShards;
import info.tehnut.soulshardsrespawn.api.CageSpawnEvent;
import info.tehnut.soulshardsrespawn.api.IShardTier;
import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.item.ItemSoulShard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySoulCage extends BlockEntity {

    private static ItemStackHandler inventory;
    private static boolean active = false;

    public TileEntitySoulCage(BlockPos pos, BlockState state) {
        super(RegistrarSoulShards.SOUL_CAGE_TE, pos, state);

        this.inventory = new SoulCageInventory();
    }

    public static void tick(Level world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        InteractionResultHolder<Binding> result = canSpawn(world, blockPos, blockState, blockEntity);
        if (result.getResult() != InteractionResult.SUCCESS) {
            if (active) {
                setState(false, world, blockPos, blockState);
                world.updateNeighborsAt(blockPos, blockState.getBlock());
            }
            return;
        }

        if (!active) {
            setState(true,world, blockPos, blockState);
            world.updateNeighborsAt(blockPos, blockState.getBlock());
        }

        if (world.getGameTime() % result.getObject().getTier().getCooldown() == 0)
            spawnEntities(world, blockPos, blockState, blockEntity);
    }

    private static void spawnEntities(Level world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityType<?> entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
        if (entityEntry == null)
            return;

        IShardTier tier = binding.getTier();
        for (int i = 0; i < tier.getSpawnAmount(); i++) {
            for (int attempts = 0; attempts < 5; attempts++) {
                double x = blockPos.getX() + (world.random.nextDouble() - world.random.nextDouble()) * 4.0D + 0.5D;
                double y = blockPos.getY() + world.random.nextInt(3);
                double z = blockPos.getZ() + (world.random.nextDouble() - world.random.nextDouble()) * 4.0D + 0.5D;
                BlockPos spawnAt = new BlockPos(x, y, z);

                if (spawnAt.equals(blockPos))
                    spawnAt = new BlockPos(x, y + 1, z);

                LivingEntity entityLiving = (LivingEntity) entityEntry.create(world);
                if (entityLiving == null)
                    continue;

                if (binding.getTier().checkLight() && !canSpawnInLight(entityLiving, spawnAt, world))
                    continue;

                entityLiving.moveTo(spawnAt, world.random.nextFloat() * 360F, 0F);
                entityLiving.getPersistentData().putBoolean("cageBorn", true);
                //entityLiving.forceSpawn = true;

                if (entityLiving.isAlive() && !hasReachedSpawnCap(entityLiving, blockPos, world) && world.noCollision(entityLiving)) { // func_226668_i_ -> checkNoEntityCollision
                    if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !entityLiving.canChangeDimensions())
                        continue;

                    CageSpawnEvent event = new CageSpawnEvent(binding, inventory.getStackInSlot(0), entityLiving);
                    if (MinecraftForge.EVENT_BUS.post(event))
                        continue;

                    world.addFreshEntity(entityLiving);
                    if (entityLiving instanceof Monster)
                        ((Monster) entityLiving).finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(spawnAt), MobSpawnType.SPAWNER, null, null);
                    break;
                }
            }
        }
    }

    private static InteractionResultHolder<Binding> canSpawn(Level world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        BlockState state = blockState;
        if (state.getBlock() != RegistrarSoulShards.SOUL_CAGE)
            return new InteractionResultHolder<>(InteractionResult.FAIL, null);

        ItemStack shardStack = inventory.getStackInSlot(0);
        if (shardStack.isEmpty() || !(shardStack.getItem() instanceof ItemSoulShard))
            return new InteractionResultHolder<>(InteractionResult.FAIL, null);

        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        IShardTier tier = binding.getTier();

        if (tier.getSpawnAmount() == 0)
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (SoulShards.CONFIG.getBalance().requireOwnerOnline() && !ownerOnline(world))
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getBalance().requireRedstoneSignal()) {
            if (state.getValue(BlockSoulCage.POWERED) && tier.checkRedstone())
                return new InteractionResultHolder<>(InteractionResult.FAIL, binding);
        } else if (!state.getValue(BlockSoulCage.POWERED))
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (tier.checkPlayer() && world.getNearestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 16, false) == null)
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, binding);
    }

    private static boolean canSpawnInLight(LivingEntity entityLiving, BlockPos pos, Level world) {
        return !(entityLiving instanceof Mob) || world.getBrightness(LightLayer.BLOCK, pos) <= 8;
    }

    private static boolean hasReachedSpawnCap(LivingEntity living, BlockPos blockPos, Level world) {
        AABB box = new AABB(blockPos.getX() - 16, blockPos.getY() - 16, blockPos.getZ() - 16, blockPos.getX() + 16, blockPos.getY() + 16, blockPos.getZ() + 16);

        int mobCount = world.getEntitiesOfClass(living.getClass(), box, e -> e != null && e.getPersistentData().getBoolean("cageBorn")).size();
        return mobCount >= SoulShards.CONFIG.getBalance().getSpawnCap();
    }

    public static void setState(boolean active, Level world, BlockPos blockPos, BlockState blockState) {
        BlockState state = blockState;
        if (!(state.getBlock() instanceof BlockSoulCage))
            return;

        world.setBlockAndUpdate(blockPos, state.setValue(BlockSoulCage.ACTIVE, active));
        TileEntitySoulCage.active = active;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        this.inventory.deserializeNBT(tag.getCompound("inventory"));
        this.active = tag.getBoolean("active");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", inventory.serializeNBT());
        tag.putBoolean("active", active);

        return super.serializeNBT();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        return LazyOptional.of(() -> inventory).cast();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Nullable
    public static Binding getBinding() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return null;

        return ((ItemSoulShard) stack.getItem()).getBinding(stack);
    }

    public static boolean ownerOnline(Level world) {
        Binding binding = getBinding();
        //noinspection ConstantConditions
        return binding != null && binding.getOwner() != null && world.getServer().getPlayerList().getPlayer(binding.getOwner()) == null;
    }

    public static class SoulCageInventory extends ItemStackHandler {

        public SoulCageInventory() {
            super(1);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!(stack.getItem() instanceof ItemSoulShard))
                return stack;

            Binding binding = ((ItemSoulShard) stack.getItem()).getBinding(stack);
            if (binding == null || binding.getBoundEntity() == null || !SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    }
}
