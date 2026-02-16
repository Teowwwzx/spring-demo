package com.example.springdemo.factory;

import com.example.springdemo.model.Achievement;

/**
 * 工厂模式：负责产生各种"成就勋章"对象
 */
public class AchievementFactory {

    public enum AchievementType {
        FIRST_PAINT,
        HUNDRED_PIXELS,
        THOUSAND_PIXELS,
        TERRITORY_MASTER,
        SPEED_PAINTER,
        RAINBOW_ARTIST
    }

    /**
     * 创建成就对象
     */
    public static Achievement createAchievement(AchievementType type, String username) {
        Achievement achievement = new Achievement();
        achievement.setUsername(username);
        achievement.setEarnedAt(System.currentTimeMillis());

        switch (type) {
            case FIRST_PAINT:
                achievement.setAchievementType("FIRST_PAINT");
                achievement.setTitle("初次绘画");
                achievement.setDescription("完成你的第一个像素点涂色");
                achievement.setIcon("🎨");
                break;

            case HUNDRED_PIXELS:
                achievement.setAchievementType("HUNDRED_PIXELS");
                achievement.setTitle("百像素画师");
                achievement.setDescription("成功绘制 100 个像素点");
                achievement.setIcon("🖌️");
                break;

            case THOUSAND_PIXELS:
                achievement.setAchievementType("THOUSAND_PIXELS");
                achievement.setTitle("千像素大师");
                achievement.setDescription("成功绘制 1000 个像素点");
                achievement.setIcon("🏆");
                break;

            case TERRITORY_MASTER:
                achievement.setAchievementType("TERRITORY_MASTER");
                achievement.setTitle("领地霸主");
                achievement.setDescription("占领一片 10x10 的完整区域");
                achievement.setIcon("👑");
                break;

            case SPEED_PAINTER:
                achievement.setAchievementType("SPEED_PAINTER");
                achievement.setTitle("闪电画师");
                achievement.setDescription("在 1 秒内完成 10 次涂色");
                achievement.setIcon("⚡");
                break;

            case RAINBOW_ARTIST:
                achievement.setAchievementType("RAINBOW_ARTIST");
                achievement.setTitle("彩虹艺术家");
                achievement.setDescription("使用 7 种不同颜色完成涂色");
                achievement.setIcon("🌈");
                break;

            default:
                achievement.setAchievementType("UNKNOWN");
                achievement.setTitle("未知成就");
                achievement.setDescription("");
                achievement.setIcon("❓");
                break;
        }

        return achievement;
    }
}
