package net.thecubecollective.horsebreedinghelper.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.Vec3d;
import net.thecubecollective.horsebreedinghelper.util.HorseScoreCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HorseRenderManager {

    private static final double HIGHLIGHT_RADIUS = 100.0;
    private static final Set<HorseEntity> highlightedHorses = new HashSet<>();

    public static void renderHorseHighlights(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Vec3d playerPos = new Vec3d(client.player.getX(), client.player.getY(), client.player.getZ());
        Set<HorseEntity> currentHorses = new HashSet<>();

        // Find all horses within radius
        List<Entity> entities = client.world.getOtherEntities(null,
            client.player.getBoundingBox().expand(HIGHLIGHT_RADIUS));

        for (Entity entity : entities) {
            if (entity instanceof HorseEntity horse) {
                double distance = playerPos.distanceTo(horse.getPos());
                if (distance <= HIGHLIGHT_RADIUS) {
                    currentHorses.add(horse);

                    // Calculate horse score
                    HorseScoreCalculator.HorseScore score = HorseScoreCalculator.calculateScore(horse);
                    int color = HorseScoreCalculator.getScoreColor(score.totalScore);

                    // Apply highlight
                    HorseHighlightRenderer.renderHorseOutline(horse, matrices, color, tickDelta);
                }
            }
        }

        // Remove highlights from horses that are no longer in range
        highlightedHorses.removeIf(horse -> {
            if (!currentHorses.contains(horse)) {
                HorseHighlightRenderer.clearHorseOutline(horse);
                return true;
            }
            return false;
        });

        // Add new horses to highlighted set
        highlightedHorses.addAll(currentHorses);
    }

    public static void clearAllHighlights() {
        for (HorseEntity horse : highlightedHorses) {
            HorseHighlightRenderer.clearHorseOutline(horse);
        }
        highlightedHorses.clear();
    }
}
