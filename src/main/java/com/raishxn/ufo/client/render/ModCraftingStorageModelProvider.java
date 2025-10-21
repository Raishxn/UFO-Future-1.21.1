package com.raishxn.ufo.client.render;

import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import appeng.client.render.crafting.LightBakedModel;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ModCraftingStorageModelProvider extends AbstractCraftingUnitModelProvider<MegaCraftingStorageTier> {
    private static final List<Material> MATERIALS = new ArrayList<>();

    // Carrega texturas diretamente de 'assets/ufo/textures/block/'
    protected static final Material RING_CORNER = texture("ring_corner");
    protected static final Material RING_SIDE_HOR = texture("ring_side_hor");
    protected static final Material RING_SIDE_VER = texture("ring_side_ver");
    protected static final Material LIGHT_BASE = texture("light_base");

    public ModCraftingStorageModelProvider(MegaCraftingStorageTier type) {
        super(type);
    }

    @Override
    public List<Material> getMaterials() {
        // Garante que a textura de luz para este tier específico seja registrada
        getLightMaterial();
        return Collections.unmodifiableList(MATERIALS);
    }

    private Material getLightMaterial() {
        return switch (this.type) {
            case STORAGE_1B -> texture("1b_mega_crafting_storage_light");
            case STORAGE_50B -> texture("50b_mega_crafting_storage_light");
            case STORAGE_1T -> texture("1t_mega_crafting_storage_light");
            case STORAGE_250T -> texture("250t_mega_crafting_storage_light");
            case STORAGE_1QD -> texture("1qd_mega_crafting_storage_light");
        };
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite ringCorner = spriteGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = spriteGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = spriteGetter.apply(RING_SIDE_VER);
        TextureAtlasSprite lightBase = spriteGetter.apply(LIGHT_BASE);
        TextureAtlasSprite light = spriteGetter.apply(getLightMaterial());

        return new LightBakedModel(ringCorner, ringSideHor, ringSideVer, lightBase, light);
    }

    private static Material texture(String name) {
        var material = new Material(InventoryMenu.BLOCK_ATLAS, UfoMod.asResource("block/" + name));
        if (!MATERIALS.contains(material)) {
            MATERIALS.add(material);
        }
        return material;
    }
}