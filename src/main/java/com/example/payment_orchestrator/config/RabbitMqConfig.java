package com.example.payment_orchestrator.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "payment.notification.exchange";
    public static final String QUEUE = "payment.notification.queue";
    public static final String ROUTING_KEY = "payment.notification.routingKey";

    public static final String COMPENSATE_ROUTING_KEY = "payment.compensate.route";
    public static final String COMPENSATE_QUEUE = "payment.compensate.queue";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding(@Qualifier("queue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue compensateQueue() {
        return new Queue(COMPENSATE_QUEUE, true);
    }

    @Bean
    public Binding compensateBinding(@Qualifier("compensateQueue") Queue compensateQueue, DirectExchange exchange) {
        return BindingBuilder.bind(compensateQueue).to(exchange).with(COMPENSATE_ROUTING_KEY);
    }
}
