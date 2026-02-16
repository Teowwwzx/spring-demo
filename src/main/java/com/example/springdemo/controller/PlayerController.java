package com.example.springdemo.controller;

import com.example.springdemo.model.Achievement;
import com.example.springdemo.model.Player;
import com.example.springdemo.service.AchievementService;
import com.example.springdemo.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家管理控制器
 */
@RestController
@RequestMapping("/api/player")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private AchievementService achievementService;

    /**
     * 注册新玩家
     */
    @PostMapping("/register")
    public Player registerPlayer(@RequestParam String username) {
        return playerService.registerPlayer(username);
    }

    /**
     * 获取玩家信息
     */
    @GetMapping("/info")
    public Map<String, Object> getPlayerInfo(@RequestParam String username) {
        Player player = playerService.getPlayer(username);
        List<Achievement> achievements = achievementService.getPlayerAchievements(username);

        Map<String, Object> result = new HashMap<>();
        result.put("player", player);
        result.put("achievements", achievements);
        return result;
    }

    /**
     * 获取玩家成就
     */
    @GetMapping("/achievements")
    public List<Achievement> getPlayerAchievements(@RequestParam String username) {
        return achievementService.getPlayerAchievements(username);
    }
}
