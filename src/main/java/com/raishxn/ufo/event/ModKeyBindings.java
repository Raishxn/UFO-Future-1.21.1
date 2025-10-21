package com.raishxn.ufo.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final String KEY_CATEGORY_UFO = "key.category.ufo";

    public static final KeyMapping CYCLE_TOOL_FORWARD = new KeyMapping(
            "key.ufo.cycle_tool_forward",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY_UFO
    );

    public static final KeyMapping CYCLE_TOOL_BACKWARD = new KeyMapping(
            "key.ufo.cycle_tool_backward",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KEY_CATEGORY_UFO
    );

    public static final KeyMapping CYCLE_MODE = new KeyMapping(
            "key.ufo.cycle_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KEY_CATEGORY_UFO
    );
}