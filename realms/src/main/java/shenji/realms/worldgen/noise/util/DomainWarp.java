package shenji.realms.worldgen.noise.util;

import shenji.realms.worldgen.noise.NoiseSampler2D;

import java.util.function.DoubleUnaryOperator;

public class DomainWarp {

    public record Point2D(double x, double z) {}
    public record Point3D(double x, double y, double z) {}

    public static Point2D shearX(Point2D point, DoubleUnaryOperator displacement) {
        return new Point2D(point.x() + displacement.applyAsDouble(point.z()), point.z());
    }
    public static Point2D shearZ(Point2D point, DoubleUnaryOperator displacement) {
        return new Point2D(point.x(), point.z() + displacement.applyAsDouble(point.x()));
    }


    public static Point3D shearX(Point3D point, NoiseSampler2D displacement) {
        return new Point3D(point.x() + displacement.sample(point.y(), point.z()), point.y(), point.z());
    }
    public static Point3D shearZ(Point3D point, NoiseSampler2D displacement) {
        return new Point3D(point.x(), point.y(), point.z() + displacement.sample(point.x(), point.y()));
    }

    public static Point3D horizontalWarp(Point3D point, NoiseSampler2D displacementX, NoiseSampler2D displacementZ) {
        Point3D xSheared = shearX(point, displacementX);
        return shearZ(xSheared, displacementZ);
    }
}
