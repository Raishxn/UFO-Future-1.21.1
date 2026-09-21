package com.raishxn.ufo.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formatting shared by the AE2 CPU row and tooltip for UFO's very large capacities. */
public final class CpuAmountFormatter {
    public static final long INFINITE_STORAGE_THRESHOLD = Long.MAX_VALUE - 16;
    public static final int INFINITE_THREADS_THRESHOLD = Integer.MAX_VALUE - 1;
    public static final int FINITE_THREADS_CAP = Integer.MAX_VALUE - 2;

    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = ThreadLocal.withInitial(
            () -> new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT)));
    private static final String[] DECIMAL_UNITS = {"", "K", "M", "G", "T", "P", "E", "Y", "Z", "R", "Q"};
    private static final String[] BINARY_UNITS = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};

    private CpuAmountFormatter() {
    }

    public static String storage(long bytes) {
        if (bytes >= INFINITE_STORAGE_THRESHOLD) return "\u221E";
        if (bytes < 1024) return DECIMAL_FORMAT.get().format(bytes) + "B";
        int unit = Math.min((int) (Math.log(bytes) / Math.log(1024)), BINARY_UNITS.length - 1);
        return DECIMAL_FORMAT.get().format(bytes / Math.pow(1024, unit)) + BINARY_UNITS[unit];
    }

    public static String threads(long amount) {
        if (amount >= INFINITE_THREADS_THRESHOLD) return "\u221E";
        if (amount == FINITE_THREADS_CAP) return "\u2265" + decimal(amount);
        return decimal(amount);
    }

    static String decimal(long amount) {
        if (amount < 1000) return DECIMAL_FORMAT.get().format(amount);
        int unit = Math.min((int) (Math.log10(amount) / 3), DECIMAL_UNITS.length - 1);
        return DECIMAL_FORMAT.get().format(amount / Math.pow(1000, unit)) + DECIMAL_UNITS[unit];
    }
}
