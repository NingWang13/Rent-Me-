package com.community.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_CALLBACK_QUEUE = "payment.callback.queue";
    public static final String PAYMENT_CALLBACK_ROUTING_KEY = "payment.callback";

    public static final String MESSAGE_EXCHANGE = "message.exchange";
    public static final String MESSAGE_QUEUE = "message.queue";
    public static final String MESSAGE_ROUTING_KEY = "message.send";

    @Bean
    Queue orderTimeoutQueue() {
        return new Queue(ORDER_TIMEOUT_QUEUE, true);
    }

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue()).to(orderExchange()).with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    Queue paymentCallbackQueue() {
        return new Queue(PAYMENT_CALLBACK_QUEUE, true);
    }

    @Bean
    TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    Binding paymentCallbackBinding() {
        return BindingBuilder.bind(paymentCallbackQueue()).to(paymentExchange()).with(PAYMENT_CALLBACK_ROUTING_KEY);
    }

    @Bean
    Queue messageQueue() {
        return new Queue(MESSAGE_QUEUE, true);
    }

    @Bean
    TopicExchange messageExchange() {
        return new TopicExchange(MESSAGE_EXCHANGE);
    }

    @Bean
    Binding messageBinding() {
        return BindingBuilder.bind(messageQueue()).to(messageExchange()).with(MESSAGE_ROUTING_KEY);
    }

    @Bean
    @org.springframework.lang.NonNull
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(@NonNull ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
