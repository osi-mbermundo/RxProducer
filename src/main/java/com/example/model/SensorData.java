package com.example.model;

import java.time.LocalDateTime;

public record SensorData(
        String id,
        double temperature,
        double humidity,
        LocalDateTime timestamp) {
}
