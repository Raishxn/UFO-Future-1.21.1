package com.raishxn.ufo.block.entity.pattern;

import com.raishxn.ufo.api.multiblock.MultiblockPattern;
import com.raishxn.ufo.block.MultiblockBlocks;
import net.minecraft.world.level.block.Block;

public class StellarNexusPatternFactory {

    /**
     * GTCEu Eye of Harmony pattern raw aisles.
     * Dimensions: 17 slices deep (Z), each slice is 33 rows high (Y, Top to Bottom), 33 cols wide (X).
     */
    private static final String[][] EOH_RAW_AISLES = new String[][] {
            // Aisle 0
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "            AAAAAAAAA            ", "               A A               ", "            AAAAAAAAA            ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 1
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "               A A               ", "              DDDDD              ", "             DDADADD             ", "         AAAADAADAADAAAA         ", "             DDDDDDD             ", "         AAAADAADAADAAAA         ", "             DDADADD             ", "              DDDDD              ", "               A A               ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 2
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "               A A               ", "                D                ", "                D                ", "             DDDDDDD             ", "            DD     DD            ", "            D  EEE  D            ", "       AAA  D EFFFE D  AAA       ", "          DDD EFFFE DDD          ", "       AAA  D EFFFE D  AAA       ", "            D  EEE  D            ", "            DD     DD            ", "             DDDDDDD             ", "                D                ", "                D                ", "               A A               ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 3
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "      AA                 AA      ", "        DD             DD        ", "      AA                 AA      ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 4
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "              AAAAA              ", "                D                ", "                F                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "      A                   A      ", "     AA                   AA     ", "      ADFF             FFDA      ", "     AA                   AA     ", "      A                   A      ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                F                ", "                D                ", "              AAAAA              ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 5
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "             GEEFEEG             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "       G                 G       ", "       E                 E       ", "    AA E                 E AA    ", "      DF                 FD      ", "    AA E                 E AA    ", "       E                 E       ", "       G                 G       ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             GEEFEEG             ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 6
            new String[]{
                    "                                 ", "                                 ", "                                 ", "               A A               ", "              AAAAA              ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "    A                       A    ", "   AA                       AA   ", "    ADF                   FDA    ", "   AA                       AA   ", "    A                       A    ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "              AAAAA              ", "               A A               ", "                                 ", "                                 ", "                                 "
            },
            // Aisle 7
            new String[]{
                    "                                 ", "                                 ", "               A A               ", "               A A               ", "                D                ", "             GEEFEEG             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "     G                     G     ", "     E                     E     ", "  AA E                     E AA  ", "    DF                     FD    ", "  AA E                     E AA  ", "     E                     E     ", "     G                     G     ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             GEEFEEG             ", "                D                ", "               A A               ", "               A A               ", "                                 ", "                                 "
            },
            // Aisle 8
            new String[]{
                    "                                 ", "                                 ", "               A A               ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  A                           A  ", "   DF                       FD   ", "  A                           A  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "               A A               ", "                                 ", "                                 "
            },
            // Aisle 9
            new String[]{
                    "                                 ", "               A A               ", "               A A               ", "                D                ", "                F                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " AA                           AA ", "   DF                       FD   ", " AA                           AA ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                F                ", "                D                ", "               A A               ", "               A A               ", "                                 "
            },
            // Aisle 10
            new String[]{
                    "                                 ", "               A A               ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " A                             A ", "  D                           D  ", " A                             A ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "               A A               ", "                                 "
            },
            // Aisle 11
            new String[]{
                    "                                 ", "               A A               ", "                D                ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " A                             A ", "  D                           D  ", " A                             A ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                D                ", "               A A               ", "                                 "
            },
            // Aisle 12
            new String[]{
                    "               A A               ", "               A A               ", "             DDDDDDD             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", "  D                           D  ", "AAD                           DAA", "  D                           D  ", "AAD                           DAA", "  D                           D  ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             DDDDDDD             ", "               A A               ", "               A A               "
            },
            // Aisle 13
            new String[]{
                    "               A A               ", "              DDDDD              ", "            DD     DD            ", "                                 ", "                                 ", "       G                 G       ", "                                 ", "     G                     G     ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", "  D                           D  ", " D                             D ", "AD                             DA", " D                             D ", "AD                             DA", " D                             D ", "  D                           D  ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "     G                     G     ", "                                 ", "       G                 G       ", "                                 ", "                                 ", "            DD     DD            ", "              DDDDD              ", "               A A               "
            },
            // Aisle 14
            new String[]{
                    "               A A               ", "             DDADADD             ", "            D  EEE  D            ", "                                 ", "      A                   A      ", "       E                 E       ", "    A                       A    ", "     E                     E     ", "                                 ", "                                 ", "                                 ", "                                 ", "  D                           D  ", " D                             D ", " D                             D ", "AAE                           EAA", " DE                           ED ", "AAE                           EAA", " D                             D ", " D                             D ", "  D                           D  ", "                                 ", "                                 ", "                                 ", "                                 ", "     E                     E     ", "    A                       A    ", "       E                 E       ", "      A                   A      ", "                                 ", "            D  EEE  D            ", "             DDADADD             ", "               A A               "
            },
            // Aisle 15
            new String[]{
                    "            AAAAAAAAA            ", "         AAAADAADAADAAAA         ", "       AAA  D EFFFE D  AAA       ", "      AA                 AA      ", "     AA                   AA     ", "    AA E                 E AA    ", "   AA                       AA   ", "  AA E                     E AA  ", "  A                           A  ", " AA                           AA ", " A                             A ", " A                             A ", "AAD                           DAA", "AD                             DA", "AAE                           EAA", "AAF                           FAA", "ADF                           FDA", "AAF                           FAA", "AAE                           EAA", "AD                             DA", "AAD                           DAA", " A                             A ", " A                             A ", " AA                           AA ", "  A                           A  ", "  AA E                     E AA  ", "   AA                       AA   ", "    AA E                 E AA    ", "     AA                   AA     ", "      AA                 AA      ", "       AAA  D EFFFE D  AAA       ", "         AAAADAADAADAAAA         ", "            AAAAAAAAA            "
            },
            // Aisle 16 - Front pane
            new String[]{
                    "               A A               ", "             DDDDDDD             ", "          DDD EFFFE DDD          ", "        DD             DD        ", "      ADFF             FFDA      ", "      DF                 FD      ", "    ADF                   FDA    ", "    DF                     FD    ", "   DF                       FD   ", "   DF                       FD   ", "  D                           D  ", "  D                           D  ", "  D                           D  ", " D                             D ", " DE                           ED ", "ADF                           FDA", " DF                           FD ", "ADF                           FDA", " DE                           ED ", " D                             D ", "  D                           D  ", "  D                           D  ", "  D                           D  ", "   DF                       FD   ", "   DF                       FD   ", "    DF                     FD    ", "    ADF                   FDA    ", "      DF                 FD      ", "      ADFF             FFDA      ", "        DD             DD        ", "          DDD EFFFE DDD          ", "             DDDDDDD             ", "               A A               "
            },
            // Aisle 17 - Controller pane
            new String[]{
                    "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "             AAAAAAA             ", "            AABBBBBAA            ", "            ABBBBBBBA            ", "            ABBAAABBA            ", "            ABBA~ABBA            ", "            ABBAAABBA            ", "            ABBBBBBBA            ", "            AABBBBBAA            ", "             AAAAAAA             ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 "
            }
    };

