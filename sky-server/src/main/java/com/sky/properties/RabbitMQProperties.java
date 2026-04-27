package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq.listener.custom")
public class RabbitMQProperties {
    private Dlq dlq = new Dlq();
    private Mq mq = new Mq();

    @Data
    public static class Dlq{
        private Integer prefetch;
        private Integer batchSize;
    }

    @Data
    public static class Mq{
        private Integer prefetch;
    }
}
