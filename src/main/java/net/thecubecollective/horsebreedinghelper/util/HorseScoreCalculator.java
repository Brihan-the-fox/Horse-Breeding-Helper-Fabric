package net.thecubecollective.horsebreedinghelper.util;

import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.attribute.EntityAttributes;

public class HorseScoreCalculator {
    
    // Updated maximum values based on OFFICIAL Minecraft horse mechanics from the wiki
    private static final double MAX_SPEED = 0.3375; // Internal speed units (confirmed from wiki)
    private static final double MIN_SPEED = 0.1125; // Minimum horse speed (confirmed from wiki)
    private static final double MAX_JUMP = 1.0; // Internal jump strength units (confirmed from wiki)
    private static final double MIN_JUMP = 0.4; // Minimum horse jump (confirmed from wiki)
    private static final double MIN_HEALTH = 15.0; // hearts (confirmed from wiki)
    private static final double MAX_HEALTH = 30.0; // hearts (confirmed from wiki)
    
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
        // Get horse attributes (using internal Minecraft units directly)
        double speed = horse.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
        double jumpStrength = horse.getAttributeValue(EntityAttributes.JUMP_STRENGTH);
        double maxHealth = horse.getAttributeValue(EntityAttributes.MAX_HEALTH);
        
        // Calculate normalized scores using proper ranges (0.0 to 1.0)
        // Speed: normalize between minimum and maximum possible horse speeds
        double speedScore = Math.max(0.0, Math.min(1.0, (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED)));
        
        // Jump: normalize between minimum and maximum possible horse jump strengths  
        double jumpScore = Math.max(0.0, Math.min(1.0, (jumpStrength - MIN_JUMP) / (MAX_JUMP - MIN_JUMP)));
        
        // Health: normalize between minimum and maximum possible horse health
        double healthScore = Math.max(0.0, Math.min(1.0, (maxHealth - MIN_HEALTH) / (MAX_HEALTH - MIN_HEALTH)));
        
        return new HorseScore(speedScore, jumpScore, healthScore);
    }
    
    /**
     * Debug method to get detailed breakdown of horse scoring
     */
    public static String getScoreBreakdown(HorseEntity horse) {
        double speed = horse.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
        double jumpStrength = horse.getAttributeValue(EntityAttributes.JUMP_STRENGTH);
        double maxHealth = horse.getAttributeValue(EntityAttributes.MAX_HEALTH);
        
        // Convert to display units for debugging
        double speedBPS = speed * 43.17; // Convert to blocks per second for display
        double jumpHeight = -0.1817584952 * Math.pow(jumpStrength, 3) + 
                           3.689713992 * Math.pow(jumpStrength, 2) + 
                           2.128599134 * jumpStrength - 
                           0.343930367; // Convert to jump height for display
        
        double speedScore = Math.max(0.0, Math.min(1.0, (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED)));
        double jumpScore = Math.max(0.0, Math.min(1.0, (jumpStrength - MIN_JUMP) / (MAX_JUMP - MIN_JUMP)));
        double healthScore = Math.max(0.0, Math.min(1.0, (maxHealth - MIN_HEALTH) / (MAX_HEALTH - MIN_HEALTH)));
        
        return String.format("Speed: %.4f internal (%.1f BPS) -> %.2f | Jump: %.3f internal (%.1f blocks) -> %.2f | Health: %.1f hearts -> %.2f",
                speed, speedBPS, speedScore, jumpStrength, jumpHeight, jumpScore, maxHealth, healthScore);
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
