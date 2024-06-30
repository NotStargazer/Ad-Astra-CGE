package earth.terrarium.ad_astra.common.entity.vehicle;

import earth.terrarium.ad_astra.common.registry.ModEntityTypes;
import earth.terrarium.ad_astra.common.registry.ModItems;
import earth.terrarium.ad_astra.common.registry.ModTags;
import earth.terrarium.botarium.api.fluid.FluidHolder;
import earth.terrarium.botarium.api.fluid.FluidHooks;
import earth.terrarium.botarium.api.fluid.PlatformFluidItemHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class RocketTier2 extends Rocket
{

    public RocketTier2(Level level)
    {
        super(ModEntityTypes.TIER_2_ROCKET.get(), level, 2);
    }

    public RocketTier2(EntityType<?> type, Level level)
    {
        super(type, level, 2);
    }

    @Override
    public void tryInsertingIntoTank()
    {
        if (this.level.isClientSide)
        {
            ItemStack stack = this.getInventory().getItem(0);
            Optional<PlatformFluidItemHandler> possibleItemFluidContainer = FluidHooks.safeGetItemFluidManager(stack);
            if (possibleItemFluidContainer.isPresent())
            {
                PlatformFluidItemHandler itemFluidHandler = possibleItemFluidContainer.get();
                FluidHolder itemHolder = itemFluidHandler.getFluidInTank(0);
                if (
                        itemHolder.getFluid().is(ModTags.FUEL_TIER_2) ||
                        itemHolder.getFluid().is(ModTags.FUEL_TIER_3) ||
                        itemHolder.getFluid().is(ModTags.FUEL_TIER_4)) {
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
        return ModItems.TIER_2_ROCKET.get().getDefaultInstance();
    }
}