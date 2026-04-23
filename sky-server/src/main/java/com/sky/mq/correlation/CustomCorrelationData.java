package com.sky.mq.correlation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.amqp.rabbit.connection.CorrelationData;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomCorrelationData extends CorrelationData {
    private Long userId;
    private String jsonMessageBody;

    public CustomCorrelationData(String messageId, Long userId, String jsonMessageBody) {
        super(messageId);
        this.userId = userId;
        this.jsonMessageBody = jsonMessageBody;
    }
}
