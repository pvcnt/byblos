package dev.byblos.util;

public final class Hash {
    /**
     * Hash function for use with 64-bit integers.
     */
    public static int lowbias64(long v) {
        var h1 = lowbias32((int) (v >>> 32));
        var h2 = lowbias32((int) v);
        return h1 ^ h2;
    }

    /**
     * Hash function for use with 32-bit integers. For more details on this hash see:
     * https://nullprogram.com/blog/2018/07/31/
     */
    public static int lowbias32(int v) {
        var h = v;
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        h *= 0x846CA68B;
        h ^= h >>> 16;
        return h;
    }

    private Hash() {
        // Do not instantiate.
    }
}
