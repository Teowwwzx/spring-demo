package com.example.springdemo.repository;

import com.example.springdemo.model.Player;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    /**
     * 🔥 Core Game Logic: Atomic Energy Deduction
     * Deducts specified energy and increments pixel count IF energy >= cost.
     * Returns 1 if successful, 0 if failed (not enough energy).
     */
    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.energy = p.energy - :cost, p.pixelsPainted = p.pixelsPainted + 1 " +
           "WHERE LOWER(p.username) = LOWER(:username) AND p.energy >= :cost")
    int consumeEnergyAtomically(String username, int cost);

    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.energy = CASE WHEN (p.energy + :amount) > p.maxEnergy THEN p.maxEnergy ELSE (p.energy + :amount) END, " +
           "p.lastEnergyUpdate = :now " +
           "WHERE LOWER(p.username) = LOWER(:username)")
    int regenerateEnergyAtomically(String username, int amount, long now);

    Optional<Player> findByUsernameIgnoreCase(String username);

    /**
     * Get top players sorted by pixels painted
     */
    @Query("SELECT p FROM Player p ORDER BY p.pixelsPainted DESC LIMIT 10")
    java.util.List<Player> findTop10ByPixelsPainted();
}
