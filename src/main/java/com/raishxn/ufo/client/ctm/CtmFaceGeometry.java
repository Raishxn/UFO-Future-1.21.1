package com.raishxn.ufo.client.ctm;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.joml.Vector3f;

import java.util.EnumMap;

/** Builds correctly oriented face quadrants for the compact CTM sheet. */
final class CtmFaceGeometry {
    static final int UP = 0;
    static final int RIGHT = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;

    private static final EnumMap<Direction, Vector3f[]> CORNERS = buildCorners();
    private static final EnumMap<Direction, Direction[]> NEIGHBOURS = buildNeighbours();

    private CtmFaceGeometry() {
    }

    static Direction neighbourDirection(Direction face, int edge) {
        return NEIGHBOURS.get(face)[edge];
    }

    static BlockPos cornerPosition(BlockPos pos, Direction face, CtmTileSelector.Quadrant quadrant) {
        return switch (quadrant) {
            case TOP_LEFT -> pos.relative(neighbourDirection(face, UP)).relative(neighbourDirection(face, LEFT));
            case TOP_RIGHT -> pos.relative(neighbourDirection(face, UP)).relative(neighbourDirection(face, RIGHT));
            case BOTTOM_RIGHT -> pos.relative(neighbourDirection(face, DOWN)).relative(neighbourDirection(face, RIGHT));
            case BOTTOM_LEFT -> pos.relative(neighbourDirection(face, DOWN)).relative(neighbourDirection(face, LEFT));
        };
    }

    static BakedQuad fullFace(Direction side, TextureAtlasSprite sprite) {
        Vector3f[] corners = CORNERS.get(side);
        return quad(side, corners[0], corners[1], corners[2], corners[3], sprite, 0, 0, 1, 1);
    }

    static BakedQuad quadrant(Direction side, int horizontal, int vertical, CtmTileSelector.Tile tile,
            TextureAtlasSprite sprite) {
        Vector3f[] corners = CORNERS.get(side);
        float s0 = horizontal * 0.5F;
        float t0 = vertical * 0.5F;
        Vector3f topLeft = interpolate(corners, s0, t0);
        Vector3f bottomLeft = interpolate(corners, s0, t0 + 0.5F);
        Vector3f bottomRight = interpolate(corners, s0 + 0.5F, t0 + 0.5F);
        Vector3f topRight = interpolate(corners, s0 + 0.5F, t0);
        float step = 1.0F / tile.source().gridSize();
        float u0 = tile.x() * step;
        float v0 = tile.y() * step;
        return quad(side, topLeft, bottomLeft, bottomRight, topRight, sprite,
                u0, v0, u0 + step, v0 + step);
    }

    private static Vector3f interpolate(Vector3f[] corners, float horizontal, float vertical) {
        Vector3f topLeft = corners[0];
        Vector3f bottomLeft = corners[1];
        Vector3f topRight = corners[3];
        return new Vector3f(
                topLeft.x + horizontal * (topRight.x - topLeft.x) + vertical * (bottomLeft.x - topLeft.x),
                topLeft.y + horizontal * (topRight.y - topLeft.y) + vertical * (bottomLeft.y - topLeft.y),
                topLeft.z + horizontal * (topRight.z - topLeft.z) + vertical * (bottomLeft.z - topLeft.z));
    }

    private static BakedQuad quad(Direction side, Vector3f topLeft, Vector3f bottomLeft,
            Vector3f bottomRight, Vector3f topRight, TextureAtlasSprite sprite,
            float u0, float v0, float u1, float v1) {
        QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
        builder.setSprite(sprite);
        builder.setDirection(side);
        builder.setShade(true);
        Vec3i normal = side.getNormal();
        putVertex(builder, normal, topLeft, sprite, u0, v0);
        putVertex(builder, normal, bottomLeft, sprite, u0, v1);
        putVertex(builder, normal, bottomRight, sprite, u1, v1);
        putVertex(builder, normal, topRight, sprite, u1, v0);
        return builder.bakeQuad();
    }

    private static void putVertex(QuadBakingVertexConsumer builder, Vec3i normal, Vector3f position,
            TextureAtlasSprite sprite, float u, float v) {
        builder.addVertex(position.x, position.y, position.z);
        builder.setColor(255, 255, 255, 255);
        builder.setNormal(normal.getX(), normal.getY(), normal.getZ());
        builder.setUv(sprite.getU(u), sprite.getV(v));
    }

    private static EnumMap<Direction, Vector3f[]> buildCorners() {
        EnumMap<Direction, Vector3f[]> result = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            float plane = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0 : 1;
            Vector3f[] corners = switch (facing.getAxis()) {
                case X -> new Vector3f[]{new Vector3f(plane, 1, 1), new Vector3f(plane, 0, 1),
                        new Vector3f(plane, 0, 0), new Vector3f(plane, 1, 0)};
                case Y -> new Vector3f[]{new Vector3f(1, plane, 1), new Vector3f(1, plane, 0),
                        new Vector3f(0, plane, 0), new Vector3f(0, plane, 1)};
                case Z -> new Vector3f[]{new Vector3f(0, 1, plane), new Vector3f(0, 0, plane),
                        new Vector3f(1, 0, plane), new Vector3f(1, 1, plane)};
            };
            if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                corners = new Vector3f[]{corners[3], corners[2], corners[1], corners[0]};
            }
            result.put(facing, corners);
        }
        return result;
    }

    private static EnumMap<Direction, Direction[]> buildNeighbours() {
        EnumMap<Direction, Direction[]> result = new EnumMap<>(Direction.class);
        result.put(Direction.DOWN, new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST});
        result.put(Direction.UP, new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST});
        result.put(Direction.NORTH, new Direction[]{Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST});
        result.put(Direction.SOUTH, new Direction[]{Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST});
        result.put(Direction.WEST, new Direction[]{Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH});
        result.put(Direction.EAST, new Direction[]{Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH});
        return result;
    }
}
