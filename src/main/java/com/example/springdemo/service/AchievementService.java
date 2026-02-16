package com.example.springdemo.service;

import com.example.springdemo.factory.AchievementFactory;
import com.example.springdemo.model.Achievement;
import com.example.springdemo.repository.AchievementRepository;
import com.example.springdemo.repository.PixelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 成就系统服务
 */
@Service
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private PixelRepository pixelRepository;

    /**
     * 检查并授予玩家成就
     */
    public void checkAndGrantAchievements(String username, int totalPixelsPainted) {
        // 第一次绘画成就
        if (totalPixelsPainted == 1) {
            grantAchievement(username, AchievementFactory.AchievementType.FIRST_PAINT);
        }

        // 百像素画师
        if (totalPixelsPainted == 100) {
            grantAchievement(username, AchievementFactory.AchievementType.HUNDRED_PIXELS);
        }

        // 千像素大师
        if (totalPixelsPainted == 1000) {
            grantAchievement(username, AchievementFactory.AchievementType.THOUSAND_PIXELS);
        }
    }

    /**
     * 授予成就（使用工厂模式创建）
     */
    private void grantAchievement(String username, AchievementFactory.AchievementType type) {
        // 检查是否已经获得该成就
        String typeString = type.name();
        Optional<Achievement> existing = achievementRepository.findByUsernameAndAchievementType(username, typeString);

        if (existing.isEmpty()) {
            Achievement achievement = AchievementFactory.createAchievement(type, username);
            achievementRepository.save(achievement);
            System.out.println("🎉 " + username + " 获得成就: " + achievement.getIcon() + " " + achievement.getTitle());
        }
    }

    /**
     * 获取玩家的所有成就
     */
    public List<Achievement> getPlayerAchievements(String username) {
        return achievementRepository.findByUsername(username);
    }
}
