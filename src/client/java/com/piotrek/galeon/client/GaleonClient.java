package com.piotrek.galeon.client;

import com.piotrek.galeon.GaleonMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class GaleonClient implements ClientModInitializer {
    public static final ModelLayerLocation GALEON_LAYER =
            new ModelLayerLocation(GaleonMod.id("galeon"), "main");
    @Override public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(GALEON_LAYER, GaleonModel::createBodyLayer);
        EntityRendererRegistry.register(GaleonMod.GALEON, GaleonRenderer::new);
    }
}
