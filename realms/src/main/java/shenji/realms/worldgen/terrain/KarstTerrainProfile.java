package shenji.realms.worldgen.terrain;

import shenji.realms.internal.fastnoiselite.FastNoiseLite;
import shenji.realms.worldgen.noise.CellularPointSampler;
import shenji.realms.worldgen.noise.NoiseFactory;
import shenji.realms.worldgen.noise.NoiseSampler2D;
import shenji.realms.worldgen.noise.NoiseSampler3D;
import shenji.realms.worldgen.noise.util.DomainWarp;
import shenji.realms.worldgen.noise.util.Hashing;

public class KarstTerrainProfile implements TerrainProfile {

    private final CellularPointSampler pillars;

    private final NoiseSampler2D ground;
    private final NoiseSampler2D warpX;
    private final NoiseSampler2D warpZ;
    private final NoiseSampler2D summitSecondary;

    private final NoiseSampler3D cliffVariation;
    private final NoiseSampler3D cliffDetail;
    private final NoiseSampler3D bendX;
    private final NoiseSampler3D bendZ;

    private final KarstParameters params;

    public KarstTerrainProfile(NoiseFactory noiseFactory, KarstParameters params) {
        this.params = params;

        this.pillars = noiseFactory.createCellular(
                "karst/pillars", params.pillar().cellSize()
        );

        // 2d
        this.summitSecondary = noiseFactory.create2D("karst/summit_secondary", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.summit().detailFrequency());
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(2);
        });

        this.ground = noiseFactory.create2D("karst/ground", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.ground().frequency());
            noise.SetFractalType(FastNoiseLite.FractalType.FBm);
            noise.SetFractalOctaves(3);
        });

        this.warpX = noiseFactory.create2D("karst/warp_x", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.warp().warpFrequency());
        });

        this.warpZ = noiseFactory.create2D("karst/warp_z", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.warp().warpFrequency());
        });

        // 3d
        this.cliffVariation = noiseFactory.create3D("karst/cliff_variation", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.cliff().variationFrequency());
        });

        this.cliffDetail = noiseFactory.create3D("karst/cliff_detail", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
            noise.SetFrequency(params.cliff().detailFrequency());
            noise.SetFractalOctaves(3);
        });

        this.bendX = noiseFactory.create3D("karst/bend_x", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.warp().bendFrequency());
        });

        this.bendZ = noiseFactory.create3D("karst/bend_z", noise -> {
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetFrequency(params.warp().bendFrequency());
        });
    }

    @Override
    public double sampleDensity(double x, double y, double z) {

        // ----- constants ----- //
        // pillar geometry
        double radius = params.pillar().radius();

        // domain warping
        double warpAmplitude = params.warp().warpAmplitude();

        // summit elevation
        double pillarHeight = params.pillar().height();
        double pillarRange = params.pillar().heightRange();
        double summitDetailAmplitude = params.summit().detailAmplitude();

        // ground
        double groundBaseHeight = params.ground().baseHeight();
        double groundAmplitude = params.ground().amplitude();

        // base tapering
        double baseWidening = params.pillar().baseWidening();
        double baseTaperHeight = params.pillar().baseTaperHeight();

        // cliff variation
        double cliffHorizontalScale = params.cliff().variationHorizontalScale();
        double cliffVerticalScale = params.cliff().variationVerticalScale();
        double cliffAmplitude = params.cliff().variationAmplitude();

        // cliff detail
        double cliffDetailHorizontalScale = params.cliff().detailHorizontalScale();
        double cliffDetailVerticalScale = params.cliff().detailVerticalScale();
        double cliffDetailAmplitude = params.cliff().detailAmplitude();

        // pillar bending
        double bendAmplitude = params.warp().bendAmplitude();

        // ----- equations ----- //
        // domain warping
        var warped = DomainWarp.horizontalWarp(
                new DomainWarp.Point3D(x, y, z),
                (sy, sz) -> warpAmplitude * warpX.sample(0, sz)
                        + bendAmplitude * bendX.sample(0, sy, sz),
                (sx, sy) -> warpAmplitude * warpZ.sample(sx, 0)
                        + bendAmplitude * bendZ.sample(sx, sy, 0)
        );

        double warpedX = warped.x();
        double warpedZ = warped.z();

        // ground
        double groundHeight = groundBaseHeight + groundAmplitude * ground.sample(x, z);
        double groundDensity = groundHeight - y;

        // horizontal geometry
        double maxRadius = radius + baseWidening + cliffAmplitude + cliffDetailAmplitude;

        var nearbyPillars = pillars.getNearbyPoints(warpedX, warpedZ, maxRadius);

        // cliff variation
        double radiusVariation = cliffAmplitude * cliffVariation.sample(
                x * cliffHorizontalScale,
                y * cliffVerticalScale,
                z * cliffHorizontalScale);
        double radiusDetail = cliffDetailAmplitude * cliffDetail.sample(
                x * cliffDetailHorizontalScale,
                y * cliffDetailVerticalScale,
                z * cliffDetailHorizontalScale);

        double radiusCliffVariation = radiusVariation + radiusDetail;

        // summit elevation
        double summitDetail = summitDetailAmplitude * summitSecondary.sample(warpedX, warpedZ);

        // base widening
        double heightAboveGround = y - groundHeight;
        double normalizedHeight = heightAboveGround / baseTaperHeight;
        double wideningFactor = Math.max(0.0, Math.min(1.0, 1.0 - normalizedHeight));

        // density intersection
        double baseRadiusAddition = baseWidening * wideningFactor * wideningFactor;

        double finalDensity = groundDensity;

        for (var point : nearbyPillars) {
            double summitPrimary = Hashing.hashToSignedDouble(point.seed());

            double summitHeight = pillarHeight
                    + pillarRange * summitPrimary
                    + summitDetail;

            double pillarDensity = samplePillarDensity(
                    point,
                    warped,
                    groundHeight,
                    summitHeight,
                    radiusCliffVariation,
                    baseRadiusAddition
            );

            finalDensity = Math.max(finalDensity, pillarDensity);
        }

        return finalDensity;
    }

    private double samplePillarDensity(
            CellularPointSampler.FeaturePoint point,
            DomainWarp.Point3D warped,
            double groundHeight,
            double summitHeight,
            double radiusCliffVariation,
            double baseRadiusAddition
    ) {
        double radius = params.pillar().radius();
        double upwardTaper = params.pillar().upwardTaper();
        double edgeLoss = params.summit().edgeLoss();
        double roundingExponent = params.summit().roundingExponent();

        double distance = Math.hypot(
                warped.x() - point.x(),
                warped.z() - point.z()
        );

        // upward tapering
        double pillarLength = Math.max(1.0, summitHeight - groundHeight);
        double relativeHeight = (warped.y() - groundHeight) / pillarLength;
        double taperFactor = Math.clamp(relativeHeight, 0.0, 1.0);

        double taperedRadius = radius * (1.0 - upwardTaper * taperFactor);

        // horizontal density
        double effectiveRadius = taperedRadius
                + radiusCliffVariation
                + baseRadiusAddition;

        double horizontalDensity = effectiveRadius - distance;

        // summit rounding
        double normalizedDistance = Math.min(1.0, distance / radius);

        double roundedSummit = summitHeight
                - edgeLoss * Math.pow(normalizedDistance, roundingExponent);

        // vertical density
        double verticalDensity = roundedSummit - warped.y();

        return Math.min(horizontalDensity, verticalDensity);
    }
}
