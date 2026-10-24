package com.piotrek.galeon.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Milestone 1: persistent stationary museum-quality exterior.
 * Movement, deck walking/collisions, cannon interactions and sails physics follow later.
 */
public final class GaleonEntity extends Entity {
    public GaleonEntity(EntityType<? extends GaleonEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }
    @Override protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 3);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(ValueInput input) {}
    @Override protected void addAdditionalSaveData(ValueOutput output) {}
    @Override public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
    }
}
