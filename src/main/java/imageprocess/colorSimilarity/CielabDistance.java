package imageprocess.colorSimilarity;

public class CielabDistance {

    public static double[] rgbToXyz(int r, int g, int b) {
        double rn = r / 255.0;
        double gn = g / 255.0;
        double bn = b / 255.0;

        rn = (rn > 0.04045) ? Math.pow((rn + 0.055) / 1.055, 2.4) : rn / 12.92;
        gn = (gn > 0.04045) ? Math.pow((gn + 0.055) / 1.055, 2.4) : gn / 12.92;
        bn = (bn > 0.04045) ? Math.pow((bn + 0.055) / 1.055, 2.4) : bn / 12.92;

        double x = rn * 0.4124564 + gn * 0.3575761 + bn * 0.1804375;
        double y = rn * 0.2126729 + gn * 0.7151522 + bn * 0.0721750;
        double z = rn * 0.0193339 + gn * 0.1191920 + bn * 0.9503041;

        return new double[]{x * 100, y * 100, z * 100};
    }

    public static double[] xyzToLab(double x, double y, double z) {
        double xn = 95.047;
        double yn = 100.000;
        double zn = 108.883;

        double xr = x / xn;
        double yr = y / yn;
        double zr = z / zn;

        double fx = (xr > 0.008856) ? Math.pow(xr, 1.0 / 3.0) : (7.787 * xr) + (16.0 / 116.0);
        double fy = (yr > 0.008856) ? Math.pow(yr, 1.0 / 3.0) : (7.787 * yr) + (16.0 / 116.0);
        double fz = (zr > 0.008856) ? Math.pow(zr, 1.0 / 3.0) : (7.787 * zr) + (16.0 / 116.0);

        double l = 116 * fy - 16;
        double a = 500 * (fx - fy);
        double b = 200 * (fy - fz);

        return new double[]{l, a, b};
    }

    public static double calculateDeltaE(double[] lab1, double[] lab2) {
        double deltaL = lab1[0] - lab2[0];
        double deltaA = lab1[1] - lab2[1];
        double deltaB = lab1[2] - lab2[2];

        return Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
    }


}
