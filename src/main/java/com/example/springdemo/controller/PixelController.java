package com.example.springdemo.controller;

import com.example.springdemo.context.UserContextHolder;
import com.example.springdemo.model.PaintBatchRequest;
import com.example.springdemo.service.PixelPaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 像素绘画控制器
 */
@RestController
@RequestMapping("/api/pixel")
public class PixelController {

    @Autowired
    private PixelPaintService pixelPaintService;

    /**
     * 绘制像素
     * @param username 用户名
     * @param x X坐标
     * @param y Y坐标
     * @param color 颜色
     * @param tool 工具类型
     * @param requestId 请求ID
     */
    @PostMapping("/paint")
    public String paintPixel(
            @RequestParam String username,
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam String color,
            @RequestParam(defaultValue = "normalBrush") String tool,
            @RequestParam(required = false) String requestId
    ) {
        try {
            UserContextHolder.setUser(username);

            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            return pixelPaintService.paintPixel(requestId, x, y, color, tool);
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * 批量绘制像素 (用于平滑画笔)
     */
    @PostMapping("/paint-batch")
    public String paintBatch(@RequestBody PaintBatchRequest request) {
        if (request == null || request.getPixels() == null) {
            return "ERROR: 像素列表不能为空";
        }
        
        try {
            UserContextHolder.setUser(request.getUsername());
            
            java.util.List<int[]> coords = request.getPixels().stream()
                .filter(p -> p != null && p.getX() != null && p.getY() != null)
                .map(p -> new int[]{p.getX(), p.getY()})
                .collect(Collectors.toList());

            if (coords.isEmpty()) {
                return "ERROR: 无效的像素数据";
            }

            return pixelPaintService.paintBatch(request.getUsername(), coords, request.getColor());
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * 快速测试接口
     */
    @GetMapping("/paint-simple")
    public String paintSimple(
            @RequestParam String username,
            @RequestParam int x,
            @RequestParam int y,
            @RequestParam(defaultValue = "#FF0000") String color
    ) {
        return paintPixel(username, x, y, color, "normalBrush", null);
    }
}
