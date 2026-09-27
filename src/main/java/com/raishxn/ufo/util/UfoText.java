package com.raishxn.ufo.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Pattern;

public final class UfoText {

    private static final Pattern FORMATTING = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

    private UfoText() {
    }

    public static MutableComponent literal(String key, Object... args) {
        return Component.translatableWithFallback(bare(key), key, args);
    }

    public static String str(String key, Object... args) {
        return Component.translatableWithFallback(bare(key), key, args).getString();
    }

    private static String bare(String key) {
        return FORMATTING.matcher(key).replaceAll("").trim();
    }
}
