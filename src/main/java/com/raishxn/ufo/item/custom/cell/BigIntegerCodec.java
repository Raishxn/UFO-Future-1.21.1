package com.raishxn.ufo.item.custom.cell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.math.BigInteger;

public class BigIntegerCodec {
    public static final Codec<BigInteger> INSTANCE = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    return DataResult.success(BigIntegerLimits.parseNonNegativeDecimal(s));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(e::getMessage);
                }
            },
            value -> BigIntegerLimits.requireNonNegativeAndBounded(value).toString()
    );

    public static final StreamCodec<ByteBuf, BigInteger> STREAM_CODEC = ByteBufCodecs
            .stringUtf8(BigIntegerLimits.MAX_DECIMAL_CHARS)
            .map(
            BigIntegerLimits::parseNonNegativeDecimal,
            value -> BigIntegerLimits.requireNonNegativeAndBounded(value).toString()
    );
}
