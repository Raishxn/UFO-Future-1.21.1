package com.raishxn.ufo.api.multiblock;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiblockTemplateCompilerTest {

    @Test
    void rejectsEmptyAndIrregularTemplates() {
        assertThrows(IllegalArgumentException.class, () -> validate(List.of(), Set.of('C'), true));
        assertThrows(IllegalArgumentException.class, () -> validate(
                List.<String[]>of(new String[]{"C", "XX"}), Set.of('C', 'X'), true));
        assertThrows(IllegalArgumentException.class, () -> validate(
                List.of(new String[]{"C"}, new String[]{"X", "X"}), Set.of('C', 'X'), true));
    }

    @Test
    void requiresExactlyOneController() {
        assertThrows(IllegalArgumentException.class, () -> validate(
                List.<String[]>of(new String[]{"XX"}), Set.of('X'), true));
        assertThrows(IllegalArgumentException.class, () -> validate(
                List.<String[]>of(new String[]{"CC"}), Set.of('C'), true));
    }

    @Test
    void strictModeRejectsUndeclaredSymbolsWhileLegacyModeKeepsCompatibility() {
        List<String[]> layers = List.<String[]>of(new String[]{"CX"});
        assertThrows(IllegalArgumentException.class, () -> validate(layers, Set.of('C'), true));
        assertDoesNotThrow(() -> validate(layers, Set.of('C'), false));
    }

    @Test
    void compilesDimensionsSymbolsAndControllerAnchor() {
        var summary = validate(List.of(
                new String[]{"XXX", "XCX"},
                new String[]{"AAA", "AAA"}), Set.of('A', 'C', 'X'), true);
        assertEquals(3, summary.width());
        assertEquals(2, summary.height());
        assertEquals(2, summary.depth());
        assertEquals(1, summary.controllerX());
        assertEquals(0, summary.controllerY());
        assertEquals(1, summary.controllerZ());
        assertEquals(Set.of('A', 'C', 'X'), summary.symbols());
    }

    @Test
    void rotatesOffsetsForEveryHorizontalFacing() {
        assertEquals(new MultiblockTemplateCompiler.Offset(2, 3, 4), rotate(MultiblockTemplateCompiler.HorizontalFacing.NORTH));
        assertEquals(new MultiblockTemplateCompiler.Offset(-2, 3, -4), rotate(MultiblockTemplateCompiler.HorizontalFacing.SOUTH));
        assertEquals(new MultiblockTemplateCompiler.Offset(-4, 3, 2), rotate(MultiblockTemplateCompiler.HorizontalFacing.EAST));
        assertEquals(new MultiblockTemplateCompiler.Offset(4, 3, -2), rotate(MultiblockTemplateCompiler.HorizontalFacing.WEST));
    }

    private static MultiblockTemplateCompiler.Summary validate(List<String[]> layers, Set<Character> symbols,
                                                                 boolean strict) {
        return MultiblockTemplateCompiler.validate(layers, 'C', symbols, strict);
    }

    private static MultiblockTemplateCompiler.Offset rotate(MultiblockTemplateCompiler.HorizontalFacing facing) {
        return MultiblockTemplateCompiler.rotate(2, 3, 4, facing);
    }
}
