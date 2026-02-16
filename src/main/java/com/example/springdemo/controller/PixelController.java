package com.example.springdemo.controller;

import com.example.springdemo.context.UserContextHolder;
import com.example.springdemo.service.PixelPaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
     * @param username 用户名（实际项目中应该从token或session获取）
     * @param x X坐标
     * @param y Y坐标
     * @param color 颜色（十六进制，如 #FF0000）
     * @param tool 工具类型：normalBrush, bomb, paintBucket
     * @param requestId 请求ID（可选，用于幂等性）
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
            // 设置 ThreadLocal 用户上下文
            UserContextHolder.setUser(username);

            // 如果没有提供 requestId，自动生成一个
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            // 执行绘画
            String result = pixelPaintService.paintPixel(requestId, x, y, color, tool);

            return result;

        } finally {
            // 清除 ThreadLocal，防止内存泄漏
            UserContextHolder.clear();
        }
    }

    /**
     * 快速测试接口（简化版）
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
