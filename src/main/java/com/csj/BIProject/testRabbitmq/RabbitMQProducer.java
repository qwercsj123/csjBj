//package com.csj.BIProject.testRabbitmq;
//
//import com.csj.BIProject.Rabitmq.BiConstant;
//import org.springframework.amqp.core.AmqpTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class RabbitMQProducer {
//
//
//    @Autowired
//    private AmqpTemplate rabbitTemplate;
//
//    public void sendMessage(String message) {
//        rabbitTemplate.convertAndSend(BiConstant.EXCHANGE_NAME, BiConstant.ROUTINGKEY, message);
//        System.out.println("Sent message: " + message);
//    }
//}