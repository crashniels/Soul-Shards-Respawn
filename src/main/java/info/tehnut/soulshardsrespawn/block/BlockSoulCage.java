package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.core.RegistrarSoulShards;
import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockSoulCage extends BaseEntityBlock {

    public static final Property<Boolean> ACTIVE = BooleanProperty.create("active");
    public static final Property<Boolean> POWERED = BooleanProperty.create("powered");

    public BlockSoulCage() {
        super(Properties.copy(Blocks.SPAWNER));

        registerDefaultState(getStateDefinition().getOwner().defaultBlockState().setValue(ACTIVE, false).setValue(POWERED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isSteppingCarefully())
            return InteractionResult.PASS;

        TileEntitySoulCage cage = (TileEntitySoulCage) world.getBlockEntity(pos);
        if (cage == null)
            return InteractionResult.PASS;

        ItemStack stack = cage.getInventory().extractItem(0, 1, false);
        if (stack.isEmpty())
            return InteractionResult.PASS;

        ItemHandlerHelper.giveItemToPlayer(player, stack);
        return InteractionResult.SUCCESS;
    }


    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState state2, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if (state.getValue(POWERED) && !world.hasNeighborSignal(pos))
            world.setBlockAndUpdate(pos, state.setValue(POWERED, false));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState blockState2, boolean someBool) {
        if (state.hasBlockEntity() && state.getBlock() != blockState2.getBlock()) {
            TileEntitySoulCage cage = (TileEntitySoulCage) world.getBlockEntity(pos);
            if (cage != null) {
                ItemStack stack = cage.getInventory().getStackInSlot(0);
                Containers.dropContents(world, pos, NonNullList.of(ItemStack.EMPTY, stack));
            }
        }

        super.onRemove(state, world, pos, blockState2, someBool);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        TileEntitySoulCage cage = (TileEntitySoulCage) world.getBlockEntity(pos);
        if (cage == null)
            return 0;

        Binding binding = cage.getBinding();
        if (binding == null)
            return 0;

        return (int) (((double) binding.getTier().getIndex() / ((double) Tier.INDEXED.size() - 1)) * 15D);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos state, BlockState world) {
        return new TileEntitySoulCage(state, world);
    }

    private void handleRedstoneChange(Level world, BlockState state, BlockPos pos) {
        boolean powered = world.hasNeighborSignal(pos);
        if (state.getValue(POWERED) && !powered)
            world.setBlock(pos, state.setValue(POWERED, false), 2);
        else if (!state.getValue(POWERED) && powered)
            world.setBlock(pos, state.setValue(POWERED, true), 2);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntityTicker getTicker(Level world, BlockState blockState, BlockEntityType type) {
        return world.isClientSide ? null : createTickerHelper(type, RegistrarSoulShards.SOUL_CAGE_TE, TileEntitySoulCage::tick);
    }
}
