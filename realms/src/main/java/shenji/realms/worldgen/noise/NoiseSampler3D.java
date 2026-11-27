package shenji.realms.worldgen.noise;

@FunctionalInterface
public interface NoiseSampler3D {
    double sample(double x, double y, double z);
}
