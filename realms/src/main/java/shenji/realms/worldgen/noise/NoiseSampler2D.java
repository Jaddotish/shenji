package shenji.realms.worldgen.noise;

@FunctionalInterface
public interface NoiseSampler2D {
    double sample(double x, double z);
}
