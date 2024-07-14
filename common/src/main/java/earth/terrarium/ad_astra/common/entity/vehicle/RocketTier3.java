package earth.terrarium.ad_astra.common.entity.vehicle;

import earth.terrarium.ad_astra.common.registry.ModEntityTypes;
import earth.terrarium.ad_astra.common.registry.ModItems;
import earth.terrarium.ad_astra.common.registry.ModParticleTypes;
import earth.terrarium.ad_astra.common.registry.ModTags;
import earth.terrarium.ad_astra.common.util.ModUtils;
import earth.terrarium.botarium.api.fluid.FluidHolder;
import earth.terrarium.botarium.api.fluid.FluidHooks;
import earth.terrarium.botarium.api.fluid.PlatformFluidItemHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class RocketTier3 extends Rocket
{

    public RocketTier3(Level level)
    {
        super(ModEntityTypes.TIER_3_ROCKET.get(), level, 3);
    }

    public RocketTier3(EntityType<?> type, Level level)
    {
        super(type, level, 3);
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
                if (fluidState.is(ModTags.FUEL_TIER_3) || fluidState.is(ModTags.FUEL_TIER_4))
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
        return ModItems.TIER_3_ROCKET.get().getDefaultInstance();
    }

    @Override
    public void spawnAfterburnerParticles()
    {
        super.spawnAfterburnerParticles();
        if (this.level instanceof ServerLevel serverWorld)
        {
            Vec3 pos = this.position();

            float xRotator = Mth.cos(this.getYRot() * ((float) Math.PI / 180.0f)) * 0.98f;
            float zRotator = Mth.sin(this.getYRot() * ((float) Math.PI / 180.0f)) * 0.98f;

            ModUtils.spawnForcedParticles(serverWorld, ModParticleTypes.SMALL_FLAME.get(), pos.x() + xRotator, pos.y() + 0.35, pos.z() + zRotator, 20, 0.1, 0.1, 0.1, 0.001);
            ModUtils.spawnForcedParticles(serverWorld, ModParticleTypes.SMALL_SMOKE.get(), pos.x() + xRotator, pos.y() + 0.35, pos.z() + zRotator, 10, 0.1, 0.1, 0.1, 0.04);

            ModUtils.spawnForcedParticles(serverWorld, ModParticleTypes.SMALL_FLAME.get(), pos.x() - xRotator, pos.y() + 0.35, pos.z() - zRotator, 20, 0.1, 0.1, 0.1, 0.001);
            ModUtils.spawnForcedParticles(serverWorld, ModParticleTypes.SMALL_SMOKE.get(), pos.x() - xRotator, pos.y() + 0.35, pos.z() - zRotator, 10, 0.1, 0.1, 0.1, 0.04);
        }
    }
}