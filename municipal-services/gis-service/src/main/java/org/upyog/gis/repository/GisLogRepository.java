package org.upyog.gis.repository;

import org.upyog.gis.kafka.Producer;
import org.upyog.gis.model.GisLog;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Repository for GisLog entity that publishes logs to Kafka.
 * 
 * <p>This repository handles the publishing of GIS processing logs to Kafka topics
 * for consumption by other services in the ecosystem using the standard Producer pattern.</p>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class GisLogRepository {

    private final Producer producer;
    
    private static final String GIS_LOG_TOPIC = "save-gis-log";

    /**
     * Publishes a GIS log event to Kafka.
     * 
     * @param gisLog the GIS log object to publish
     */
    public void save(GisLog gisLog) {
        try {
            log.info("Publishing GIS log to Kafka topic: {} for application: {}", 
                    GIS_LOG_TOPIC, gisLog.getApplicationNo());
            
            producer.push(GIS_LOG_TOPIC, gisLog);
            
            log.debug("Successfully published GIS log with ID: {}", gisLog.getId());
        } catch (Exception e) {
            log.error("Failed to publish GIS log to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish GIS log to Kafka", e);
        }
    }
}
