package com.example.controller;

import com.example.model.SensorData;
import com.example.service.SensorRxService;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/sensors/webflux")
public class SensorWebfluxController {

    private final SensorRxService sensorService;

    public SensorWebfluxController(SensorRxService sensorService) {
        this.sensorService = sensorService;
    }

    // Spring webflux
    // Observable: 0..N flows, no backpressure,
    @GetMapping(value = "/observable", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SensorData> streamSensorData() {
        return Flux.from(sensorService.getSensorUpdates().toFlowable(BackpressureStrategy.BUFFER));
    }

    @GetMapping(value = "/flowable", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SensorData> streamFastSensorUpdates() {
        return Flux.from(sensorService.getFastSensorUpdates());
    }

    // Single: a flow of exactly 1 item or an error
    @GetMapping(value = "/single", produces = MediaType.APPLICATION_JSON_VALUE)
    public Single<SensorData> getSingleSensorUpdateAsMono() {
        return sensorService.getSingleSensorUpdate();
    }

    // Maybe: a flow with no items, exactly one item or an error
    @GetMapping(value = "/maybe", produces = MediaType.APPLICATION_JSON_VALUE)
    public Maybe<SensorData> getMaybeSensorUpdateAsMono(boolean flag) {
        return sensorService.getMaybeSensorUpdate(flag);
    }

    // Completable: a flow without items but only a completion or error signal
    @PostMapping(value = "/calibrate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Completable getMaybeSensorUpdateAsMono() {
        return sensorService.calibrateSensors();
    }
}
