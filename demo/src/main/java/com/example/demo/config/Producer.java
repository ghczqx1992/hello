package com.example.demo.config;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Producer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public  void send(){
        for(int i=0;i<100;i++){
            String msg = "hello, 序号: " + i;
            System.out.println("Producer, " + msg);
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            rabbitTemplate.convertAndSend("queue-test", (Object) msg,correlationData);
        }
    }
}
