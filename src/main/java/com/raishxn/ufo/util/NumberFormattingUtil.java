package com.raishxn.ufo.util;

import java.text.DecimalFormat;

public class NumberFormattingUtil {

    // Este método é a nossa própria implementação para formatar bytes.
    // Ele funcionará independentemente de qualquer mod.
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i"; // Gera K, M, G, T, etc. e adiciona 'i' para KiB, MiB...

        // Formata o número para ter no máximo uma casa decimal
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}