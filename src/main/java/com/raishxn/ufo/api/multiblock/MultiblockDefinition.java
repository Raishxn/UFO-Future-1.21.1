package com.raishxn.ufo.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Immutable, validated source of truth for a 3.0 multiblock structure. */
public record MultiblockDefinition(
        ResourceLocation id,
        int schemaVersion,
        Component name,
        MultiblockPattern pattern,
        Map<Character, BlockState> defaultCreativeStates,
        Set<Direction> allowedFacings,
        Map<Character, MultiblockCellRole> roles) {

    private static final Set<Direction> HORIZONTAL_FACINGS = Set.copyOf(EnumSet.of(
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));

    public MultiblockDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(pattern, "pattern");
        if (schemaVersion < 1) throw new IllegalArgumentException("schemaVersion must be positive");
        defaultCreativeStates = Map.copyOf(defaultCreativeStates);
        allowedFacings = Set.copyOf(allowedFacings);
        roles = Map.copyOf(roles);
        if (allowedFacings.isEmpty() || allowedFacings.stream().anyMatch(direction -> !direction.getAxis().isHorizontal())) {
            throw new IllegalArgumentException("allowedFacings must contain horizontal directions only");
        }
        Set<Character> missingRoles = new LinkedHashSet<>(pattern.getSymbols());
        missingRoles.removeAll(roles.keySet());
        if (!missingRoles.isEmpty()) throw new IllegalArgumentException("Pattern symbols without roles: " + missingRoles);
        if (roles.get(pattern.getControllerChar()) != MultiblockCellRole.CONTROLLER) {
            throw new IllegalArgumentException("Controller symbol must use CONTROLLER role");
        }
    }

    public MultiblockPattern.MatchResult scan(Level level, BlockPos controllerPos, Direction facing,
                                               MultiblockScanMode mode) {
        if (!allowedFacings.contains(facing)) {
            MultiblockPattern.PatternError error = new MultiblockPattern.PatternError(
                    controllerPos, Component.literal("Horizontal controller facing"));
            return new MultiblockPattern.MatchResult(false, List.of(), Optional.of(error), List.of(error), false);
        }
        return mode == MultiblockScanMode.FAST
                ? pattern.matchFast(level, controllerPos, facing)
                : pattern.match(level, controllerPos, facing);
    }

    public static MultiblockDefinition legacy(ResourceLocation id, Component name, MultiblockPattern pattern,
                                               Map<Character, BlockState> defaultCreativeStates) {
        Map<Character, MultiblockCellRole> inferredRoles = new LinkedHashMap<>();
        for (char symbol : pattern.getSymbols()) {
            inferredRoles.put(symbol, symbol == pattern.getControllerChar()
                    ? MultiblockCellRole.CONTROLLER
                    : symbol == ' ' ? MultiblockCellRole.IGNORED : MultiblockCellRole.STRUCTURE);
        }
        return new MultiblockDefinition(id, 1, name, pattern, defaultCreativeStates,
                HORIZONTAL_FACINGS, inferredRoles);
    }

    public static Set<Direction> horizontalFacings() {
        return HORIZONTAL_FACINGS;
    }
}
