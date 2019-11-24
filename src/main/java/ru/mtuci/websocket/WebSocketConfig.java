package ru.mtuci.websocket;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final EventHandler eventHandler;

  public static class Consts {

    public static final String GAME_ID_ATTRIBUTE = "gameId";

  }

  public WebSocketConfig(EventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(eventHandler, "/game/*")
        .addInterceptors(interceptor())
        .setAllowedOrigins("*");
  }

  @Bean
  public HandshakeInterceptor interceptor() {
    return new HandshakeInterceptor() {
      public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
          WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // Получить gameId из url
        String path = request.getURI().getPath();
        String gameId = path.substring(path.lastIndexOf('/') + 1);

        // Сохранение gameId в атрибутах
        attributes.put(Consts.GAME_ID_ATTRIBUTE, gameId);
        return true;
      }

      public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
          WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do after handshake
      }
    };
  }

  @Bean
  public ServletServerContainerFactoryBean createWebSocketContainer() {
    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
    container.setMaxSessionIdleTimeout(5 * 60 * 1000L);
    return container;
  }
}
