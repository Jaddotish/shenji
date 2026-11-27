package shenji.realms.worldgen.noise;

import shenji.realms.internal.fastnoiselite.FastNoiseLite;

import java.util.function.Consumer;

public class NoiseFactory {

    private final SeedContext seedContext;
    public NoiseFactory(SeedContext seedContext) {
        this.seedContext = seedContext;
    }

    public static NoiseSampler2D adapt2D(FastNoiseLite noise) {
        return (x, z) -> noise.GetNoise((float) x, (float) z);
    }

    public NoiseSampler2D create2D(String name, Consumer<FastNoiseLite> configure) {
        FastNoiseLite noise = createNoise(name, configure);
        return adapt2D(noise);
    }

    public static NoiseSampler3D adapt3D(FastNoiseLite noise) {
        return (x, y, z) -> noise.GetNoise((float) x, (float) y, (float) z);
    }

    public NoiseSampler3D create3D(String name, Consumer<FastNoiseLite> configure) {
        FastNoiseLite noise = createNoise(name, configure);
        return adapt3D(noise);
    }


    // creates the noise
    private FastNoiseLite createNoise(String name, Consumer<FastNoiseLite> configure) {
        int seed = seedContext.deriveSeed(name);
        FastNoiseLite noise = new FastNoiseLite(seed);

        configure.accept(noise);

        return noise;
    }
}
