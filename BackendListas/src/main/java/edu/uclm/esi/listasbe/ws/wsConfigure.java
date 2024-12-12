package edu.uclm.esi.listasbe.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class wsConfigure implements WebSocketConfigurer {
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(new wsChat(), "/wsChat").setAllowedOrigins("*")
				.addInterceptors(new HttpSessionHandshakeInterceptor())
				.addHandler(new wsListas(), "/wsListas").
				addInterceptors(new HttpSessionHandshakeInterceptor());
	}
}