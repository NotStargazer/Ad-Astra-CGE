package earth.terrarium.ad_astra.mixin;

import earth.terrarium.ad_astra.AdAstra;
import earth.terrarium.ad_astra.util.OxygenUtils;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "onPlaced", at = @At("HEAD"))
    public void adastra_onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        Block block = (Block) (Object) this;
        if (block instanceof PlantBlock || block instanceof CactusBlock) {
            if (!Registry.BLOCK.getId(block).getNamespace().equals(AdAstra.MOD_ID)) {
                if (!OxygenUtils.posHasOxygen(world, pos)) {
                    world.breakBlock(pos, true);
                }
            }
        }

        if (block instanceof GrassBlock) {
            if (!OxygenUtils.posHasOxygen(world, pos)) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }
        }

        if (block instanceof LeavesBlock) {
            if (!OxygenUtils.posHasOxygen(world, pos)) {
                world.breakBlock(pos, false);
            }
        }

        if (block instanceof VineBlock) {
            if (!OxygenUtils.posHasOxygen(world, pos)) {
                world.breakBlock(pos, false);
            }
        }
    }
}