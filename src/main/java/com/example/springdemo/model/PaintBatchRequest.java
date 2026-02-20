package com.example.springdemo.model;

import lombok.Data;
import java.util.List;

@Data
public class PaintBatchRequest {
    private String username;
    private List<Pixel> pixels;
    private String color;
}
