package com.sky.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class MqReturnedMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String messageId;
    private String exchange;
    private String routingKey;
    private Integer replyCode;
    private String replyText;
    private String messageBody;
    private LocalDateTime createTime;
}
