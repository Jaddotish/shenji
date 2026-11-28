package shenji.realms.worldgen.terrain;

public record KarstParameters(
        PillarParameters pillar,
        SummitParameters summit,
        GroundParameters ground,
        WarpParameters warp,
        CliffParameters cliff,
        RegionParameters region
) {
    public record PillarParameters(
            double minimumRadius,
            double radiusHeightScale,
            double radiusRange,
            double cellSize,
            double height,
            double heightRange,
            double upwardTaper,
            double baseWidening,
            double baseTaperHeight
    ) {}

    public record SummitParameters(
            double edgeLoss,
            double roundingExponent,
            double detailAmplitude,
            float detailFrequency
    ) {}

    public record GroundParameters(
            double baseHeight,
            double amplitude,
            float frequency
    ) {}

    public record WarpParameters(
            double warpAmplitude,
            float warpFrequency,
            double bendAmplitude,
            float bendFrequency
    ) {}

    public record CliffParameters(
            double variationAmplitude,
            float variationFrequency,
            double variationHorizontalScale,
            double variationVerticalScale,
            double detailAmplitude,
            float detailFrequency,
            double detailHorizontalScale,
            double detailVerticalScale
    ) {}

    public record RegionParameters(
            float frequency,
            double densityThreshold,
            double foundationAmplitude,
            int pointsPerCell
    ) {}

    public static KarstParameters defaults() {
        return new KarstParameters(
                new PillarParameters(
                        5.0,
                        0.05,
                        14.0,
                        1.0 / 0.015f,
                        200,
                        80,
                        0.35,
                        0.20 / 0.015f,
                        50
                ),
                new SummitParameters(
                        12,
                        2,
                        8,
                        0.03f
                ),
                new GroundParameters(
                        64,
                        25,
                        0.008f
                ),
                new WarpParameters(
                        15,
                        0.010f,
                        10,
                        0.005f
                ),
                new CliffParameters(
                        0.24,
                        0.01f,
                        1.0,
                        0.4,
                        0.16,
                        0.045f,
                        1.0,
                        0.6
                ),
                new RegionParameters(
                        0.0015f,
                        -0.15,
                        70,
                        3
                )
        );
    }
}