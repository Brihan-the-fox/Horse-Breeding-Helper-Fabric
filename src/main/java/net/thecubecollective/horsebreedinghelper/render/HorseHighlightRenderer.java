package net.thecubecollective.horsebreedinghelper.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.thecubecollective.horsebreedinghelper.util.HorseScoreCalculator;

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
        Vec3d playerPos = client.player.getPos();
        Vec3d cameraPos = context.camera().getPos();
        Set<HorseEntity> currentHorses = new HashSet<>();

        // List to track horses with their scores for finding top 2
        List<Map.Entry<HorseEntity, Double>> horsesWithScores = new ArrayList<>();

        // Find all horses within radius and calculate their scores
        List<Entity> entities = client.world.getOtherEntities(null,
            client.player.getBoundingBox().expand(HIGHLIGHT_RADIUS));

        for (Entity entity : entities) {
            if (entity instanceof HorseEntity horse) {
                double distance = playerPos.distanceTo(horse.getPos());
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

    private static void drawHorseOutlineBox(MatrixStack matrices, HorseEntity horse, int color, Vec3d cameraPos, net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        matrices.push();

        // Get horse position relative to camera
        Vec3d horsePos = horse.getPos();
        double relX = horsePos.x - cameraPos.x;
        double relY = horsePos.y - cameraPos.y;
        double relZ = horsePos.z - cameraPos.z;

        // Get horse bounding box
        Box boundingBox = horse.getBoundingBox();
        double width = boundingBox.maxX - boundingBox.minX;
        double height = boundingBox.maxY - boundingBox.minY;
        double depth = boundingBox.maxZ - boundingBox.minZ;

        // Extract color components
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float alpha = 0.4f; // Semi-transparent

        // Position the box
        matrices.translate(relX - width/2, relY, relZ - depth/2);

        try {
            // Try using VertexRendering.drawFilledBox first
            VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getDebugFilledBox());
            VertexRendering.drawFilledBox(
                matrices,
                buffer,
                0.0f, 0.0f, 0.0f,
                (float)width, (float)height, (float)depth,
                red, green, blue, alpha
            );

        } catch (Exception e1) {
            // Fallback: Use basic glow
            horse.setGlowing(true);
        }

        matrices.pop();
    }

    private static void drawTopHorseHighlight(MatrixStack matrices, HorseEntity horse, Vec3d cameraPos, net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext context) {
        matrices.push();

        // Get horse position relative to camera
        Vec3d horsePos = horse.getPos();
        double relX = horsePos.x - cameraPos.x;
        double relY = horsePos.y - cameraPos.y;
        double relZ = horsePos.z - cameraPos.z;

        // Get horse bounding box and make it slightly larger
        Box boundingBox = horse.getBoundingBox();
        double width = (boundingBox.maxX - boundingBox.minX) * 1.15; // 15% larger for better visibility
        double height = (boundingBox.maxY - boundingBox.minY) * 1.15;
        double depth = (boundingBox.maxZ - boundingBox.minZ) * 1.15;

        // White color for top horses with slight pulsing effect
        long time = System.currentTimeMillis();
        float pulse = (float)(0.5 + 0.3 * Math.sin(time * 0.005)); // Gentle pulsing

        // Position the box (centered)
        matrices.translate(relX - width/2, relY - (height - (boundingBox.maxY - boundingBox.minY))/2, relZ - depth/2);

        try {
            // Use filled box rendering with pulsing white outline effect
            float alpha = 0.2f + pulse * 0.3f; // Pulsing alpha between 0.2 and 0.5

            // Draw a slightly transparent white box
            VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getDebugFilledBox());
            VertexRendering.drawFilledBox(matrices, buffer,
                0, 0, 0,
                (float)width, (float)height, (float)depth,
                1.0f, 1.0f, 1.0f, alpha); // White with pulsing alpha

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
        if (score >= 80) return 0xFFFF00FF; // Magenta for Legendary horses (80-100)
        if (score >= 70) return 0xFF87CEEB; // Light Blue for Rare horses (70-79)
        if (score >= 60) return 0xFFFFFF00; // Yellow for Uncommon horses (60-69)
        if (score >= 50) return 0xFF00FF00; // Green for Common horses (50-59)
        return 0xFFFF0000; // Red for Poor horses (<50)
    }
}
