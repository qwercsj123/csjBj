package com.csj.BIProject.testRabbitmq;

import com.csj.BIProject.Rabitmq.BiConstant;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {


//    public static final String EXCHANGE_NAME = "exampleExchange";
//    public static final String QUEUE_NAME = "exampleQueue";
//    public static final String DLQ_NAME = "deadLetterQueue";
//
//
//    public static final String ROUTING_KEY = "exampleRoutingKey";
//    public static final String DLQ_ROUTING_KEY = "deadLetterRoutingKey";


    @Bean
    public Queue queue() {
        return QueueBuilder.durable(BiConstant.QUEUE_NAME)
               .withArgument("x-dead-letter-exchange", BiConstant.EXCHANGE_NAME)
               .withArgument("x-dead-letter-routing-key", BiConstant.Dlq_Routing_Key)
               .build();
    }


    @Bean
    public Queue deadLetterQueue() {
        return new Queue(BiConstant.DLQ_QUEUE);
    }


    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(BiConstant.EXCHANGE_NAME);
    }


    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BiConstant.ROUTINGKEY);
    }


    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange exchange) {
        return BindingBuilder.bind(deadLetterQueue).to(exchange).with(BiConstant.Dlq_Routing_Key);
    }
}