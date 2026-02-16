package com.example.springdemo.strategy;

import com.example.springdemo.model.Pixel;
import com.example.springdemo.repository.PixelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 炸弹：涂 3x3 区域
 */
@Component("bomb")
public class BombStrategy implements PaintStrategy {

    @Autowired
    private PixelRepository pixelRepository;

    @Override
    public List<int[]> paint(int x, int y, String color, String username) {
        List<int[]> affected = new ArrayList<>();

        // 涂 3x3 区域 (以 x,y 为中心)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = x + dx;
                int newY = y + dy;

                if (isValidCoordinate(newX, newY)) {
                    paintPixel(newX, newY, color, username);
                    affected.add(new int[]{newX, newY});
                }
            }
        }

        return affected;
    }

    @Override
    public int getEnergyCost() {
        return 5;
    }

    @Override
    public String getToolName() {
        return "炸弹 (3x3)";
    }

    private void paintPixel(int x, int y, String color, String username) {
        Optional<Pixel> existingPixel = pixelRepository.findByXAndY(x, y);
        Pixel pixel;

        if (existingPixel.isPresent()) {
            pixel = existingPixel.get();
            pixel.setColor(color);
            pixel.setPaintedBy(username);
            pixel.setPaintedAt(System.currentTimeMillis());
        } else {
            pixel = new Pixel();
            pixel.setX(x);
            pixel.setY(y);
            pixel.setColor(color);
            pixel.setPaintedBy(username);
            pixel.setPaintedAt(System.currentTimeMillis());
        }

        pixelRepository.save(pixel);
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 1000 && y >= 0 && y < 1000;
    }
}
