package games.negative.chat.temp;

import java.util.Random;

public final class PerlinNoise {
    private final int[] perm = new int[512];

    public PerlinNoise(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;

        Random r = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    public double noise2D(double x, double y) {
        int X = fastFloor(x) & 255;
        int Y = fastFloor(y) & 255;

        x -= fastFloor(x);
        y -= fastFloor(y);

        double u = fade(x);
        double v = fade(y);

        int aa = perm[X     + perm[Y]];
        int ab = perm[X     + perm[Y + 1]];
        int ba = perm[X + 1 + perm[Y]];
        int bb = perm[X + 1 + perm[Y + 1]];

        double gradAA = grad2(aa, x, y);
        double gradBA = grad2(ba, x - 1, y);
        double gradAB = grad2(ab, x, y - 1);
        double gradBB = grad2(bb, x - 1, y - 1);

        double lerpX1 = lerp(gradAA, gradBA, u);
        double lerpX2 = lerp(gradAB, gradBB, u);

        return lerp(lerpX1, lerpX2, v); // ~[-1,1]
    }

    public double noise3D(double x, double y, double z) {
        int X = fastFloor(x) & 255;
        int Y = fastFloor(y) & 255;
        int Z = fastFloor(z) & 255;

        x -= fastFloor(x);
        y -= fastFloor(y);
        z -= fastFloor(z);

        double u = fade(x), v = fade(y), w = fade(z);

        int aaa = perm[X     + perm[Y     + perm[Z    ]]];
        int aab = perm[X     + perm[Y     + perm[Z + 1]]];
        int aba = perm[X     + perm[Y + 1 + perm[Z    ]]];
        int abb = perm[X     + perm[Y + 1 + perm[Z + 1]]];
        int baa = perm[X + 1 + perm[Y     + perm[Z    ]]];
        int bab = perm[X + 1 + perm[Y     + perm[Z + 1]]];
        int bba = perm[X + 1 + perm[Y + 1 + perm[Z    ]]];
        int bbb = perm[X + 1 + perm[Y + 1 + perm[Z + 1]]];

        double x1 = lerp(grad3(aaa, x, y, z),     grad3(baa, x - 1, y, z),     u);
        double x2 = lerp(grad3(aba, x, y - 1, z), grad3(bba, x - 1, y - 1, z), u);
        double y1 = lerp(x1, x2, v);

        double x3 = lerp(grad3(aab, x, y, z - 1),     grad3(bab, x - 1, y, z - 1),     u);
        double x4 = lerp(grad3(abb, x, y - 1, z - 1), grad3(bbb, x - 1, y - 1, z - 1), u);
        double y2 = lerp(x3, x4, v);

        return lerp(y1, y2, w);
    }

    // --- fBm helpers ---

    public double fbm2D(double x, double y, int octaves, double lacunarity, double gain) {
        double amp = 1.0;
        double freq = 1.0;
        double sum = 0.0;
        double norm = 0.0;

        for (int i = 0; i < octaves; i++) {
            sum += noise2D(x * freq, y * freq) * amp;
            norm += amp;
            amp *= gain;
            freq *= lacunarity;
        }
        return sum / norm; // ~[-1,1]
    }

    public double fbm3D(double x, double y, double z, int octaves, double lacunarity, double gain) {
        double amp = 1.0;
        double freq = 1.0;
        double sum = 0.0;
        double norm = 0.0;

        for (int i = 0; i < octaves; i++) {
            sum += noise3D(x * freq, y * freq, z * freq) * amp;
            norm += amp;
            amp *= gain;
            freq *= lacunarity;
        }
        return sum / norm; // ~[-1,1]
    }

    // Ridged noise (0..1), great for coastlines/edges
    public double ridged2D(double x, double y, int octaves, double lacunarity, double gain) {
        double amp = 0.5;
        double freq = 1.0;
        double sum = 0.0;

        for (int i = 0; i < octaves; i++) {
            double n = noise2D(x * freq, y * freq);
            n = 1.0 - Math.abs(n); // ridge
            sum += n * amp;

            amp *= gain;
            freq *= lacunarity;
        }
        return Math.max(0.0, Math.min(1.0, sum));
    }

    private static int fastFloor(double x) {
        return x >= 0 ? (int) x : (int) x - 1;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double grad2(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static double grad3(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}

