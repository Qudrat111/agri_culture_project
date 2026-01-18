package com.agri.order.infrastructure.outbox;

import com.agri.common.outbox.OutboxEvent;
import com.agri.order.infrastructure.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    
    private static final String TOPIC = "procurement.procurementorder.events";
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${procurement.order.outbox.publisher.batch-size:100}")
    private int batchSize;
    
    @Scheduled(fixedDelayString = "${procurement.order.outbox.publisher.fixed-delay:5000}")
    public void publishEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        
        if (events.isEmpty()) {
            return;
        }
        
        int totalEvents = events.size();
        int eventsToProcess = Math.min(totalEvents, batchSize);
        
        log.info("Publishing {} outbox events to Kafka (total unprocessed: {})", eventsToProcess, totalEvents);
        
        int published = 0;
        for (int i = 0; i < eventsToProcess; i++) {
            OutboxEvent event = events.get(i);
            
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event {} to Kafka", event.getId(), ex);
                        } else {
                            log.debug("Event {} published to Kafka successfully", event.getId());
                            markEventAsProcessed(event.getId());
                        }
                    });
                
                published++;
                
            } catch (Exception e) {
                log.error("Error publishing event {} to Kafka", event.getId(), e);
            }
        }
        
        log.info("Sent {} events to Kafka", published);
    }
    
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void markEventAsProcessed(String eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.markAsProcessed();
            outboxEventRepository.save(event);
        });
    }
}
