package shenji.realms.worldgen.noise;

public class ValueNoise {
    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double smooth(double t) {
        return (1 - Math.cos(Math.PI * t)) / 2;
    }

    private static double hash(long seed, int gridX, int gridZ) {
        long h = seed ^ (gridX * 73856093L) ^ (gridZ * 19349663L);

        h = (h ^ (h >>> 30)) * 0xbf58476d1ce4e5b9L;
        h = (h ^ (h >>> 27)) * 0x94d049bb133111ebL;
        h = h ^ (h >>> 31);

        return (h >>> 11) * 0x1.0p-53 * 2.0 - 1.0;
    }


    public static double sample(long seed, int worldX, int worldZ, double gridSize) {
        // requires: gridSize > 0
        int x0 = (int) Math.floor(worldX / gridSize);
        int x1 = x0 + 1;
        int z0 = (int) Math.floor(worldZ / gridSize);
        int z1 = z0 + 1;

        double tx = (worldX - (gridSize * x0)) / gridSize;
        double tz = (worldZ - (gridSize * z0)) / gridSize;

        double tx_s = smooth(tx);
        double tz_s = smooth(tz);

        double v00 = hash(seed, x0, z0);
        double v01 = hash(seed, x0, z1);
        double v10 = hash(seed, x1, z0);
        double v11 = hash(seed, x1, z1);

        double va = lerp(v00, v10, tx_s);
        double vb = lerp(v01, v11, tx_s);
        double v = lerp(va, vb, tz_s);

        return v;
    }
}
