//package com.csj.BIProject.testRabbitmq;
//
//
//import com.csj.BIProject.Rabitmq.BiConstant;
//import com.rabbitmq.client.Channel;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//
//@Component
//public class RabbitMQConsumer {
//
//
//    @RabbitListener(queues = BiConstant.QUEUE_NAME,ackMode = "MANUAL")
//    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
//        System.out.println("Received message: " +"正常接受消息，消息的内容是:"+message);
//        // 模拟消息处理失败
//        if (message.contains("error")) {
//            System.out.println("失败了");
//            channel.basicNack(deliveryTag,false,false);
//        }
//        channel.basicAck(deliveryTag,false);
//    }
//
//
//    @RabbitListener(queues = BiConstant.DLQ_QUEUE,ackMode = "MANUAL")
//    public void receiveDeadLetterMessage(String message,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
//        System.out.println("Received dead letter message: " + message);
//        channel.basicNack(deliveryTag,false,false);
//    }
//}