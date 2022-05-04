package tech.idawud.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import tech.idawud.gateway.models.Customer;
import tech.idawud.gateway.models.Order;
import tech.idawud.gateway.services.CrmClient;

@Controller
class CrmGraphqlController {
    private final CrmClient client;

    @Autowired
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
