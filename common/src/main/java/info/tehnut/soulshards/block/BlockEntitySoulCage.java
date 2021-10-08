package info.tehnut.soulshards.block;

import dev.architectury.injectables.annotations.ExpectPlatform;
import info.tehnut.soulshards.SoulShards;
import info.tehnut.soulshards.api.IShardTier;
import info.tehnut.soulshards.core.RegistrarSoulShards;
import info.tehnut.soulshards.core.data.Binding;
import info.tehnut.soulshards.item.ItemSoulShard;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
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

    @ExpectPlatform
    private static void spawnEntities(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        throw new AssertionError();
    }

    private static TypedActionResult<Binding> canSpawn(World world, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        // TODO mojang pls
        //if (!getWorld().getServer().getWorld(DimensionType.OVERWORLD_ID).getGameRules().getBoolean(SoulShards.allowCageSpawns))
        //    return new TypedActionResult<>(ActionResult.FAIL, null);

        BlockState state = world.getBlockState(blockPos);
        if (state.getBlock() != RegistrarSoulShards.SOUL_CAGE)
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
            if (state.get(BlockSoulCage.POWERED) && tier.checkRedstone())
                return new TypedActionResult<>(ActionResult.FAIL, binding);
        } else if (!state.get(BlockSoulCage.POWERED))
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
        BlockState state = blockState;
        if (!(state.getBlock() instanceof BlockSoulCage))
            return;

        world.setBlockState(blockPos, state.with(BlockSoulCage.ACTIVE, active));
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
}
