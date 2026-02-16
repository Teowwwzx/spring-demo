package com.example.springdemo.strategy;

import com.example.springdemo.model.Pixel;
import com.example.springdemo.repository.PixelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 油漆桶：触发 BFS (广度优先搜索) 算法进行连通区域填色
 */
@Component("paintBucket")
public class PaintBucketStrategy implements PaintStrategy {

    @Autowired
    private PixelRepository pixelRepository;

    // 限制最大填充数量，防止恶意刷屏
    private static final int MAX_FILL = 100;

    @Override
    public List<int[]> paint(int x, int y, String color, String username) {
        List<int[]> affected = new ArrayList<>();

        if (!isValidCoordinate(x, y)) {
            return affected;
        }

        // 获取起始点的颜色
        Optional<Pixel> startPixelOpt = pixelRepository.findByXAndY(x, y);
        String targetColor = startPixelOpt.map(Pixel::getColor).orElse(null);

        // 如果目标颜色和新颜色相同，不需要填充
        if (color.equals(targetColor)) {
            return affected;
        }

        // BFS 算法进行连通区域填色
        Queue<int[]> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.offer(new int[]{x, y});
        visited.add(x + "," + y);

        // 四个方向：上、下、左、右
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (!queue.isEmpty() && affected.size() < MAX_FILL) {
            int[] current = queue.poll();
            int curX = current[0];
            int curY = current[1];

            // 获取当前像素的颜色
            Optional<Pixel> currentPixelOpt = pixelRepository.findByXAndY(curX, curY);
            String currentColor = currentPixelOpt.map(Pixel::getColor).orElse(null);

            // 如果当前颜色与目标颜色相同，则填充
            if ((targetColor == null && currentColor == null) ||
                (targetColor != null && targetColor.equals(currentColor))) {

                paintPixel(curX, curY, color, username);
                affected.add(new int[]{curX, curY});

                // 探索四个方向
                for (int[] dir : directions) {
                    int newX = curX + dir[0];
                    int newY = curY + dir[1];
                    String key = newX + "," + newY;

                    if (isValidCoordinate(newX, newY) && !visited.contains(key)) {
                        visited.add(key);
                        queue.offer(new int[]{newX, newY});
                    }
                }
            }
        }

        return affected;
    }

    @Override
    public int getEnergyCost() {
        return 10;
    }

    @Override
    public String getToolName() {
        return "油漆桶 (BFS填充)";
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
