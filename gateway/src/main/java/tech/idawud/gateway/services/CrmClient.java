package tech.idawud.gateway.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.idawud.gateway.models.Customer;
import tech.idawud.gateway.models.CustomerOrder;
import tech.idawud.gateway.models.Order;

@Service
public class CrmClient {
    public static final String CUSTOMERS_ENDPOINT = "/customers";
    public final String CUSTOMER_SERVICE_URL;
    private final WebClient webClient;
    private final RSocketRequester requester;

    @Autowired
    public CrmClient(WebClient webClient, RSocketRequester requester) {
        this.CUSTOMER_SERVICE_URL = String.format(
                "http://%s:%s",
                System.getenv().getOrDefault("CUSTOMERS_SERVICE_HOST", "localhost"),
                System.getenv().getOrDefault("CUSTOMERS_SERVICE_PORT", "8080")
        );
        this.webClient = webClient;
        this.requester = requester;
    }

    public Flux<Customer> getCustomers() {
        return this.webClient.get()
                .uri(this.CUSTOMER_SERVICE_URL + CUSTOMERS_ENDPOINT)
                .retrieve()
                .bodyToFlux(Customer.class);
    }

    public Flux<Order> getOrders(Integer customerId) {
        return this.requester.route("orders.{customerId}", customerId)
                .retrieveFlux(Order.class);
    }

    public Flux<CustomerOrder> getCustomerOrders() {
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
