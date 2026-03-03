package net.thecubecollective.horsebreedinghelper.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.thecubecollective.horsebreedinghelper.util.HorseScoreCalculator;
import net.thecubecollective.horsebreedinghelper.event.HorseMountHandler;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HorseHighlightRenderer {

    private static final double HIGHLIGHT_RADIUS = 100.0;
    private static final ConcurrentHashMap<HorseEntity, Integer> HIGHLIGHTED_HORSES = new ConcurrentHashMap<>();
    private static boolean isEnabled = false;

    public static void initialize() {
        // Register world render event for custom box rendering
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!isEnabled) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            MatrixStack matrices = context.matrices();
            if (matrices == null) return;

            renderHorseHighlights(matrices, context);
        });
    }

    private static void renderHorseHighlights(MatrixStack matrices, net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d playerPos = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
        Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
        Set<HorseEntity> currentHorses = new HashSet<>();

        // List to track horses with their scores for finding top 2
        List<Map.Entry<HorseEntity, Double>> horsesWithScores = new ArrayList<>();

        // Find all horses within radius and calculate their scores
        List<Entity> entities = client.world.getOtherEntities(null,
            client.player.getBoundingBox().expand(HIGHLIGHT_RADIUS));

        for (Entity entity : entities) {
            if (entity instanceof HorseEntity horse) {
                double distance = playerPos.distanceTo(new Vec3d(horse.getX(), horse.getY(), horse.getZ()));
                if (distance <= HIGHLIGHT_RADIUS) {
                    currentHorses.add(horse);

                    // Calculate horse score
                    HorseScoreCalculator.HorseScore score = HorseScoreCalculator.calculateScore(horse);
                    int color = getScoreColor(score.totalScore);

                    // Store the horse and its color for rendering
                    HIGHLIGHTED_HORSES.put(horse, color);

                    // Add to list for top 2 calculation
                    horsesWithScores.add(new AbstractMap.SimpleEntry<>(horse, score.totalScore));

                    // Draw colored box around horse
                    drawHorseOutlineBox(matrices, horse, color, cameraPos, context);
                }
            }
        }

        // Sort horses by score (descending) and get top 2
        horsesWithScores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Draw white highlight boxes around top 2 horses
        for (int i = 0; i < Math.min(2, horsesWithScores.size()); i++) {
            HorseEntity topHorse = horsesWithScores.get(i).getKey();
            drawTopHorseHighlight(matrices, topHorse, cameraPos, context);
        }

        // Remove horses that are no longer in range
        HIGHLIGHTED_HORSES.entrySet().removeIf(entry -> !currentHorses.contains(entry.getKey()));
    }

    private static void drawManualBoxQuads(MatrixStack matrices, VertexConsumer buffer, net.minecraft.util.math.Box box, float r, float g, float b, float a) {
        float minX = (float)box.minX; float minY = (float)box.minY; float minZ = (float)box.minZ;
        float maxX = (float)box.maxX; float maxY = (float)box.maxY; float maxZ = (float)box.maxZ;

        org.joml.Matrix4f m = matrices.peek().getPositionMatrix();
        int overlay = 655360;  // Default Overlay
        int light = 15728880;  // Max Light

        // 6 Faces (Quads), each requiring 4 vertices
        float[][] vertices = {
            // Bottom face (y = minY)
            {minX, minY, minZ}, {maxX, minY, minZ}, {maxX, minY, maxZ}, {minX, minY, maxZ},
            // Top face (y = maxY)
            {minX, maxY, minZ}, {minX, maxY, maxZ}, {maxX, maxY, maxZ}, {maxX, maxY, minZ},
            // North face (z = minZ)
            {minX, minY, minZ}, {minX, maxY, minZ}, {maxX, maxY, minZ}, {maxX, minY, minZ},
            // South face (z = maxZ)
            {maxX, minY, maxZ}, {maxX, maxY, maxZ}, {minX, maxY, maxZ}, {minX, minY, maxZ},
            // West face (x = minX)
            {minX, minY, maxZ}, {minX, maxY, maxZ}, {minX, maxY, minZ}, {minX, minY, minZ},
            // East face (x = maxX)
            {maxX, minY, minZ}, {maxX, maxY, minZ}, {maxX, maxY, maxZ}, {maxX, minY, maxZ}
        };

        for (float[] v : vertices) {
            // Fully compliant vertex format for standard entity layers
            buffer.vertex(m, v[0], v[1], v[2])
                  .color(r, g, b, a)
                  .texture(0f, 0f)      // UV0
                  .overlay(overlay)     // UV1
                  .light(light)         // UV2
                  .normal(0, 1, 0);     // Normal
        }
    }

    private static void drawHorseOutlineBox(MatrixStack matrices, HorseEntity horse, int color, Vec3d cameraPos, net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        matrices.push();

        try {
            float red = ((color >> 16) & 0xFF) / 255.0f;
            float green = ((color >> 8) & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;

            // Get box relative to camera
            Box box = horse.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            VertexConsumer buffer = context.consumers().getBuffer(net.minecraft.client.render.TexturedRenderLayers.getEntitySolid());

            drawManualBoxQuads(matrices, buffer, box, red, green, blue, 0.4f);

        } catch (Exception e1) {
            // Fallback: Use basic glow
            horse.setGlowing(true);
        }

        matrices.pop();
    }

    private static void drawTopHorseHighlight(MatrixStack matrices, HorseEntity horse, Vec3d cameraPos, net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        matrices.push();

        try {
            long time = System.currentTimeMillis();
        float pulse = (float)(0.4 + 0.3 * Math.sin(time * 0.005));
            Box box = horse.getBoundingBox().offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            VertexConsumer buffer = context.consumers().getBuffer(net.minecraft.client.render.TexturedRenderLayers.getEntitySolid());

            drawManualBoxQuads(matrices, buffer, box, 1.0f, 1.0f, 1.0f, pulse);

        } catch (Exception e) {
            // If rendering fails, fall back to simple glow effect
            horse.setGlowing(true);
        }

        matrices.pop();
    }

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
        if (!enabled) {
            clearAllHighlights();
        }

        // Send feedback to player
        if (MinecraftClient.getInstance().player != null) {
            String status = enabled ? "§aEnabled" : "§cDisabled";
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§6[Horse Breeding Helper] §rHighlighting " + status + " §8(100 block range, RPG rarity colors + top-2 highlighting)"),
                false
            );
        }
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void renderHorseOutline(HorseEntity horse, MatrixStack matrices, int color, float tickDelta) {
        // This method is called by HorseRenderManager, but we handle rendering in the WorldRenderEvents
        // Just store the horse for tracking
        HIGHLIGHTED_HORSES.put(horse, color);
    }

    public static void clearHorseOutline(HorseEntity horse) {
        horse.setGlowing(false);
        HIGHLIGHTED_HORSES.remove(horse);
    }

    public static void clearAllHighlights() {
        for (HorseEntity horse : HIGHLIGHTED_HORSES.keySet()) {
            horse.setGlowing(false);
        }
        HIGHLIGHTED_HORSES.clear();
    }

    // Helper method to get color for breeding score - RPG style rarity classification
    public static int getScoreColor(double score) {
        return HorseMountHandler.getTierColor(score).getColorValue();
    }
}
