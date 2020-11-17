package com.example.demo;

import com.example.demo.config.ExpirationMessagePostProcessor;
import com.example.demo.config.Producer;
import com.rabbitmq.client.MessageProperties;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
class DemoApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Producer pr;


    @Test
    void contextLoads() {
        pr.send();
    }

    @Test
    public void testDelayQueuePerMessageTTL() throws InterruptedException {
        for (int i = 1; i <= 3; i++) {
        	int b=3/0;
        	//
        	int a=1/0;
            long expiration = i * 1000;
            rabbitTemplate.convertAndSend("delay_queue_msg_ttl_name",
                    (Object) ("Message From delay_queue_per_message_ttl with expiration " + expiration), new ExpirationMessagePostProcessor(expiration));
        }
    }

}
