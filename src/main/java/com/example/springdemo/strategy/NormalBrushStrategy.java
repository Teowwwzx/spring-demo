package com.example.springdemo.strategy;

import com.example.springdemo.model.Pixel;
import com.example.springdemo.repository.PixelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 普通刷子：涂 1 个点
 */
@Component("normalBrush")
public class NormalBrushStrategy implements PaintStrategy {

    @Autowired
    private PixelRepository pixelRepository;

    @Override
    public List<int[]> paint(int x, int y, String color, String username) {
        List<int[]> affected = new ArrayList<>();

        if (!isValidCoordinate(x, y)) {
            return affected;
        }

        paintPixel(x, y, color, username);
        affected.add(new int[]{x, y});

        return affected;
    }

    @Override
    public int getEnergyCost() {
        return 1;
    }

    @Override
    public String getToolName() {
        return "普通刷子";
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
