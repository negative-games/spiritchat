package games.negative.chat.temp;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public final class SkyIslandsGenerator extends ChunkGenerator {

    private static final int BASE_CENTER_Y = 120;

    private static final double CELL_SIZE = 170.0;
    private static final double BASE_RADIUS = 64.0;
    private static final double BASE_THICKNESS = 28.0;

    // ---- edge / realism tuning ----
    private static final double EDGE_FALLOFF_EXP = 2.2;     // gentle skirt
    private static final double TOP_ROUND_EXP = 3.0;        // flatter top
    private static final double BOTTOM_ROUND_EXP = 2.0;     // round underside

    private static final double MIN_EDGE_THICKNESS = 0.28;
    private static final double MAX_THICKNESS_MULT = 1.10;
    private static final int HEIGHT_VARIANCE = 24;

    // Metaball field tuning
    private static final double TOP_POINT_SPREAD = 0.55;     // how far top points drift from center (fraction of radius)
    private static final double BOTTOM_POINT_SPREAD = 0.65;  // underside lobes drift a bit more
    private static final double TOP_POINT_SIZE_MIN = 0.55;   // relative size of top points
    private static final double TOP_POINT_SIZE_MAX = 1.05;
    private static final double BOT_POINT_SIZE_MIN = 0.60;
    private static final double BOT_POINT_SIZE_MAX = 1.15;

    private final PerlinNoise noise = new PerlinNoise(987654321L);

    @Override public boolean shouldGenerateNoise() { return false; }
    @Override public boolean shouldGenerateSurface() { return false; }
    @Override public boolean shouldGenerateCaves() { return false; }
    @Override public boolean shouldGenerateDecorations() { return false; }
    @Override public boolean shouldGenerateMobs() { return false; }
    @Override public boolean shouldGenerateStructures() { return false; }

    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        final int minY = chunkData.getMinHeight();
        final int maxY = chunkData.getMaxHeight();

        final int worldChunkX = chunkX << 4;
        final int worldChunkZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = worldChunkX + x;
                int wz = worldChunkZ + z;

                IslandSample island = sampleNearestIsland(wx, wz);
                if (island == null) continue;

                // --- Domain warp to kill circularity ---
                double warp1 = noise.fbm2D(wx * 0.01, wz * 0.01, 3, 2.0, 0.5) * 16.0;
                double warp2 = noise.fbm2D(wx * 0.01 + 500, wz * 0.01 + 500, 3, 2.0, 0.5) * 16.0;

                double localX = (wx - island.centerX) + warp1;
                double localZ = (wz - island.centerZ) + warp2;

                // --- Rotate + stretch for ellipse shapes ---
                double cos = island.cosRot, sin = island.sinRot;
                double rx =  localX * cos - localZ * sin;
                double rz =  localX * sin + localZ * cos;

                rx /= island.stretchX;
                rz /= island.stretchZ;

                double distToCenter = Math.sqrt(rx * rx + rz * rz);

                // Ridged noise coastline variation
                double ridge = noise.ridged2D(wx * 0.02, wz * 0.02, 4, 2.0, 0.55);
                double radiusHere = island.radius * (0.8 + ridge * 0.5);
                if (distToCenter > radiusHere) continue;

                // --- Metaball top field (plateaus + multiple highs) ---
                double topField = 0.0;
                for (Point p : island.topPoints) {
                    double dx = rx - p.ox;
                    double dz = rz - p.oz;
                    double d = Math.sqrt(dx * dx + dz * dz);

                    // metaball influence (smooth, wide)
                    double influence = 1.0 - (d / (radiusHere * p.size));
                    if (influence > 0) {
                        // soften into plateau instead of pointy mountain
                        influence = smoothstep(0, 1, influence);
                        topField = Math.max(topField, influence * p.weight);
                    }
                }

                // --- Metaball bottom field (multiple underside lobes) ---
                double bottomField = 0.0;
                for (Point p : island.bottomPoints) {
                    double dx = rx - p.ox;
                    double dz = rz - p.oz;
                    double d = Math.sqrt(dx * dx + dz * dz);

                    double influence = 1.0 - (d / (radiusHere * p.size));
                    if (influence > 0) {
                        influence = smoothstep(0, 1, influence);
                        bottomField = Math.max(bottomField, influence * p.weight);
                    }
                }

                // Combine fields into horizontal falloff (0..1)
                double hRaw = clamp01((topField * 0.7) + (bottomField * 0.3));
                if (hRaw <= 0) continue;

                // Gentle skirt so edges slope
                double hSkirt = Math.pow(hRaw, EDGE_FALLOFF_EXP);

                double thicknessNoise = noise.fbm2D(wx * 0.03, wz * 0.03, 2, 2.0, 0.5);

                double thicknessHere = island.thickness
                        * (MIN_EDGE_THICKNESS + (1.0 - MIN_EDGE_THICKNESS) * hSkirt)
                        * (1.0 + thicknessNoise * 0.10);

                double maxThick = island.thickness * MAX_THICKNESS_MULT;
                if (thicknessHere > maxThick) thicknessHere = maxThick;

                int centerY = island.centerY;

                int yBottom = (int) Math.floor(centerY - thicknessHere);
                int yTop    = (int) Math.ceil(centerY + thicknessHere);

                if (yTop < minY || yBottom > maxY) continue;

                yBottom = Math.max(yBottom, minY);
                yTop    = Math.min(yTop, maxY - 1);

                for (int y = yBottom; y <= yTop; y++) {
                    double dy = (y - centerY) / thicknessHere; // -1..1
                    double ady = Math.abs(dy);

                    // Asymmetric vertical profile
                    double verticalProfile;
                    if (dy >= 0) {
                        verticalProfile = 1.0 - Math.pow(ady, TOP_ROUND_EXP);
                    } else {
                        verticalProfile = 1.0 - Math.pow(ady, BOTTOM_ROUND_EXP);
                    }

                    // Roughness reduced to keep smooth silhouette
                    double topRough = noise.fbm3D(wx * 0.04, y * 0.05, wz * 0.04, 3, 2.0, 0.5);
                    double botRough = noise.fbm3D(wx * 0.04 + 2000, y * 0.05, wz * 0.04 + 2000, 3, 2.0, 0.5);
                    double rough = (dy >= 0 ? topRough : botRough) * 0.20;

                    double density = verticalProfile + rough;

                    // pockets still exist, but taper strongly at rim
                    double pocket = noise.fbm3D(wx * 0.09, y * 0.09, wz * 0.09, 2, 2.2, 0.45);
                    double pocketStrength = hSkirt;
                    density -= Math.max(0, pocket - 0.30) * 0.6 * pocketStrength;

                    if (density > 0.10) {
                        chunkData.setBlock(x, y, z, Material.STONE);
                    }
                }
            }
        }
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        final int minY = chunkData.getMinHeight();
        final int maxY = chunkData.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = -1;
                for (int y = maxY - 1; y >= minY; y--) {
                    if (!chunkData.getType(x, y, z).isAir()) {
                        top = y;
                        break;
                    }
                }
                if (top == -1) continue;

                chunkData.setBlock(x, top, z, Material.GRASS_BLOCK);

                for (int d = 1; d <= 3; d++) {
                    int yy = top - d;
                    if (yy >= minY && chunkData.getType(x, yy, z) == Material.STONE) {
                        chunkData.setBlock(x, yy, z, Material.DIRT);
                    }
                }
            }
        }
    }

    @Override
    public int getBaseHeight(WorldInfo worldInfo, Random random, int x, int z, org.bukkit.HeightMap heightMap) {
        IslandSample island = sampleNearestIsland(x, z);
        if (island == null) return worldInfo.getMinHeight();

        double dist = Math.hypot(x - island.centerX, z - island.centerZ);
        if (dist > island.radius) return worldInfo.getMinHeight();

        return Math.min(island.centerY + (int) island.thickness, worldInfo.getMaxHeight() - 1);
    }

    private IslandSample sampleNearestIsland(int wx, int wz) {
        int cellX = floorDiv(wx, (int) CELL_SIZE);
        int cellZ = floorDiv(wz, (int) CELL_SIZE);

        IslandSample best = null;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = cellX + dx;
                int cz = cellZ + dz;

                double presence = noise.noise2D(cx * 0.7 + 100, cz * 0.7 + 100);
                if (presence < -0.05) continue;

                double jitterX = noise.noise2D(cx * 0.9, cz * 0.9) * (CELL_SIZE * 0.45);
                double jitterZ = noise.noise2D(cx * 0.9 + 1000, cz * 0.9 + 1000) * (CELL_SIZE * 0.45);

                double centerX = cx * CELL_SIZE + CELL_SIZE / 2.0 + jitterX;
                double centerZ = cz * CELL_SIZE + CELL_SIZE / 2.0 + jitterZ;

                double rVar = noise.noise2D(cx * 0.45 + 2000, cz * 0.45 + 2000);
                double tVar = noise.noise2D(cx * 0.45 + 3000, cz * 0.45 + 3000);
                double hVar = noise.noise2D(cx * 0.25 + 4000, cz * 0.25 + 4000);

                double radius = BASE_RADIUS * (0.6 + (rVar + 1) * 0.5);
                double thickness = BASE_THICKNESS * (0.55 + (tVar + 1) * 0.55);
                int centerY = BASE_CENTER_Y + (int) Math.round(hVar * HEIGHT_VARIANCE);

                double stretchX = 0.7 + (noise.noise2D(cx * 0.6 + 10, cz * 0.6 + 10) + 1) * 0.65;
                double stretchZ = 0.7 + (noise.noise2D(cx * 0.6 + 20, cz * 0.6 + 20) + 1) * 0.65;

                double rot = (noise.noise2D(cx * 0.8 + 30, cz * 0.8 + 30) + 1) * Math.PI;
                double cosRot = Math.cos(rot);
                double sinRot = Math.sin(rot);

                // Determine how many points this island gets based on size.
                int pointCount = clampInt(1 + (int) Math.floor(radius / 55.0), 1, 4);

                Point[] tops = makePoints(cx, cz, pointCount, radius, TOP_POINT_SPREAD,
                        TOP_POINT_SIZE_MIN, TOP_POINT_SIZE_MAX, 10000);
                Point[] bottoms = makePoints(cx, cz, pointCount, radius, BOTTOM_POINT_SPREAD,
                        BOT_POINT_SIZE_MIN, BOT_POINT_SIZE_MAX, 20000);

                // Distance to nearest top metaball for choosing nearest island
                double minBlobDist = Double.MAX_VALUE;
                for (Point p : tops) {
                    double d = Math.hypot(wx - (centerX + p.ox), wz - (centerZ + p.oz));
                    minBlobDist = Math.min(minBlobDist, d);
                }

                if (best == null || minBlobDist < best.dist) {
                    best = new IslandSample(
                            minBlobDist,
                            centerX, centerZ,
                            radius, thickness,
                            centerY,
                            stretchX, stretchZ,
                            cosRot, sinRot,
                            tops, bottoms
                    );
                }
            }
        }

        return best;
    }

    private Point[] makePoints(int cx, int cz, int count, double radius, double spreadFrac,
                               double sizeMin, double sizeMax, int seedOffset) {
        Point[] pts = new Point[count];

        for (int i = 0; i < count; i++) {
            double a = hash01(cx, cz, i, seedOffset) * Math.PI * 2.0;
            double r = hash01(cx, cz, i, seedOffset + 17);

            double spread = radius * spreadFrac * r;

            double ox = Math.cos(a) * spread;
            double oz = Math.sin(a) * spread;

            double size = lerp(sizeMin, sizeMax, hash01(cx, cz, i, seedOffset + 33));
            double weight = lerp(0.7, 1.0, hash01(cx, cz, i, seedOffset + 49));

            pts[i] = new Point(ox, oz, size, weight);
        }

        return pts;
    }

    // Deterministic hash -> [0,1)
    private double hash01(int x, int z, int i, int salt) {
        long h = 1469598103934665603L;
        h ^= x * 341873128712L; h *= 1099511628211L;
        h ^= z * 132897987541L; h *= 1099511628211L;
        h ^= (long) i * 42317861L; h *= 1099511628211L;
        h ^= salt; h *= 1099511628211L;
        // map to [0,1)
        return ((h >>> 11) & ((1L << 53) - 1)) / (double)(1L << 53);
    }

    private static double smoothstep(double a, double b, double t) {
        t = clamp01((t - a) / (b - a));
        return t * t * (3 - 2 * t);
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && (r * b != a)) r--;
        return r;
    }

    private record Point(double ox, double oz, double size, double weight) {}

    private record IslandSample(
            double dist,
            double centerX, double centerZ,
            double radius, double thickness,
            int centerY,
            double stretchX, double stretchZ,
            double cosRot, double sinRot,
            Point[] topPoints,
            Point[] bottomPoints
    ) {}
}


