package net.thecubecollective.horsebreedinghelper.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.thecubecollective.horsebreedinghelper.render.HorseHighlightRenderer;
import net.thecubecollective.horsebreedinghelper.util.HorseScoreCalculator;

public class HorseMountHandler {
    private static HorseEntity lastMountedHorse = null;
    private static boolean wasRiding = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                wasRiding = false;
                lastMountedHorse = null;
                return;
            }

            boolean isCurrentlyRiding = client.player.hasVehicle();
            Entity vehicle = client.player.getVehicle();

            // Check if player just mounted a horse
            if (isCurrentlyRiding && !wasRiding && vehicle instanceof HorseEntity horse) {
                // Player just mounted a horse
                showHorseScore(horse);
                lastMountedHorse = horse;
            } else if (!isCurrentlyRiding && wasRiding) {
                // Player dismounted
                lastMountedHorse = null;
            } else if (isCurrentlyRiding && vehicle instanceof HorseEntity horse && horse != lastMountedHorse) {
                // Player switched to a different horse
                showHorseScore(horse);
                lastMountedHorse = horse;
            }

            wasRiding = isCurrentlyRiding;
        });
    }

    private static void showHorseScore(HorseEntity horse) {
        try {
            HorseScoreCalculator.HorseScore score = HorseScoreCalculator.calculateScore(horse);
            String tierName = getTierName(score.totalScore);
            Formatting color = getTierColor(score.totalScore);
            double diamondValue = calculateDiamondValue(score.totalScore);

            // Calculate individual scores (0-100)
            double sScore = score.speedScore * 100.0;
            double jScore = score.jumpScore * 100.0;
            double hScore = score.healthScore * 100.0;

            // Create formatted message with color and diamond value
            MutableText scoreText = Text.literal("Horse Score: ")
                    .append(Text.literal(String.format("%.1f", score.totalScore))
                            .formatted(color, Formatting.BOLD))
                    .append(Text.literal(" (")
                            .append(Text.literal(tierName).formatted(color))
                            .append(")"));

            // Add individual stats
            scoreText.append(Text.literal(" |"));
            scoreText.append(Text.literal(" Speed: "))
                    .append(Text.literal(String.format("%.0f", sScore))
                            .formatted(getTierColor(sScore)));
            scoreText.append(Text.literal(" Jump: "))
                    .append(Text.literal(String.format("%.0f", jScore))
                            .formatted(getTierColor(jScore)));
            scoreText.append(Text.literal(" Health: "))
                    .append(Text.literal(String.format("%.0f", hScore))
                            .formatted(getTierColor(hScore)));

            // Add diamond value
            scoreText.append(Text.literal(" | Value: "));
            if (diamondValue > 0) {
                scoreText.append(Text.literal(String.format("%.2f", diamondValue) + " 💎")
                        .formatted(Formatting.GOLD, Formatting.BOLD));
            } else {
                scoreText.append(Text.literal("Worthless")
                        .formatted(Formatting.DARK_GRAY));
            }

            // Send message to player
            MinecraftClient.getInstance().player.sendMessage(scoreText, true); // true = action bar

            // Debug: Send detailed breakdown to chat ONLY if F6 highlighting is enabled
            if (HorseHighlightRenderer.isEnabled()) {
                String breakdown = HorseScoreCalculator.getScoreBreakdown(horse);
                MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("DEBUG: " + breakdown).formatted(Formatting.GRAY), false
                );
            }

        } catch (Exception e) {
            // Fallback message if score calculation fails
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("Mounted horse!").formatted(Formatting.GRAY), true
            );
        }
    }

    private static String getTierName(double totalScore) {
        if (totalScore >= 90) {
            return "Legendary";
        } else if (totalScore >= 80) {
            return "Epic";
        } else if (totalScore >= 70) {
            return "Rare";
        } else if (totalScore >= 60) {
            return "Good";
        } else if (totalScore >= 50) {
            return "Common";
        } else {
            return "Poor";
        }
    }

    public static Formatting getTierColor(double totalScore) {
        if (totalScore >= 90) {
            return Formatting.GOLD; // Legendary
        } else if (totalScore >= 80) {
            return Formatting.LIGHT_PURPLE; // Epic
        } else if (totalScore >= 70) {
            return Formatting.BLUE; // Rare
        } else if (totalScore >= 60) {
            return Formatting.GREEN; // Good
        } else if (totalScore >= 50) {
            return Formatting.GRAY; // Common
        } else {
            return Formatting.DARK_GRAY; // Poor
        }
    }

    /**
     * Calculates the diamond value of a horse based on its score using an exponential curve.
     * Formula: value = 100 * ((score - 50) / 50)^3.5 for scores >= 50, 0 for scores < 50
     *
     * This creates a slower start with explosive growth at high scores:
     * - Score 50: 0.00 diamonds (worthless)
     * - Score 60: 0.13 diamonds
     * - Score 70: 0.76 diamonds
     * - Score 80: 2.90 diamonds
     * - Score 90: 10.56 diamonds
     * - Score 100: 100.00 diamonds
     */
    private static double calculateDiamondValue(double score) {
        if (score < 50.0) {
            return 0.0; // Horses under 50 are worthless
        }

        // Normalize score to 0-1 range (50-100 becomes 0-1)
        double normalizedScore = (score - 50.0) / 50.0;

        // Apply steeper exponential curve: value = 100 * normalized^3.5
        double value = 100.0 * Math.pow(normalizedScore, 3.5);

        // Round to nearest 0.01 (2 decimal places)
        return Math.round(value * 100.0) / 100.0;
    }
}
