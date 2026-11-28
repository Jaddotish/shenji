package shenji.realms.worldgen.noise;

import java.util.ArrayList;
import java.util.List;

import static shenji.realms.worldgen.noise.util.Hashing.hashToUnitDouble;
import static shenji.realms.worldgen.noise.util.Hashing.mix;

public class CellularPointSampler {

    private final long seed;
    private final double cellSize;

    public record FeaturePoint(long seed, double x, double z) {}

    public CellularPointSampler(long seed, double cellSize) {
        this.seed = seed;
        this.cellSize = cellSize;
    }

    public List<FeaturePoint> getNearbyPoints(double x, double z, double maxRadius, int pointsPerCell) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellZ = (int) Math.floor(z / cellSize);

        int searchRadius = (int) Math.ceil(maxRadius / cellSize);

        List<FeaturePoint> list = new ArrayList<>();

        for (int i = cellX - searchRadius; i <= cellX + searchRadius; i++) {
            for (int j = cellZ - searchRadius; j <= cellZ + searchRadius; j++) {
                for (int p = 0; p < pointsPerCell; p++) {
                    list.add(getFeaturePoint(i, j, p));
                }
            }
        }

        return list;
    }

    public FeaturePoint getFeaturePoint(int cellX, int cellZ, int index) {
        long cellHash = hashCell(cellX, cellZ, index);

        long hashX = mix(cellHash ^ 0x9e3779b97f4a7c15L);
        long hashZ = mix(cellHash ^ 0xc2b2ae3d27d4eb4fL);

        double offsetX = hashToUnitDouble(hashX);
        double offsetZ = hashToUnitDouble(hashZ);


        double x = (cellX + offsetX) * cellSize;
        double z = (cellZ + offsetZ) * cellSize;

        return new FeaturePoint(cellHash, x, z);
    }

    private long hashCell(int cellX, int cellZ, int index) {
        long hash = seed;
        hash ^= (long) cellX * 0x9e3779b97f4a7c15L;
        hash ^= (long) cellZ * 0xc2b2ae3d27d4eb4fL;
        hash ^= (long) index * 0xa24baed4963ee407L;
        return mix(hash);
    }
}