package ru.brainnotfound.backend.config;

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
        // Точка подключения (Handshake)
        // Фронтенд будет подключаться к ws://localhost:8080/ws-grouping
        registry.addEndpoint("/ws-grouping")
                .setAllowedOriginPatterns("*"); // Разрешаем CORS для фронтенда (например, с localhost:3000)
        // .withSockJS(); // Раскомментируйте, если используете SockJS на клиенте
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Префикс для топиков, на которые подписывается клиент
        registry.enableSimpleBroker("/topic");

        // Префикс для сообщений, которые клиент отправляет на сервер (нам пока не нужно, но стандарт)
        registry.setApplicationDestinationPrefixes("/app");
    }
}

