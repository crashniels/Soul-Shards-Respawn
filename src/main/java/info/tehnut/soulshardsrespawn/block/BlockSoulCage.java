package info.tehnut.soulshardsrespawn.block;

import info.tehnut.soulshardsrespawn.core.data.Binding;
import info.tehnut.soulshardsrespawn.core.data.Tier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockSoulCage extends Block {

    public static final Property<Boolean> POWERED = BooleanProperty.create("powered");
    public static final Property<Boolean> ACTIVE = BooleanProperty.create("active");

    public BlockSoulCage() {
        super(Properties.of(Material.METAL).harvestLevel(1).harvestTool(ToolType.PICKAXE).destroyTime(3.0F).noCollission());

        registerDefaultState(getStateDefinition().any().setValue(POWERED, false).setValue(ACTIVE, false));
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
    public void onBlockAdded(BlockState state, Level world, BlockPos pos, BlockState state2, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean someBool) {
        handleRedstoneChange(world, state, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED) && !world.isBlockPowered(pos))
            world.setBlockState(pos, state.with(POWERED, false));
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState blockState2, boolean someBool) {
        if (this.hasTileEntity(state) && state.getBlock() != blockState2.getBlock()) {
            TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
            if (cage != null) {
                ItemStack stack = cage.getInventory().getStackInSlot(0);
                InventoryHelper.dropItems(world, pos, NonNullList.from(ItemStack.EMPTY, stack));
            }
        }

        super.onReplaced(state, world, pos, blockState2, someBool);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        TileEntitySoulCage cage = (TileEntitySoulCage) world.getTileEntity(pos);
        if (cage == null)
            return 0;

        Binding binding = cage.getBinding();
        if (binding == null)
            return 0;

        return (int) (((double) binding.getTier().getIndex() / ((double) Tier.INDEXED.size() - 1)) * 15D);
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader reader, BlockPos pos) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ACTIVE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntitySoulCage();
    }

    private void handleRedstoneChange(World world, BlockState state, BlockPos pos) {
        boolean powered = world.isBlockPowered(pos);
        if (state.get(POWERED) && !powered)
            world.setBlockState(pos, state.with(POWERED, false), 2);
        else if (!state.get(POWERED) && powered)
            world.setBlockState(pos, state.with(POWERED, true), 2);
    }
}
