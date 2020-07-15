package xyz.zionjo.littlemall.seckill.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@EnableRabbit
@Configuration
public class MyRabbitConfig {

//    @Autowired
//    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

//    @PostConstruct
//    public void initRabbitTemplate(){
//        rabbitTemplate.setConfirmCallback(
//                new RabbitTemplate.ConfirmCallback() {
//                    @Override
//                    public void confirm(CorrelationData correlationData, boolean b, String s) {
//
//                    }
//                }
//        );
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            @Override
//            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
//
//            }
//        });
//    }
}
