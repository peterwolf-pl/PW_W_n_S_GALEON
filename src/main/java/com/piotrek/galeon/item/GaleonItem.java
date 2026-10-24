package com.piotrek.galeon.item;

import com.piotrek.galeon.GaleonMod;
import com.piotrek.galeon.entity.GaleonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public final class GaleonItem extends Item {
    public GaleonItem(Properties props) { super(props); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        BlockPos water = BlockPos.containing(hit.getLocation());
        if (!level.getFluidState(water).is(FluidTags.WATER)) water = water.above();
        if (!level.getFluidState(water).is(FluidTags.WATER)) return InteractionResult.PASS;
        // This checks the placement point. Full hull footprint clearance is not yet implemented.
        for (int d=0; d<5; d++) {
            if (!level.getFluidState(water.below(d)).is(FluidTags.WATER)) {
                if (!level.isClientSide())
                    player.displayClientMessage(
                            Component.translatable("message.pw_wind_n_sails_galeon.shallow"), true);
                return InteractionResult.FAIL;
            }
        }
        if (level instanceof ServerLevel server) {
            GaleonEntity entity = GaleonMod.GALEON.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
            if (entity == null) return InteractionResult.FAIL;
            entity.setPos(hit.getLocation().x, water.getY()+0.5, hit.getLocation().z);
            entity.setYRot(player.getYRot());
            if (!server.addFreshEntity(entity)) return InteractionResult.FAIL;
            ItemStack stack = player.getItemInHand(hand);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
