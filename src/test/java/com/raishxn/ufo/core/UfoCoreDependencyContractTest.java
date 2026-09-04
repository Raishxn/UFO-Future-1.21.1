package com.raishxn.ufo.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.raishxn.ufocore.api.amount.UfoAmount;
import com.raishxn.ufocore.api.amount.UfoRatio;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class UfoCoreDependencyContractTest {
    @Test
    void consumesExactAmountApiFromExternalCoreProject() {
        UfoAmount extreme = UfoAmount.of(BigInteger.TEN.pow(100));
        assertEquals(BigInteger.TEN.pow(99), UfoRatio.of(1, 10).applyFloor(extreme).asBigInteger());
    }
}
