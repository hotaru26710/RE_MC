package org.hotaru.re_mc.deathreturn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.hotaru.re_mc.deathreturn.spirit.SpiritService;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public final class UnseenHandItem extends Item {
    private static final float SPIRIT_COST = 8.0F;
    private static final float SCENT_GAIN = 3.0F;
    private static final double RANGE = 22.0D;
    private static final int COOLDOWN_TICKS = 60;

    public UnseenHandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        LivingEntity target = findTarget(serverPlayer);
        if (target == null) {
            serverPlayer.displayClientMessage(Component.translatable("message.re_mc.unseen_hand.no_target").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.success(stack);
        }
        if (!SpiritService.hasSpirit(serverPlayer, 30.0F) || !SpiritService.spendSpirit(serverPlayer, SPIRIT_COST, SCENT_GAIN)) {
            serverPlayer.displayClientMessage(Component.translatable("message.re_mc.unseen_hand.no_spirit").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.success(stack);
        }

        Vec3 pull = serverPlayer.position().subtract(target.position());
        if (pull.lengthSqr() > 1.0E-4D) {
            pull = pull.normalize().scale(1.25D).add(0.0D, 0.45D, 0.0D);
            target.setDeltaMovement(pull);
            target.hurtMarked = true;
        }
        target.hurt(serverPlayer.damageSources().playerAttack(serverPlayer), 4.0F);
        ServerLevel serverLevel = serverPlayer.serverLevel();
        serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 0.65F);
        serverLevel.sendParticles(serverPlayer, ParticleTypes.PORTAL, false, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.02D);
        serverLevel.sendParticles(serverPlayer, ParticleTypes.SCULK_SOUL, false, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 6, 0.25D, 0.35D, 0.25D, 0.01D);
        serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return InteractionResultHolder.success(stack);
    }

    private static LivingEntity findTarget(ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(RANGE));
        HitResult hitResult = player.pick(RANGE, 0.0F, false);
        if (hitResult instanceof BlockHitResult blockHit) {
            end = blockHit.getLocation();
        }

        AABB search = player.getBoundingBox().inflate(RANGE);
        List<LivingEntity> candidates = player.serverLevel().getEntitiesOfClass(LivingEntity.class, search,
                entity -> entity != player && entity instanceof Enemy && entity.isAlive() && !entity.isSpectator());
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.35D).clip(start, end);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(hit.get());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.re_mc.unseen_hand").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.re_mc.unseen_hand.cost").withStyle(ChatFormatting.GRAY));
    }
}