    /**
     * Translates the Eye of Harmony GTCEu Pattern (Z, Y_TopToBottom, X) 
     * into the Ufo MultiblockPattern (Y_BottomToTop, Z, X).
     */
    public static MultiblockPattern getPattern() {
        // Z-axis layers (18 slices deep total)
        int depthSize = EOH_RAW_AISLES.length;
        // Y-axis height (33 rows)
        int heightSize = EOH_RAW_AISLES[0].length;
        
        MultiblockPattern.Builder builder = new MultiblockPattern.Builder()
                .controllerChar('~');
                
        // MultiblockPattern builds layer by layer from the bottom up.
        // EOH_RAW_AISLES has strings ordered from top (index 0) to bottom (index 32).
        for (int y = 0; y < heightSize; y++) { // y is the index of layer starting from bottom
            int gtY = (heightSize - 1) - y; // The index in the GT array (0 is top, 32 is bottom)
            
            String[] layerStrLines = new String[depthSize];
            for (int z = 0; z < depthSize; z++) { // z represents the aisle array (depth)
                layerStrLines[z] = EOH_RAW_AISLES[z][gtY];
            }
            builder.layer(layerStrLines);
        }

        // Apply predicates according to user request mapping
        builder.where('A', (state, level, pos) -> {
            Block block = state.getBlock();
            return block == MultiblockBlocks.ENTROPY_SINGULARITY_CASING.get();
        });
        builder.where('B', (state, level, pos) -> {
            Block block = state.getBlock();
            return block == MultiblockBlocks.ENTROPY_ASSEMBLER_CORE_CASING.get()
                || block == MultiblockBlocks.ME_MASSIVE_OUTPUT_HATCH.get()
                || block == MultiblockBlocks.ME_MASSIVE_FLUID_HATCH.get();
        });
        builder.where('D', (state, level, pos) -> {
            Block block = state.getBlock();
            return block == MultiblockBlocks.ENTROPY_COOLANT_MATRIX_COMPONENTS.get();
        });
        builder.where('E', (state, level, pos) -> {
            Block block = state.getBlock();
            return block == MultiblockBlocks.ENTROPY_CATALYST_BANK_COMPONENTS.get();
        });
        builder.where('F', (state, level, pos) -> {
            Block block = state.getBlock();
            return block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T1.get()
                || block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T2.get()
                || block == MultiblockBlocks.STELLAR_FIELD_GENERATOR_T3.get();
        });
        builder.where('G', (state, level, pos) -> {
            Block block = state.getBlock();
            return block == MultiblockBlocks.ENTROPY_COMPUTER_CONDENSATION_MATRIX.get();
        });
        builder.where(' ', (state, level, pos) -> state.isAir());

        return builder.build();
    }
}
