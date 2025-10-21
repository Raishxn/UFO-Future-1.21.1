package com.raishxn.ufo.client.render;

import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import appeng.client.render.crafting.LightBakedModel;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCraftingStorageTier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ModCraftingStorageModelProvider extends AbstractCraftingUnitModelProvider<MegaCraftingStorageTier> {
    private static final List<Material> MATERIALS = new ArrayList<>();
    public static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());

    // Carrega texturas diretamente de 'assets/ufo/textures/block/'
    protected static final Material RING_CORNER = texture("ring_corner");
    protected static final Material RING_SIDE_HOR = texture("ring_side_hor");
    protected static final Material RING_SIDE_VER = texture("ring_side_ver");
    protected static final Material LIGHT_BASE = texture("light_base");
    protected static final Material STORAGE_1B_LIGHT = texture("1b_mega_crafting_storage_light");
    protected static final Material STORAGE_50B_LIGHT = texture("50b_mega_crafting_storage_light");
    protected static final Material STORAGE_1T_LIGHT = texture("1t_mega_crafting_storage_light");
    protected static final Material STORAGE_250T_LIGHT = texture("250t_mega_crafting_storage_light");
    protected static final Material STORAGE_1QD_LIGHT = texture("1qd_mega_crafting_storage_light");

    public ModCraftingStorageModelProvider(MegaCraftingStorageTier type) {
        super(type);
    }

    @Override
    public List<Material> getMaterials() {
        // Garante que a textura de luz para este tier específico seja registrada
        return Collections.unmodifiableList(MATERIALS);
    }

    private TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter) {
        return switch (type) {
            case STORAGE_1B -> textureGetter.apply(STORAGE_1B_LIGHT);
            case STORAGE_50B -> textureGetter.apply(STORAGE_50B_LIGHT);
            case STORAGE_1T -> textureGetter.apply(STORAGE_1T_LIGHT);
            case STORAGE_250T -> textureGetter.apply(STORAGE_250T_LIGHT);
            case STORAGE_1QD -> textureGetter.apply(STORAGE_1QD_LIGHT);
        };
    }

    @Override
    public BakedModel getBakedModel(Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite ringCorner = spriteGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = spriteGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = spriteGetter.apply(RING_SIDE_VER);
        return new LightBakedModel(ringCorner, ringSideHor, ringSideVer, spriteGetter.apply(LIGHT_BASE), this.getLightMaterial(spriteGetter)) {
            @Override
            @NotNull
            public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
                return CUTOUT;
            }
        };
    }

    private static Material texture(String name) {
        var material = new Material(InventoryMenu.BLOCK_ATLAS, UfoMod.asResource("block/" + name));
        if (!MATERIALS.contains(material)) {
            MATERIALS.add(material);
        }
        return material;
    }
}