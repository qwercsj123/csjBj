package com.csj.BIProject.Rabitmq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
public class BiProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    ///发消息
    public void sendMessage(String messageInfo) {
        rabbitTemplate.convertAndSend(BiConstant.EXCHANGE_NAME, BiConstant.ROUTINGKEY, messageInfo);
        log.info("发送的消息"+messageInfo);
    }
}