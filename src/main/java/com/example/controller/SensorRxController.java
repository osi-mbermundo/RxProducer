package com.example.controller;

import com.example.model.SensorData;
import com.example.service.SensorRxService;
import io.reactivex.rxjava3.core.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sensors/rx")
public class SensorRxController {

    private final SensorRxService sensorService;

    public SensorRxController(SensorRxService sensorService) {
        this.sensorService = sensorService;
    }

    // RxJava
    // We cannot directly expose an Observable object thru REST call, since it normal REST is a request-response
    // Observable: 0..N flows, no backpressure,
    @GetMapping(value = "/observable", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Observable<SensorData> getSensorUpdates() {
        return sensorService.getSensorUpdates();
    }

    // Flowable: 0..N flows, supporting Reactive-Streams and backpressure
    @GetMapping(value = "/flowable", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flowable<SensorData> getFastSensorUpdates() {
        return sensorService.getFastSensorUpdates();
    }

    // Single: a flow of exactly 1 item or an error
    @GetMapping(value = "/single", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Single<SensorData> getSingleSensorUpdate() {
        return sensorService.getSingleSensorUpdate();
    }

    // Maybe: a flow with no items, exactly one item or an error
    @GetMapping(value = "/maybe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Maybe<SensorData> getMaybeSensorUpdate(boolean flag) {
        return sensorService.getMaybeSensorUpdate(flag);
    }

    // Completable: a flow without items but only a completion or error signal
    @PostMapping(value = "/calibrate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Completable calibrateSensors() {
        return sensorService.calibrateSensors();
    }

    @GetMapping(value = "/backpressure", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flowable<SensorData> getBackpressure(String backpressureStrategy, boolean isFastProducer, int dataSize) {
        return sensorService.getBackpressure(backpressureStrategy, isFastProducer, dataSize);
    }
    @GetMapping(value = "/errorHandling", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flowable<SensorData> getFastSensorUpdatesWithErrorHandling(String errorHandlingFlag) {
        return sensorService.getFastSensorUpdatesWithErrorHandling(errorHandlingFlag);
    }
}
