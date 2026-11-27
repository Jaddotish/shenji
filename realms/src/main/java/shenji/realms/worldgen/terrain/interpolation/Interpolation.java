package shenji.realms.worldgen.terrain.interpolation;

public class Interpolation {

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double bilerp(double v00, double v10, double v01, double v11, double tx, double tz) {
        double v1 = lerp(v00, v10, tx);
        double v2 = lerp(v01, v11, tx);

        return lerp(v1, v2, tz);
    }

    public static double trilerp(double v000, double v001, double v100, double v101, double v010, double v011, double v110, double v111, double tx, double ty, double tz) {
        double v1 = bilerp(v000, v100, v001, v101, tx, tz);
        double v2 = bilerp(v010, v110, v011, v111, tx, tz);

        return lerp(v1, v2, ty);
    }
}
