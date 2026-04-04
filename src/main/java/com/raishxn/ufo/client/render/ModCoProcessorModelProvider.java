package com.raishxn.ufo.client.render;

import appeng.client.render.crafting.AbstractCraftingUnitModelProvider;
import appeng.client.render.crafting.LightBakedModel;
import com.raishxn.ufo.UfoMod;
import com.raishxn.ufo.core.MegaCoProcessorTier; // Mude para o Enum do Co-Processor
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

// Mude a classe para usar MegaCoProcessorTier
public class ModCoProcessorModelProvider extends AbstractCraftingUnitModelProvider<MegaCoProcessorTier> {
    private static final List<Material> MATERIALS = new ArrayList<>();
    public static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());

    // Texturas base (podem ser as mesmas do storage)
    protected static final Material RING_CORNER = texture("ring_corner");
    protected static final Material RING_SIDE_HOR = texture("ring_side_hor");
    protected static final Material RING_SIDE_VER = texture("ring_side_ver");
    protected static final Material LIGHT_BASE = texture("light_base");

    // Novas texturas de luz para os co-processadores
    // IMPORTANTE: Você precisará criar essas texturas em 'assets/ufo/textures/block/'
    protected static final Material COPROCESSOR_50M_LIGHT = texture("50m_mega_co_processor_light");
    protected static final Material COPROCESSOR_150M_LIGHT = texture("150m_mega_co_processor_light");
    protected static final Material COPROCESSOR_300M_LIGHT = texture("300m_mega_co_processor_light");
    protected static final Material COPROCESSOR_750M_LIGHT = texture("750m_mega_co_processor_light");
    protected static final Material COPROCESSOR_2B_LIGHT = texture("2b_mega_co_processor_light");

    public ModCoProcessorModelProvider(MegaCoProcessorTier type) {
        super(type);
    }

    @Override
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(MATERIALS);
    }

    private TextureAtlasSprite getLightMaterial(Function<Material, TextureAtlasSprite> textureGetter) {
        // Switch para os tiers de co-processador
        return switch (type) {
            case COPROCESSOR_50M -> textureGetter.apply(COPROCESSOR_50M_LIGHT);
            case COPROCESSOR_150M -> textureGetter.apply(COPROCESSOR_150M_LIGHT);
            case COPROCESSOR_300M -> textureGetter.apply(COPROCESSOR_300M_LIGHT);
            case COPROCESSOR_750M -> textureGetter.apply(COPROCESSOR_750M_LIGHT);
            case COPROCESSOR_2B -> textureGetter.apply(COPROCESSOR_2B_LIGHT);
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
        // Linha corrigida
        var material = new Material(InventoryMenu.BLOCK_ATLAS, UfoMod.id("block/" + name));
        if (!MATERIALS.contains(material)) {
            MATERIALS.add(material);
        }
        return material;
    }
}