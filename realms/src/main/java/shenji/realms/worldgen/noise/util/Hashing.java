package shenji.realms.worldgen.noise.util;

public final class Hashing {
    private Hashing() {}

    public static long mix(long hash) {
        hash = (hash ^ (hash >>> 30)) * 0xbf58476d1ce4e5b9L;
        hash = (hash ^ (hash >>> 27)) * 0x94d049bb133111ebL;
        return hash ^ (hash >>> 31);
    }

    public static double hashToUnitDouble(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    public static double hashToSignedDouble(long hash) {
        return 2 * hashToUnitDouble(hash) - 1;
    }
}
