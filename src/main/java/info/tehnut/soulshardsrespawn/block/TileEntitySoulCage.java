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

    private ItemStackHandler inventory;
    private int activeTime;
    private boolean active = false;

    public TileEntitySoulCage(BlockPos pos, BlockState state) {
        super(RegistrarSoulShards.SOUL_CAGE_TE, pos, state);

        this.inventory = new SoulCageInventory();
    }

    public void tick() {
        if (level.isClientSide)
            return;

        InteractionResultHolder<Binding> result = canSpawn();
        if (result.getResult() != InteractionResult.SUCCESS) {
            if (active) {
                setState(false);
                level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
            }
            return;
        }

        if (!active) {
            setState(true);
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
        activeTime++;

        if (activeTime % result.getObject().getTier().getCooldown() == 0)
            spawnEntities();
    }

    private void spawnEntities() {
        Binding binding = getBinding();
        if (binding == null || binding.getBoundEntity() == null)
            return;

        EntityType<?> entityEntry = ForgeRegistries.ENTITIES.getValue(binding.getBoundEntity());
        if (entityEntry == null)
            return;

        IShardTier tier = binding.getTier();
        for (int i = 0; i < tier.getSpawnAmount(); i++) {
            for (int attempts = 0; attempts < 5; attempts++) {
                double x = getBlockPos().getX() + (getLevel().random.nextDouble() - getLevel().random.nextDouble()) * 4.0D + 0.5D;
                double y = getBlockPos().getY() + getLevel().random.nextInt(3);
                double z = getBlockPos().getZ() + (getLevel().random.nextDouble() - getLevel().random.nextDouble()) * 4.0D + 0.5D;
                BlockPos spawnAt = new BlockPos(x, y, z);

                if (spawnAt.equals(getBlockPos()))
                    spawnAt = new BlockPos(x, y + 1, z);

                LivingEntity entityLiving = (LivingEntity) entityEntry.create(getLevel());
                if (entityLiving == null)
                    continue;

                if (binding.getTier().checkLight() && !canSpawnInLight(entityLiving, spawnAt))
                    continue;

                entityLiving.moveTo(spawnAt, getLevel().random.nextFloat() * 360F, 0F);
                entityLiving.getPersistentData().putBoolean("cageBorn", true);
                //entityLiving.forceSpawn = true;

                if (entityLiving.isAlive() && !hasReachedSpawnCap(entityLiving) && getLevel().noCollision(entityLiving)) { // func_226668_i_ -> checkNoEntityCollision
                    if (!SoulShards.CONFIG.getBalance().allowBossSpawns() && !entityLiving.canChangeDimensions())
                        continue;

                    CageSpawnEvent event = new CageSpawnEvent(binding, inventory.getStackInSlot(0), entityLiving);
                    if (MinecraftForge.EVENT_BUS.post(event))
                        continue;

                    getLevel().addFreshEntity(entityLiving);
                    if (entityLiving instanceof Monster)
                        ((Monster) entityLiving).finalizeSpawn((ServerLevelAccessor) getLevel(), getLevel().getCurrentDifficultyAt(spawnAt), MobSpawnType.SPAWNER, null, null);
                    break;
                }
            }
        }
    }

    private InteractionResultHolder<Binding> canSpawn() {
        BlockState state = getBlockState();
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

        if (SoulShards.CONFIG.getBalance().requireOwnerOnline() && !ownerOnline())
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getEntityList().isEnabled(binding.getBoundEntity()))
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (!SoulShards.CONFIG.getBalance().requireRedstoneSignal()) {
            if (state.getValue(BlockSoulCage.POWERED) && tier.checkRedstone())
                return new InteractionResultHolder<>(InteractionResult.FAIL, binding);
        } else if (!state.getValue(BlockSoulCage.POWERED))
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        if (tier.checkPlayer() && getLevel().getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 16, false) == null)
            return new InteractionResultHolder<>(InteractionResult.FAIL, binding);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, binding);
    }

    private boolean canSpawnInLight(LivingEntity entityLiving, BlockPos pos) {
        return !(entityLiving instanceof Mob) || level.getBrightness(LightLayer.BLOCK, pos) <= 8;
    }

    private boolean hasReachedSpawnCap(LivingEntity living) {
        AABB box = new AABB(getBlockPos().getX() - 16, getBlockPos().getY() - 16, getBlockPos().getZ() - 16, getBlockPos().getX() + 16, getBlockPos().getY() + 16, getBlockPos().getZ() + 16);

        int mobCount = getLevel().getEntitiesOfClass(living.getClass(), box, e -> e != null && e.getPersistentData().getBoolean("cageBorn")).size();
        return mobCount >= SoulShards.CONFIG.getBalance().getSpawnCap();
    }

    public void setState(boolean active) {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof BlockSoulCage))
            return;
        
        getLevel().setBlockAndUpdate(getBlockPos(), state.setValue(BlockSoulCage.ACTIVE, active));
        this.active = active;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        this.inventory.deserializeNBT(tag.getCompound("inventory"));
        this.activeTime = tag.getInt("activeTime");
        this.active = tag.getBoolean("active");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", inventory.serializeNBT());
        tag.putInt("activeTime", activeTime);
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
    public Binding getBinding() {
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSoulShard))
            return null;

        return ((ItemSoulShard) stack.getItem()).getBinding(stack);
    }

    public boolean ownerOnline() {
        Binding binding = getBinding();
        //noinspection ConstantConditions
        return binding != null && binding.getOwner() != null && level.getServer().getPlayerList().getPlayer(binding.getOwner()) == null;
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
