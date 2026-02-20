package com.example.springdemo.service;

import com.example.springdemo.context.UserContextHolder;
import com.example.springdemo.model.PaintRequest;
import com.example.springdemo.model.Player;
import com.example.springdemo.repository.PaintRequestRepository;
import com.example.springdemo.strategy.PaintStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PixelPaintServiceTest {

    @InjectMocks
    private PixelPaintService pixelPaintService;

    @Mock
    private PlayerService playerService;

    @Mock
    private AchievementService achievementService;

    @Mock
    private PaintRequestRepository paintRequestRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Map<String, PaintStrategy> paintStrategyMap;

    @Mock
    private PaintStrategy normalBrushStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject the strategy map manually as Mockito won't do it for autowired maps easily
        Map<String, PaintStrategy> strategies = new HashMap<>();
        strategies.put("normalBrush", normalBrushStrategy);
        
        // Use reflection or a setter if available, but here we can just mock the map behavior
        // Actually, PixelPaintService uses @Autowired Map<String, PaintStrategy>
        // We'll use reflection to set it since there's no setter.
        try {
            java.lang.reflect.Field field = PixelPaintService.class.getDeclaredField("paintStrategyMap");
            field.setAccessible(true);
            field.set(pixelPaintService, strategies);
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testPaintPixel_Success() {
        // Arrange
        String requestId = "req123";
        String username = "testUser";
        UserContextHolder.setUser(username);
        
        when(paintRequestRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(normalBrushStrategy.getEnergyCost()).thenReturn(1);
        when(normalBrushStrategy.getToolName()).thenReturn("普通刷子");
        when(playerService.consumeEnergy(username, 1)).thenReturn(true);
        
        ArrayList<int[]> affectedPixels = new ArrayList<>();
        affectedPixels.add(new int[]{10, 20});
        when(normalBrushStrategy.paint(10, 20, "#FF0000", username)).thenReturn(affectedPixels);
        
        Player mockPlayer = new Player();
        mockPlayer.setPixelsPainted(1);
        when(playerService.getPlayer(username)).thenReturn(mockPlayer);

        // Act
        String result = pixelPaintService.paintPixel(requestId, 10, 20, "#FF0000", "normalBrush");

        // Assert
        assertTrue(result.contains("SUCCESS"));
        verify(paintRequestRepository).save(any(PaintRequest.class));
        verify(achievementService).checkAndGrantAchievements(eq(username), anyInt());
        
        UserContextHolder.clear();
    }

    @Test
    void testPaintPixel_NoEnergy() {
        // Arrange
        String requestId = "req456";
        String username = "poorUser";
        UserContextHolder.setUser(username);
        
        when(paintRequestRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(normalBrushStrategy.getEnergyCost()).thenReturn(1);
        when(playerService.consumeEnergy(username, 1)).thenReturn(false);

        // Act
        String result = pixelPaintService.paintPixel(requestId, 10, 20, "#FF0000", "normalBrush");

        // Assert
        assertTrue(result.contains("FAIL: 体力不足"));
        verify(normalBrushStrategy, never()).paint(anyInt(), anyInt(), anyString(), anyString());
        
        UserContextHolder.clear();
    }

    @Test
    void testPaintPixel_Idempotency() {
        // Arrange
        String requestId = "req789";
        String username = "testUser";
        UserContextHolder.setUser(username);
        
        PaintRequest existingRequest = new PaintRequest();
        existingRequest.setResult("SUCCESS: Already done");
        when(paintRequestRepository.findByRequestId(requestId)).thenReturn(Optional.of(existingRequest));

        // Act
        String result = pixelPaintService.paintPixel(requestId, 10, 20, "#FF0000", "normalBrush");

        // Assert
        assertEquals("SUCCESS: Already done", result);
        verify(playerService, never()).consumeEnergy(anyString(), anyInt());
        
        UserContextHolder.clear();
    }
}
