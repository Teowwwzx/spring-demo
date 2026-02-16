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

    Optional<Player> findByUsernameIgnoreCase(String username);
}
