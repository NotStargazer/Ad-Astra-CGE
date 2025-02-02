package earth.terrarium.ad_astra.common.entity.vehicle;

import earth.terrarium.ad_astra.common.registry.ModEntityTypes;
import earth.terrarium.ad_astra.common.registry.ModItems;
import earth.terrarium.ad_astra.common.registry.ModTags;
import earth.terrarium.botarium.api.fluid.FluidHooks;
import earth.terrarium.botarium.api.fluid.PlatformFluidItemHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class RocketTier1 extends Rocket
{

    public RocketTier1(Level level)
    {
        super(ModEntityTypes.TIER_1_ROCKET.get(), level, 1);
    }

    public RocketTier1(EntityType<?> type, Level level)
    {
        super(type, level, 1);
    }

    @Override
    public void tryInsertingIntoTank()
    {
        if (!this.level.isClientSide)
        {
            ItemStack stack = this.getInventory().getItem(0);
            Optional<PlatformFluidItemHandler> possibleItemFluidContainer = FluidHooks.safeGetItemFluidManager(stack);
            if (possibleItemFluidContainer.isPresent())
            {
                PlatformFluidItemHandler itemFluidHandler = possibleItemFluidContainer.get();
                FluidState fluidState = itemFluidHandler.getFluidInTank(0).getFluid().defaultFluidState();
                if (fluidState.is(ModTags.FUEL_TIER_1) || fluidState.is(ModTags.FUEL_TIER_2)
                        || fluidState.is(ModTags.FUEL_TIER_3) || fluidState.is(ModTags.FUEL_TIER_4))
                {
                    super.tryInsertingIntoTank();
                }
            }
        }
    }

    @Override
    public double getPassengersRidingOffset()
    {
        return super.getPassengersRidingOffset() + 1.0f;
    }

    @Override
    public boolean shouldSit()
    {
        return false;
    }

    @Override
    public ItemStack getDropStack()
    {
        return ModItems.TIER_1_ROCKET.get().getDefaultInstance();
    }
}