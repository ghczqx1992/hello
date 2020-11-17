package com.example.demo.config;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    private final Logger logger = (Logger) LoggerFactory.getLogger(RabbitMqConfig.class);

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    private static  final  String DELAY_QUEUE_MSG_TTL_NAME="delay_queue_msg_ttl_name";

    private static  final  String DELAY_EXCHANGE_NAME="delay_exchange_name";

    private static  final  String DELAY_PROCESS_QUEUE_NAME="delay_process_queue_name";


    private static  final  String DELAY_QUEUE_PER_QUEUE_TTL_NAME="delay_queue_per_queue_ttl_name";

    private static  final  String QUEUE_EXPIRATION="10000";





    @Bean
    public RabbitTemplate rabbitTemplates() {
        RabbitTemplate rabbitTemplate=new RabbitTemplate(cachingConnectionFactory);
        Logger log = LoggerFactory.getLogger(RabbitTemplate.class);
        // 消息发送失败返回到队列中, yml需要配置 publisher-returns: true
        rabbitTemplate.setMandatory(true);
        // 消息返回, yml需要配置 publisher-returns: true
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            String correlationId = message.getMessageProperties().getCorrelationId();
            log.info("消息：{} 发送失败, 应答码：{} 原因：{} 交换机: {}  路由键: {}", correlationId, replyCode, replyText, exchange, routingKey);
        });
        // 消息确认, yml需要配置 publisher-confirms: true
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息发送到exchange成功,id: {}",correlationData.getId());
            } else {
                log.info("消息发送到exchange失败,原因: {}", cause);
            }
        });
        return rabbitTemplate;
    }

    @Bean
    public Queue orderQueue2() {
        //创建持久化的队列
        return new Queue("queue-test");
    }

    //注入消息延迟队列
    public Queue delayQueuePerMessageTTL(){
        return QueueBuilder.durable(DELAY_QUEUE_MSG_TTL_NAME)
                .withArgument("x-dead-letter-exchange",DELAY_EXCHANGE_NAME)//DLX,deadLetter发送到exchange
                .withArgument("x-dead-letter-routing-key",DELAY_PROCESS_QUEUE_NAME)//dead letter携带的routing key
                .build();
    }

    //注入队列延迟
    public Queue  delayQueuePerQueueTTL(){
        return QueueBuilder.durable(DELAY_QUEUE_PER_QUEUE_TTL_NAME)
                .withArgument("x-dead-letter-exchange",DELAY_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DELAY_PROCESS_QUEUE_NAME)
                .withArgument("x-message-ttl", QUEUE_EXPIRATION)
                .build();
    }

    @Bean
    public Queue delayProcessQueue() {
        return QueueBuilder.durable(DELAY_PROCESS_QUEUE_NAME)
                .build();
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE_NAME);
    }

    @Bean
    public Binding dlxBinding(Queue delayProcessQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayProcessQueue)
                .to(delayExchange)
                .with(DELAY_PROCESS_QUEUE_NAME);
    }

    @Bean
    public SimpleMessageListenerContainer processContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(cachingConnectionFactory);
        container.setQueueNames(DELAY_PROCESS_QUEUE_NAME); // 监听delay_process_queue
        container.setMessageListener(new MessageListenerAdapter());
        return container;
    }


}
