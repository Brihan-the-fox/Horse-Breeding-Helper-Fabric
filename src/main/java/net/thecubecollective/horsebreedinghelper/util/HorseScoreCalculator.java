package net.thecubecollective.horsebreedinghelper.util;

import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.attribute.EntityAttributes;

public class HorseScoreCalculator {
    
    // Maximum values for normalization
    private static final double MAX_SPEED = 14.23; // blocks per second
    private static final double MAX_JUMP = 5.5; // blocks
    private static final double MIN_HEALTH = 15.0; // hearts
    private static final double MAX_HEALTH = 30.0; // hearts
    
    public static class HorseScore {
        public final double speedScore;
        public final double jumpScore;
        public final double healthScore;
        public final double totalScore;
        
        public HorseScore(double speedScore, double jumpScore, double healthScore) {
            this.speedScore = speedScore;
            this.jumpScore = jumpScore;
            this.healthScore = healthScore;
            this.totalScore = (speedScore + jumpScore + healthScore) * 100.0 / 3.0;
        }
    }
    
    public static HorseScore calculateScore(HorseEntity horse) {
        // Get horse attributes
        double speed = horse.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
        double jumpStrength = horse.getAttributeValue(EntityAttributes.JUMP_STRENGTH);
        double maxHealth = horse.getAttributeValue(EntityAttributes.MAX_HEALTH);
        
        // Convert jump strength to actual jump height (blocks)
        // The formula for jump height is: -0.1817584952 * x^3 + 3.689713992 * x^2 + 2.128599134 * x - 0.343930367
        // where x is jump strength
        double jumpHeight = -0.1817584952 * Math.pow(jumpStrength, 3) + 
                           3.689713992 * Math.pow(jumpStrength, 2) + 
                           2.128599134 * jumpStrength - 
                           0.343930367;
        
        // Convert speed to blocks per second (multiply by 43.17 to convert from internal units)
        double speedBPS = speed * 43.17;
        
        // Calculate normalized scores (0.0 to 1.0)
        double speedScore = Math.min(speedBPS / MAX_SPEED, 1.0);
        double jumpScore = Math.min(jumpHeight / MAX_JUMP, 1.0);
        double healthScore = Math.max(0.0, Math.min((maxHealth - MIN_HEALTH) / (MAX_HEALTH - MIN_HEALTH), 1.0));
        
        return new HorseScore(speedScore, jumpScore, healthScore);
    }
    
    public static int getScoreColor(double totalScore) {
        if (totalScore < 50) {
            return 0xFF0000; // Red
        } else if (totalScore < 60) {
            return 0x00FF00; // Green
        } else if (totalScore < 70) {
            return 0xFFFF00; // Yellow
        } else if (totalScore < 80) {
            return 0x00FFFF; // Cyan
        } else {
            return 0xFF00FF; // Magenta
        }
    }
}
