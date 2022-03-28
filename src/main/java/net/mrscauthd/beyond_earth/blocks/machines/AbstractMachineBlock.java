package net.mrscauthd.beyond_earth.blocks.machines;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.mrscauthd.beyond_earth.blocks.machines.entity.AbstractMachineBlockEntity;

public abstract class AbstractMachineBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public AbstractMachineBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.buildDefaultState());
    }

    protected BlockState buildDefaultState() {
        BlockState state = this.stateManager.getDefaultState();

        if (this.useFacing()) {
            state = state.with(FACING, Direction.NORTH);
        }
        if (this.useLit()) {
            state = state.with(LIT, false);
        }

        return state;
    }

    protected boolean useFacing() {
        return false;
    }

    protected boolean useLit() {
        return false;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            AbstractMachineBlockEntity blockEntity = (AbstractMachineBlockEntity) world.getBlockEntity(pos);

            if (blockEntity != null) {
                player.openHandledScreen(blockEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        if (this.useFacing()) {
            return state.with(FACING, rotation.rotate(state.get(FACING)));
        } else {
            return state;
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AbstractMachineBlockEntity machineBlock) {
                if (machineBlock.hasInventory()) {
                    ItemScatterer.spawn(world, pos, machineBlock);
                    world.updateComparators(pos, this);
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.PUSH_ONLY;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        if (this.useFacing()) {
            builder.add(FACING);
        }

        if (this.useLit()) {
            builder.add(LIT);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = this.getDefaultState();

        return this.useFacing() ? state.with(FACING, ctx.getPlayerFacing().getOpposite()) : state;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        return blockEntity instanceof AbstractMachineBlockEntity ? ScreenHandler.calculateComparatorOutput(blockEntity) : 0;
    }
}