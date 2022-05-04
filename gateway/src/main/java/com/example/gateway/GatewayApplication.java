package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootApplication
public class GatewayApplication {
	public final String CUSTOMER_SERVICE_URL;

	GatewayApplication() {
		this.CUSTOMER_SERVICE_URL = String.format(
		"http://%s:%s", 
			System.getenv().getOrDefault("CUSTOMERS_SERVICE_HOST", "localhost"),
			System.getenv().getOrDefault("CUSTOMERS_SERVICE_PORT", "8080")
		);
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(rs -> rs.path("/proxy")
						.filters(gf -> gf.setPath("/customers")
								.addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
						)
						.uri(this.CUSTOMER_SERVICE_URL))
				.build();
	}

	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
		String orderServiceHost = System.getenv().getOrDefault("ORDERS_SERVICE_HOST", "localhost");
		String orderSericePort = System.getenv().getOrDefault("ORDERS_SERVICE_PORT", "8081");

		return builder.tcp(orderServiceHost, Integer.valueOf(orderSericePort));
	}

	@Bean
	CrmClient client(WebClient webClient, RSocketRequester requester) {
		return new CrmClient(webClient, requester);
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}

record Customer(Integer id, String name) { }

record Order(Integer id, Integer customerId) {}

record CustomerOrder(Customer customer, List<Order> orders) {}

@Controller
@ResponseBody
class CrmRestController {
	private final CrmClient client;

	CrmRestController(CrmClient client) {
		this.client = client;
	}

	@GetMapping("/cos")
	Flux<CustomerOrder> getCustomerOrders() {
		return this.client.getCustomerOrders();
	}
}

@Controller
class CrmGraphqlController {
	private final CrmClient client;

	CrmGraphqlController(CrmClient client) {
		this.client = client;
	}

	@SchemaMapping(typeName = "Query", field = "customers")
	Flux<Customer> customers() {
		return this.client.getCustomers();
	}

	@SchemaMapping
	Flux<Order> orders(Customer customer) {
		return this.client.getOrders(customer.id());
	}
}

class CrmClient {
	public static final String CUSTOMERS_ENDPOINT = "/customers";
	public final String CUSTOMER_SERVICE_URL;
	private final WebClient webClient;
	private final RSocketRequester requester;

	CrmClient(WebClient webClient, RSocketRequester requester) {
		this.CUSTOMER_SERVICE_URL = String.format(
			"http://%s:%s", System.getenv().getOrDefault("CUSTOMERS_SERVICE_HOST", "localhost"),
			 System.getenv().getOrDefault("CUSTOMERS_SERVICE_PORT", "8080")
			);
		this.webClient = webClient;
		this.requester = requester;
	}

	Flux<Customer> getCustomers() {
		return this.webClient.get()
				.uri(this.CUSTOMER_SERVICE_URL + CUSTOMERS_ENDPOINT)
				.retrieve()
				.bodyToFlux(Customer.class);
	}

	Flux<Order> getOrders(Integer customerId) {
		return this.requester.route("orders.{customerId}", customerId)
				.retrieveFlux(Order.class);
	}

	Flux<CustomerOrder> getCustomerOrders() {
		return this.getCustomers()
				.flatMap(customer ->
						Mono.zip(
								Mono.just(customer),
								this.getOrders(customer.id()).collectList()
						)
				)
				.map(tuple -> new CustomerOrder(tuple.getT1(), tuple.getT2()));
	}
}

