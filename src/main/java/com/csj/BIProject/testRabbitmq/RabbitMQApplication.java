//package com.csj.BIProject.testRabbitmq;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//
//@SpringBootApplication
//public class RabbitMQApplication implements CommandLineRunner {
//
//
//    @Autowired
//    private RabbitMQProducer rabbitMQProducer;
//
//
//    public static void main(String[] args) {
//        SpringApplication.run(RabbitMQApplication.class, args);
//    }
//
//
//    @Override
//    public void run(String... args) throws Exception {
//        rabbitMQProducer.sendMessage("Hello, RabbitMQ!");
//        rabbitMQProducer.sendMessage("error message, this should go to DLQ!");
//    }
//}