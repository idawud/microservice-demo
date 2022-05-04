package tech.idawud.gateway.configs;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.reactive.function.client.WebClient;
import tech.idawud.gateway.services.CrmClient;

@Configuration
public class GatewayConfigs {

    @Bean
    RouteLocator gateway(RouteLocatorBuilder builder) {
        final String customerServiceUrl = String.format(
                "http://%s:%s",
                System.getenv().getOrDefault("CUSTOMERS_SERVICE_HOST", "localhost"),
                System.getenv().getOrDefault("CUSTOMERS_SERVICE_PORT", "8080")
        );

        return builder.routes()
                .route(rs -> rs.path("/proxy")
                        .filters(gf -> gf.setPath("/customers")
                                .addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        )
                        .uri(customerServiceUrl))
                .build();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        String orderServiceHost = System.getenv().getOrDefault("ORDERS_SERVICE_HOST", "localhost");
        String orderServicePort = System.getenv().getOrDefault("ORDERS_SERVICE_PORT", "8081");

        return builder.tcp(orderServiceHost, Integer.parseInt(orderServicePort));
    }

    @Bean
    CrmClient client(WebClient webClient, RSocketRequester requester) {
        return new CrmClient(webClient, requester);
    }
}
