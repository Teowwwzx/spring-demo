package com.example.springdemo.service;

import com.example.springdemo.context.UserContextHolder;
import com.example.springdemo.model.PaintRequest;
import com.example.springdemo.model.Player;
import com.example.springdemo.repository.PaintRequestRepository;
import com.example.springdemo.strategy.PaintStrategy;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 像素绘画核心服务
 * 包含幂等性处理、体力消耗、Redis缓存等
 */
@Service
public class PixelPaintService {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private PaintRequestRepository paintRequestRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Autowired
    private Map<String, PaintStrategy> paintStrategyMap; // Spring自动注入所有PaintStrategy实现

    /**
     * 执行绘画操作（带幂等性保证）
     * @param requestId 请求唯一ID（用于幂等）
     * @param x X坐标
     * @param y Y坐标
     * @param color 颜色
     * @param toolType 工具类型：normalBrush, bomb, paintBucket
     * @return 结果消息
     */
    @Transactional
    public String paintPixel(String requestId, int x, int y, String color, String toolType) {
        // 从 ThreadLocal 获取当前用户
        String username = UserContextHolder.getUser();
        if (username == null || username.isEmpty()) {
            return "ERROR: 用户未登录";
        }

        // 1. 幂等性检查：检查这个 requestId 是否已经处理过
        Optional<PaintRequest> existingRequest = paintRequestRepository.findByRequestId(requestId);
        if (existingRequest.isPresent()) {
            System.out.println("⚠️ 重复请求，requestId: " + requestId + " 已处理过");
            return existingRequest.get().getResult();
        }

        // 2. 获取绘画策略
        PaintStrategy strategy = paintStrategyMap.get(toolType);
        if (strategy == null) {
            return "ERROR: 未知的工具类型: " + toolType;
        }

        // 3. 检查并消耗体力
        int energyCost = strategy.getEnergyCost();
        boolean hasEnergy = playerService.consumeEnergy(username, energyCost);

        if (!hasEnergy) {
            String result = "FAIL: 体力不足 (需要 " + energyCost + " 点体力)";
            savePaintRequest(requestId, username, toolType, result);
            return result;
        }

        // 4. 执行绘画操作
        try {
            List<int[]> affectedPixels = strategy.paint(x, y, color, username);

            if (affectedPixels.isEmpty()) {
                String result = "FAIL: 无效的坐标或无法绘制";
                savePaintRequest(requestId, username, toolType, result);
                return result;
            }

            // 5. 更新玩家统计已经在 consumeEnergyAtomically 中完成了原子增加
            int pixelCount = affectedPixels.size();

            // 6. 检查成就
            Player player = playerService.getPlayer(username);
            achievementService.checkAndGrantAchievements(username, player.getPixelsPainted());

            // 7. 更新 Redis 缓存（异步）
            updateRedisCache(affectedPixels, color, username);

            // 8. 通过 WebSocket 广播更新
            broadcastUpdates(affectedPixels, color);

            String result = "SUCCESS: 使用 " + strategy.getToolName() + " 绘制了 " + pixelCount + " 个像素";
            savePaintRequest(requestId, username, toolType, result);

            System.out.println("✅ " + username + " " + result);
            return result;

        } catch (Exception e) {
            String result = "ERROR: " + e.getMessage();
            savePaintRequest(requestId, username, toolType, result);
            return result;
        }
    }

    /**
     * 批量绘制像素 (用于平滑画笔)
     */
    @Transactional
    public String paintBatch(String username, List<int[]> coordinates, String color) {
        if (username == null || username.isEmpty()) return "ERROR: 用户未登录";
        
        // 批量扣除体力 (1个点1点体力)
        int totalCost = coordinates.size();
        boolean hasEnergy = playerService.consumeEnergy(username, totalCost);
        
        if (!hasEnergy) return "FAIL: 体力不足 (需要 " + totalCost + " 点)";

        // 批量更新
        updateRedisCache(coordinates, color, username);
        broadcastUpdates(coordinates, color);
        
        return "SUCCESS: 绘制了 " + totalCost + " 个像素";
    }

    /**
     * 保存请求记录（幂等性）
     */
    private void savePaintRequest(String requestId, String username, String tool, String result) {
        PaintRequest paintRequest = new PaintRequest();
        paintRequest.setRequestId(requestId);
        paintRequest.setUsername(username);
        paintRequest.setTool(tool);
        paintRequest.setResult(result);
        paintRequest.setCreatedAt(System.currentTimeMillis());
        paintRequestRepository.save(paintRequest);
    }

    /**
     * 更新 Redis 缓存
     */
    private void updateRedisCache(List<int[]> pixels, String color, String username) {
        try {
            for (int[] pixel : pixels) {
                String key = "pixel:" + pixel[0] + ":" + pixel[1];
                String value = color + "|" + username + "|" + System.currentTimeMillis();
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Redis 更新失败: " + e.getMessage());
        }
    }

    /**
     * 通过 WebSocket 广播像素更新
     */
    private void broadcastUpdates(List<int[]> pixels, String color) {
        for (int[] pixel : pixels) {
            Map<String, Object> update = new java.util.HashMap<>();
            update.put("x", pixel[0]);
            update.put("y", pixel[1]);
            update.put("color", color);
            messagingTemplate.convertAndSend("/topic/pixel-update", (Object) update);
        }
    }
}
