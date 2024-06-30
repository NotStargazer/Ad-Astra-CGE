package earth.terrarium.ad_astra.mixin;

import earth.terrarium.ad_astra.common.registry.ModTags;
import earth.terrarium.ad_astra.common.util.ModUtils;
import earth.terrarium.ad_astra.common.util.OxygenUtils;
import earth.terrarium.botarium.api.registry.fluid.BotariumLiquidBlock;
import earth.terrarium.botarium.api.registry.fluid.BotariumSourceFluid;
import earth.terrarium.botarium.api.registry.fluid.FluidProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {

    @Inject(method = "onPlace", at = @At("TAIL"))
    private void ad_astra$onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving, CallbackInfo ci) {
        if (ModUtils.getWorldTemperature(level) < 0 && !OxygenUtils.posHasOxygen(level, pos) && !state.is(ModTags.EXCLUDE_FREEZES_IN_SPACE)) {
            for (var dir : Direction.values()) {
                if (OxygenUtils.posHasOxygen(level, pos.relative(dir))) return;
            }
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        }
    }
}
