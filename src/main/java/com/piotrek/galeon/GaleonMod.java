package com.piotrek.galeon;

import com.piotrek.galeon.entity.GaleonEntity;
import com.piotrek.galeon.item.GaleonItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GaleonMod implements ModInitializer {
    public static final String ID = "pw_wind_n_sails_galeon";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }

    private static final ResourceKey<EntityType<?>> ENTITY_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, id("galeon"));
    public static final EntityType<GaleonEntity> GALEON =
            Registry.register(BuiltInRegistries.ENTITY_TYPE, ENTITY_KEY,
                    EntityType.Builder.of(GaleonEntity::new, MobCategory.MISC)
                            .sized(9.5F, 8.5F).eyeHeight(5.0F)
                            .clientTrackingRange(48).build(ENTITY_KEY));

    private static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, id("galeon"));
    public static final GaleonItem GALEON_ITEM = Registry.register(
            BuiltInRegistries.ITEM, ITEM_KEY,
            new GaleonItem(new Item.Properties().setId(ITEM_KEY).stacksTo(1)));

    private static final CreativeModeTab TAB = FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pw_wind_n_sails_galeon.group"))
            .icon(GALEON_ITEM::getDefaultInstance)
            .displayItems((context, entries) -> entries.accept(GALEON_ITEM))
            .build();

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("group"), TAB);
        ResourceKey<CreativeModeTab> parent = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath("pw_wind_n_sails", "group"));
        CreativeModeTabEvents.modifyOutputEvent(parent).register(o -> o.accept(GALEON_ITEM));
        LOGGER.info("Peterwolf Galeon: scale 1:1, 36m hull x 9.5m beam. Static showcase model.");
    }
}
