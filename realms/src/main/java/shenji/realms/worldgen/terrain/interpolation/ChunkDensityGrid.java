package shenji.realms.worldgen.terrain.interpolation;

import shenji.realms.worldgen.terrain.TerrainProfile;

public class ChunkDensityGrid {

    private static final int SAMPLE_INTERVAL = 4; // world height must be divisible by SAMPLE_INTERVAL
    private static final int HORIZONTAL_SAMPLES = 16 / SAMPLE_INTERVAL + 1;

    private final double[] samples;
    private final int originX;
    private final int originZ;
    private final int minY;

    public ChunkDensityGrid(TerrainProfile terrainProfile, int originX, int originZ, int minY, int height) {
        this.originX = originX;
        this.originZ = originZ;
        this.minY = minY;

        int samplesY = (height / SAMPLE_INTERVAL) + 1;
        samples = new double[HORIZONTAL_SAMPLES * HORIZONTAL_SAMPLES * samplesY];

        for (int iy = 0; iy < samplesY; iy++) {
            for (int iz = 0; iz < HORIZONTAL_SAMPLES; iz++) {
                for (int ix = 0; ix < HORIZONTAL_SAMPLES; ix++) {
                    int worldX = originX + ix * SAMPLE_INTERVAL;
                    int worldZ = originZ + iz * SAMPLE_INTERVAL;
                    int worldY = minY + iy * SAMPLE_INTERVAL;

                    double density = terrainProfile.sampleDensity(worldX, worldY, worldZ);
                    int idx = index(ix, iy, iz);
                    samples[idx] = density;
                }
            }
        }
    }

    private static int index(int ix, int iy, int iz) {
        // flattened array, x: 0-4, z: 0-4, y: 0-96 (inclusive, assuming height 384)
        return ((ix) + (HORIZONTAL_SAMPLES * iz) + (HORIZONTAL_SAMPLES * HORIZONTAL_SAMPLES * iy));
    }

    public double sampleDensity(int worldX, int worldY, int worldZ) {
        int localX = worldX - originX;
        int localZ = worldZ - originZ;
        int localY = worldY - minY;

        int ix = localX / SAMPLE_INTERVAL;
        int iz = localZ / SAMPLE_INTERVAL;
        int iy = localY / SAMPLE_INTERVAL;

        double tx = (localX % SAMPLE_INTERVAL) * 1.0 / SAMPLE_INTERVAL;
        double tz = (localZ % SAMPLE_INTERVAL) * 1.0 / SAMPLE_INTERVAL;
        double ty = (localY % SAMPLE_INTERVAL) * 1.0 / SAMPLE_INTERVAL;

        double v000 = samples[index(ix, iy, iz)];
        double v001 = samples[index(ix, iy, iz + 1)];
        double v010 = samples[index(ix, iy + 1, iz)];
        double v011 = samples[index(ix, iy + 1, iz + 1)];
        double v100 = samples[index(ix + 1, iy, iz)];
        double v101 = samples[index(ix + 1, iy, iz + 1)];
        double v110 = samples[index(ix + 1, iy + 1, iz)];
        double v111 = samples[index(ix + 1, iy + 1, iz + 1)];

        return Interpolation.trilerp(v000, v001, v100, v101, v010, v011, v110, v111, tx, ty, tz);
    }
}
