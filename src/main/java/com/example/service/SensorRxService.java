package com.example.service;

import com.example.model.SensorData;
import io.reactivex.rxjava3.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SensorRxService {

    private static final Logger logger = LoggerFactory.getLogger(SensorRxService.class);

    // Observable: Emit multiple updates of sensor data
    public Observable<SensorData> getSensorUpdates() {
        List<SensorData> sensors = Arrays.asList(
                new SensorData("sensor-1", 23.5, 60.1, LocalDateTime.now()),
                new SensorData("sensor-2", 24.0, 59.8, LocalDateTime.now()),
                new SensorData("sensor-3", 22.8, 61.2, LocalDateTime.now())
        );
        logger.info("[Observable] Initial sensor data created");
        return Observable.fromIterable(sensors)
                .doOnNext(sensor -> logger.info("[Observable] Emitting initial sensor data: {}", sensor))
                .concatWith(Observable.interval(1, TimeUnit.SECONDS)
                        .map(i -> {
                            SensorData sensor = new SensorData(
                                    "sensor-" + (i + 4),
                                    22.5 + i,
                                    58.5 + i,
                                    LocalDateTime.now());
                            logger.info("[Observable] Generated new sensor data: {}", sensor);
                            return sensor;
                        })
                        .doOnNext(sensor -> logger.info("[Observable] Emitting new sensor data: {}", sensor))
                        .onErrorReturn(throwable -> {
                            // Return a default SensorData object when error occur
                            logger.error("[Observable] Error occurred: " + throwable.getMessage());
                            return new SensorData("error-sensor", 0, 0, LocalDateTime.now());
                        }))
                .take(10)
                .doOnComplete(() -> logger.info("[Observable] End of emitting sensor data. Subscription has completed."))
                //.doOnError()
                ;
    }

    // Flowable: Handle a fast-emitting stream with backpressure
    public Flowable<SensorData> getFastSensorUpdates() {
        logger.info("[Flowable] Starting fast sensor updates");

        return Flowable.interval(500, TimeUnit.MILLISECONDS)
                .map(i -> {
                    SensorData sensorData = new SensorData(
                            "sensor-flow-" + i,
                            23.5 + i,
                            55.0 + i,
                            LocalDateTime.now());
                    logger.info("[Flowable] Generated fast sensor data: {}", sensorData);
                    return sensorData;
                })
                .doOnNext(sensor -> logger.info("[Flowable] Emitting fast sensor data: {}", sensor))
                .onBackpressureBuffer()
                .take(10)
                .doOnComplete(() -> logger.info("[Flowable] End of emitting sensor data. Subscription has completed."));
        // BackpressureStrategy.BUFFER: Buffers all emitted items until they can be consumed.
    }

    // Single: Return a single sensor update
    public Single<SensorData> getSingleSensorUpdate() {
        SensorData sensor = new SensorData("sensor-single", 24.2, 60.0, LocalDateTime.now());

        logger.info("[Single] Generated single sensor data: {}", sensor);
        return Single.just(sensor)
                .doOnSuccess(data -> logger.info("[Single] Emitting single sensor data: {}", data))
                .doAfterSuccess(data ->
                        logger.info("[Single] End of emitting sensor data. Subscription has completed.")
                );
    }

    // Maybe: Return a sensor update or nothing (for conditional updates)
    public Maybe<SensorData> getMaybeSensorUpdate(boolean emit) {
        SensorData sensor = new SensorData("sensor-maybe", 25.0, 65.0, LocalDateTime.now());

        if (emit) {
            logger.info("[Maybe] Generated sensor data: {}", sensor);
            logger.info("[Maybe] Emitting sensor data via Maybe.");
            return Maybe.just(sensor)
                    .doOnSuccess(data -> logger.info("[Maybe] Successfully emitted sensor data: {}", data))
                    .doOnComplete(() -> logger.info("[Maybe] End of emitting sensor data. Subscription has completed."));
        } else {
            logger.info("[Maybe] No sensor data will be emitted (Maybe.empty).");
            return Maybe.<SensorData>empty()
                    .doOnSuccess(data -> logger.info("[Maybe] No sensor data emitted"))
                    .doOnComplete(() -> logger.info("[Maybe] End of emitting sensor data. Subscription has completed."));
        }
    }

    // Completable: Perform a sensor calibration action, return only completion or error
    public Completable calibrateSensors() {
        return Completable.fromRunnable(() -> {
                    logger.info("[Completable] Starting sensor calibration...");
                    try {
                        // Assume processing in here
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        logger.error("[Completable] Sensor calibration was interrupted.", e);
                        Thread.currentThread().interrupt();
                    }
                    logger.info("[Completable] Sensor calibration complete.");
                })
                .doOnComplete(() -> logger.info("[Completable] End of processing. Subscription has completed."));
    }

    // Flowable: Handle a fast-emitting stream with backpressure
    public Flowable<SensorData> getBackpressure(String backpressureStrategy, boolean isFastProducer, int dataSize) {
        logger.info("[Backpressure {}] Starting fast sensor updates, isFastProducer {}, dataSize {}",
                backpressureStrategy, isFastProducer, dataSize);

        int period = isFastProducer ? 1 : 5;
        TimeUnit unit = isFastProducer ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS;

        return Flowable.interval(period, unit)
                .map(i -> {
                    SensorData sensorData = new SensorData(
                            "sensor-flow-" + i,
                            23.5 + i,
                            55.0 + i,
                            LocalDateTime.now());
                    logger.info("[Flowable] Generated fast sensor data: {}", sensorData);
                    return sensorData;
                })
                .doOnNext(sensor -> logger.info("[Flowable] Emitting fast sensor data: {}", sensor))
                .compose(flow -> applyBackpressureStrategy(flow, backpressureStrategy))
                .take(dataSize)
                .doOnComplete(() -> logger.info("[Flowable] End of emitting sensor data. Subscription has completed."));
    }

    private Flowable<SensorData> applyBackpressureStrategy(Flowable<SensorData> flow, String backpressureStrategy) {
        switch (backpressureStrategy) {
            case "DROP":
                return flow.onBackpressureDrop(dropped ->
                        logger.warn("[Flowable] Dropping sensor data due to backpressure: {}", dropped));
            case "LATEST":
                return flow.onBackpressureLatest();
            case "BUFFER":
                return flow.onBackpressureBuffer(10, // Limit to 10 capacity
                        () -> logger.warn("[Flowable] Buffer overflow occurred"),
                        BackpressureOverflowStrategy.DROP_OLDEST); // Optional overflow strategy
            default:
                return flow; // Default is no backpressure handling
        }
    }

    public Flowable<SensorData> getFastSensorUpdatesWithErrorHandling(String errorHandlingFlag) {
        logger.info("[Flowable {}] Starting fast sensor updates", errorHandlingFlag);

        var defaultSensorData = new SensorData("sensor-flow-default", 0.0, 0.0, LocalDateTime.now());
        var defaultFlowable = getFlowableSensorData(errorHandlingFlag);

        switch (errorHandlingFlag) {
            case "onErrorReturn":
                logger.info("Flowable onErrorReturn is for fallback values");
                return defaultFlowable.onErrorReturn(
                        throwable -> defaultSensorData);
            case "doOnError":
                logger.info("Flowable doOnError is for logging or side effects");
                // requires exception handling
                return defaultFlowable.doOnError(throwable -> logger.error("Error occurred: " + throwable.getMessage()));
            case "onErrorResumeNext":
                logger.info("Flowable onErrorResumeNext is for providing an alternative observable");
                return defaultFlowable.onErrorResumeNext(throwable -> {
//                    return Flowable.empty(); // This terminates or completed
//                     or
                return Flowable.just(defaultSensorData);
                });
            default:
                return defaultFlowable;
        }
    }

    private Flowable<SensorData> getFlowableSensorData(String errorFlag) {
        return Flowable.interval(500, TimeUnit.MILLISECONDS)
                .map(i -> {
                    SensorData sensorData = new SensorData(
                            "sensor-flow-" + i,
                            23.5 + i,
                            55.0 + i,
                            LocalDateTime.now());
                    logger.info("[Flowable {}] Generated fast sensor data: {}", errorFlag, sensorData);
                    if (i == 2) {
                        throw new RuntimeException(
                                String.format("[Flowable %s] Simulate an error condition, after emitting 3 records", errorFlag));
                    }
                    return sensorData;
                })
                .doOnNext(sensor -> logger.info("[Flowable {}] Emitting fast sensor data: {}", errorFlag, sensor))
                .onBackpressureBuffer()
                .take(20)
                .doOnComplete(() -> logger.info("[Flowable {}] End of emitting sensor data. Subscription has completed.", errorFlag));
    }
}
