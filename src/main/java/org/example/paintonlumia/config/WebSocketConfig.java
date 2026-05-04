package org.example.paintonlumia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-paint")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback option
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients subscribe to this prefix to receive broadcasts
        registry.enableSimpleBroker("/topic");

        // Clients send messages to this prefix to reach @MessageMapping controllers
        registry.setApplicationDestinationPrefixes("/app");
    }
}