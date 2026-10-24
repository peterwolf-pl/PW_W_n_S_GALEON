package com.piotrek.galeon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.piotrek.galeon.GaleonMod;
import com.piotrek.galeon.entity.GaleonEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

public final class GaleonRenderer extends EntityRenderer<GaleonEntity, GaleonRenderState> {
    public static final Identifier TEXTURE = GaleonMod.id("textures/entity/galeon.png");
    private final GaleonModel model;
    public GaleonRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new GaleonModel(context.bakeLayer(GaleonClient.GALEON_LAYER));
        shadowRadius = 4.0F;
    }
    @Override protected boolean affectedByCulling(GaleonEntity entity) { return false; }
    @Override protected AABB getBoundingBoxForCulling(GaleonEntity entity, float delta) {
        return entity.getBoundingBox().inflate(20.0, 4.0, 24.0).expandTowards(0.0, 52.0, 0.0);
    }
    @Override public GaleonRenderState createRenderState() { return new GaleonRenderState(); }
    @Override public void extractRenderState(GaleonEntity entity, GaleonRenderState state, float delta) {
        super.extractRenderState(entity, state, delta);
        state.yaw = entity.getYRot();
    }
    @Override public void submit(GaleonRenderState state, PoseStack stack,
                                SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, stack, collector, camera);
        stack.pushPose();
        stack.rotateDegrees(Axis.YP, 180.0F-state.yaw);
        stack.scale(-1.0F,-1.0F,1.0F);
        stack.translate(0.0F,-1.5F,0.0F);
        model.setupAnim(state);
        collector.submitModel(model,state,stack,RenderTypes.entityCutout(TEXTURE),
                state.lightCoords,OverlayTexture.NO_OVERLAY,state.outlineColor);
        stack.popPose();
    }
}
