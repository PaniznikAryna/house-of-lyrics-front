package net.houseoflyrics.service;

import com.google.common.net.HttpHeaders;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class ApiService {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080/api")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .filter(dynamicAuthHeaderFilter())
                .build();
    }

    private ExchangeFilterFunction dynamicAuthHeaderFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String jwtToken = "";
            try {
                VaadinSession session = VaadinSession.getCurrent();
                if (session != null && session.getAttribute("jwtToken") != null) {
                    jwtToken = session.getAttribute("jwtToken").toString();
                }
            } catch (Exception e) {
                // Если получение сессии вызвало какую-либо ошибку, оставляем jwtToken пустым
            }
            ClientRequest.Builder builder = ClientRequest.from(request);
            if (!jwtToken.isEmpty()) {
                builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken);
            }
            ClientRequest filteredRequest = builder.build();
            return Mono.just(filteredRequest);
        });
    }
}
