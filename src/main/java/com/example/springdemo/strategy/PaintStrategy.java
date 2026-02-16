package com.example.springdemo.strategy;

import java.util.List;

/**
 * 策略模式：不同的涂色工具接口
 */
public interface PaintStrategy {
    /**
     * 执行涂色操作
     * @param x 起始 x 坐标
     * @param y 起始 y 坐标
     * @param color 颜色
     * @param username 玩家用户名
     * @return 受影响的像素坐标列表 [[x1, y1], [x2, y2], ...]
     */
    List<int[]> paint(int x, int y, String color, String username);

    /**
     * 获取该工具消耗的体力值
     */
    int getEnergyCost();

    /**
     * 获取工具名称
     */
    String getToolName();
}
