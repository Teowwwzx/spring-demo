package com.example.springdemo.repository;

import com.example.springdemo.model.Pixel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PixelRepository extends JpaRepository<Pixel, Long> {
    Optional<Pixel> findByXAndY(Integer x, Integer y);

    @Query("SELECT p FROM Pixel p WHERE p.x BETWEEN :xStart AND :xEnd AND p.y BETWEEN :yStart AND :yEnd")
    List<Pixel> findByRegion(Integer xStart, Integer xEnd, Integer yStart, Integer yEnd);

    long countByPaintedBy(String username);
}
