package com.example.springdemo.service;

import com.example.springdemo.context.GlobalEventManager;
import com.example.springdemo.model.Player;
import com.example.springdemo.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GlobalEventManager globalEventManager;

    // 常量配置
    private static final int MAX_ENERGY = 100;
    private static final int ENERGY_REGEN_RATE = 1; // 每秒恢复1点体力
    private static final long ENERGY_REGEN_INTERVAL = 1000; // 1秒

    /**
     * 注册新玩家 (不区分大小写)
     */
    @Transactional
    public Player registerPlayer(String username) {
        String normalizedName = username.toLowerCase();
        Optional<Player> existing = playerRepository.findByUsernameIgnoreCase(normalizedName);
        if (existing.isPresent()) {
            return existing.get();
        }

        Player player = new Player();
        player.setUsername(normalizedName);
        player.setEnergy(MAX_ENERGY);
        player.setMaxEnergy(MAX_ENERGY);
        player.setPixelsPainted(0);
        player.setLastEnergyUpdate(System.currentTimeMillis());
        player.setCreatedAt(System.currentTimeMillis());
        return playerRepository.save(player);
    }

    /**
     * 获取玩家信息（自动恢复体力）
     */
    public Player getPlayer(String username) {
        Optional<Player> playerOpt = playerRepository.findByUsernameIgnoreCase(username);
        if (playerOpt.isEmpty()) {
            return registerPlayer(username);
        }

        Player player = playerOpt.get();
        regenerateEnergy(player);
        return player;
    }

    /**
     * 消耗体力（支持全局活动：免费绘画）
     * 使用原子更新，防止高并发下的超卖问题
     * @return true if successful, false if not enough energy
     */
    @Transactional
    public boolean consumeEnergy(String username, int energyCost) {
        // 检查是否有免费绘画活动
        if (globalEventManager.isFreeDrawEvent()) {
            System.out.println("🎨 免费绘画活动中，不消耗体力！");
            return true;
        }

        // 先执行一次体力恢复（可选，取决于是否需要实时看到恢复后的体力）
        getPlayer(username);

        // 使用原子 SQL 扣减
        int updatedRows = playerRepository.consumeEnergyAtomically(username, energyCost);
        return updatedRows > 0;
    }

    /**
     * 增加玩家绘制的像素计数
     */
    @Transactional
    public void incrementPixelsPainted(String username, int count) {
        Optional<Player> playerOpt = playerRepository.findByUsernameIgnoreCase(username);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setPixelsPainted(player.getPixelsPainted() + count);
            playerRepository.save(player);
        }
    }

    /**
     * 自动恢复体力
     */
    private void regenerateEnergy(Player player) {
        if (player.getEnergy() >= player.getMaxEnergy()) {
            player.setLastEnergyUpdate(System.currentTimeMillis());
            return;
        }

        long now = System.currentTimeMillis();
        long timePassed = now - player.getLastEnergyUpdate();
        int energyToRecover = (int) (timePassed / ENERGY_REGEN_INTERVAL) * ENERGY_REGEN_RATE;

        // 检查是否有双倍体力活动
        if (globalEventManager.isDoubleEnergyEvent()) {
            energyToRecover *= 2;
            System.out.println("🎉 双倍体力恢复！");
        }

        if (energyToRecover > 0) {
            int newEnergy = Math.min(player.getEnergy() + energyToRecover, player.getMaxEnergy());
            player.setEnergy(newEnergy);
            player.setLastEnergyUpdate(now);
            playerRepository.save(player);
        }
    }
}
