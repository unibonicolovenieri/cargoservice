package unibo.disi.webgui;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// "Configurazione con WebSocketConfigurer" - struttura del prof
// Il prof usa path "socket" -> ws://localhost:8085/socket
// (il wsminimal.js costruisce l'URL come: "ws://" + host + pathname + "socket")
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandlerDemo wsHandler;

    public WebSocketConfig(WebSocketHandlerDemo wsHandler) {
        this.wsHandler = wsHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Path "socket" come nel wsminimal.js del prof
        registry.addHandler(wsHandler, "/socket").setAllowedOrigins("*");
    }
}