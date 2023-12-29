package earth.terrarium.adastra.common.items.armor;

import earth.terrarium.adastra.common.constants.ConstantComponents;
import earth.terrarium.adastra.common.registry.ModFluids;
import earth.terrarium.adastra.common.utils.FluidUtils;
import earth.terrarium.adastra.common.utils.KeybindManager;
import earth.terrarium.adastra.common.utils.TooltipUtils;
import earth.terrarium.adastra.mixins.common.LivingEntityAccessor;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyItem;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.common.energy.impl.WrappedItemEnergyContainer;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JetSuitItem extends SpaceSuitItem implements BotariumEnergyItem<WrappedItemEnergyContainer> {
    private final long energyCapacity;

    public JetSuitItem(ArmorMaterial material, Type type, int buckets, int energy, Properties properties) {
        super(material, type, buckets, properties);
        this.energyCapacity = energy;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(TooltipUtils.getFluidComponent(
            FluidUtils.getTank(stack),
            FluidHooks.buckets(tankSize),
            ModFluids.OXYGEN.get()));
        var energy = getEnergyStorage(stack);
        tooltipComponents.add(TooltipUtils.getEnergyComponent(energy.getStoredEnergy(), energyCapacity));
        tooltipComponents.add(TooltipUtils.getMaxEnergyInComponent(energy.maxInsert()));
        TooltipUtils.addDescriptionComponent(tooltipComponents, ConstantComponents.JET_SUIT_INFO);
    }

    @Override
    public WrappedItemEnergyContainer getEnergyStorage(ItemStack holder) {
        return new WrappedItemEnergyContainer(
            holder,
            new SimpleEnergyContainer(energyCapacity) {
                @Override
                public long maxInsert() {
                    return 1000;
                }
            });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) return;
        if (player.getItemBySlot(EquipmentSlot.CHEST) != stack) return;

        if (player.getAbilities().flying) return;
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        if (!hasFullJetSuitSet(player)) return;

        if (!KeybindManager.suitFlightEnabled(player)) return;
        if (!isJumping(player)) return;
        if (!canFly(player, stack)) return;

        if (player.isSprinting()) {
            fullFlight(player);
            consume(player, stack, 100);
        } else {
            upwardsFlight(player);
            consume(player, stack, 50);
        }
    }

    protected void upwardsFlight(Player player) {
        double acceleration = sigmoidAcceleration(player.tickCount, 5.0, 1.0, 2.0);
        acceleration /= 25.0f;
        player.addDeltaMovement(new Vec3(0, Math.max(0.0025, acceleration), 0));
        player.fallDistance = Math.max(player.fallDistance / 1.5f, 0.0f);
    }

    protected void fullFlight(Player player) {
        Vec3 movement = player.getLookAngle().normalize().scale(0.075);
        if (player.getDeltaMovement().length() > 2.0) return;
        player.addDeltaMovement(movement);
        player.fallDistance = Math.max(player.fallDistance / 1.5f, 0.0f);
        if (!player.isFallFlying()) {
            player.startFallFlying();
        }
    }

    private boolean isJumping(Player player) {
        return ((LivingEntityAccessor) player).isJumping();
    }

    private boolean canFly(Player player, ItemStack stack) {
        return player.isCreative() || getEnergyStorage(stack).getStoredEnergy() > 0;
    }

    private void consume(Player player, ItemStack stack, int amount) {
        if (player.isCreative() || player.level().isClientSide()) return;
        getEnergyStorage(stack).internalExtract(amount, false);
    }

    protected boolean isFullFlightEnabled(Player player) {
        return KeybindManager.suitFlightEnabled(player) && isJumping(player) && player.isSprinting();
    }

    public static double sigmoidAcceleration(double t, double peakTime, double peakAcceleration, double initialAcceleration) {
        return ((2 * peakAcceleration) / (1 + Math.exp(-t / peakTime)) - peakAcceleration) + initialAcceleration;
    }

    public void spawnParticles(Level level, LivingEntity entity, HumanoidModel<?> model, ItemStack stack) {
        if (!(entity instanceof Player player)) return;
        if (!canFly(player, stack)) return;
        if (!hasFullJetSuitSet(player)) return;
        if (!KeybindManager.suitFlightEnabled(player)) return;
        if (!isJumping(player) || (!isJumping(player) && !player.isSprinting()))
            return;

        spawnParticles(level, entity, model.rightArm.xRot + 0.05, entity.isFallFlying() ? 0.0 : 0.8, -0.45);
        spawnParticles(level, entity, model.leftArm.xRot + 0.05, entity.isFallFlying() ? 0.0 : 0.8, 0.45);
        spawnParticles(level, entity, model.rightLeg.xRot + 0.05, entity.isFallFlying() ? 0.1 : 0.0, -0.1);
        spawnParticles(level, entity, model.leftLeg.xRot + 0.05, entity.isFallFlying() ? 0.1 : 0.0, 0.1);
    }

    // Spawns particles at the limbs of the player
    private void spawnParticles(Level level, LivingEntity entity, double pitch, double yOffset, double zOffset) {
        double yRot = entity.yBodyRot;
        double xRotator = Math.cos(yRot * Math.PI / 180.0) * zOffset;
        double zRotator = Math.sin(yRot * Math.PI / 180.0) * zOffset;
        double xRotator1 = Math.cos((yRot - 90) * Math.PI / 180.0) * pitch;
        double zRotator1 = Math.sin((yRot - 90) * Math.PI / 180.0) * pitch;

        level.addParticle(ParticleTypes.FLAME, true, entity.getX() + xRotator + xRotator1, entity.getY() + yOffset, entity.getZ() + zRotator1 + zRotator, 0.0, 0.0, 0.0);
    }

    @SuppressWarnings("unused") // Forge
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (entity.level().isClientSide()) return true;
        if (this.type != Type.CHESTPLATE) return true;
        int nextFlightTick = flightTicks + 1;
        if (nextFlightTick % 10 != 0) return true;

        if (nextFlightTick % 20 == 0) {
            stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.CHEST));
        }

        entity.gameEvent(GameEvent.ELYTRA_GLIDE);

        return true;
    }

    @SuppressWarnings("unused") // Forge
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return entity instanceof Player player && canFly(player, stack) && isFullFlightEnabled(player);
    }
}
