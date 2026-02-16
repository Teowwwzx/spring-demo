package com.example.springdemo.controller;

import com.example.springdemo.model.Pixel;
import com.example.springdemo.repository.PixelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 画布控制器
 */
@RestController
@RequestMapping("/api/canvas")
public class CanvasController {

    @Autowired
    private PixelRepository pixelRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取指定区域的像素
     * @param xStart 起始X坐标
     * @param xEnd 结束X坐标
     * @param yStart 起始Y坐标
     * @param yEnd 结束Y坐标
     */
    @GetMapping("/region")
    public List<Map<String, Object>> getRegion(
            @RequestParam int xStart,
            @RequestParam int xEnd,
            @RequestParam int yStart,
            @RequestParam int yEnd
    ) {
        List<Pixel> pixels = pixelRepository.findByRegion(xStart, xEnd, yStart, yEnd);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Pixel pixel : pixels) {
            Map<String, Object> pixelData = new HashMap<>();
            pixelData.put("x", pixel.getX());
            pixelData.put("y", pixel.getY());
            pixelData.put("color", pixel.getColor());
            pixelData.put("paintedBy", pixel.getPaintedBy());
            result.add(pixelData);
        }

        return result;
    }

    /**
     * 获取画布统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getCanvasStats() {
        long totalPixels = pixelRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPixelsPainted", totalPixels);
        stats.put("canvasSize", "1000x1000");
        stats.put("totalPixelsAvailable", 1000000);
        stats.put("percentagePainted", (totalPixels * 100.0) / 1000000);

        return stats;
    }

    /**
     * 获取单个像素信息（从Redis或数据库）
     */
    @GetMapping("/pixel")
    public Map<String, Object> getPixel(@RequestParam int x, @RequestParam int y) {
        Map<String, Object> result = new HashMap<>();

        // 先从 Redis 查询
        try {
            String key = "pixel:" + x + ":" + y;
            String value = redisTemplate.opsForValue().get(key);

            if (value != null) {
                String[] parts = value.split("\\|");
                result.put("x", x);
                result.put("y", y);
                result.put("color", parts[0]);
                result.put("paintedBy", parts[1]);
                result.put("paintedAt", Long.parseLong(parts[2]));
                result.put("source", "redis");
                return result;
            }
        } catch (Exception e) {
            System.err.println("Redis 查询失败: " + e.getMessage());
        }

        // Redis 没有，从数据库查询
        return pixelRepository.findByXAndY(x, y)
                .map(pixel -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x", pixel.getX());
                    map.put("y", pixel.getY());
                    map.put("color", pixel.getColor());
                    map.put("paintedBy", pixel.getPaintedBy());
                    map.put("paintedAt", pixel.getPaintedAt());
                    map.put("source", "database");
                    return map;
                })
                .orElseGet(() -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x", x);
                    map.put("y", y);
                    map.put("color", null);
                    map.put("message", "Pixel not painted yet");
                    return map;
                });
    }
}
