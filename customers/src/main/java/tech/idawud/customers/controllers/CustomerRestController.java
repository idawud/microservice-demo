package tech.idawud.customers.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.idawud.customers.models.Customer;
import tech.idawud.customers.repositories.CustomerRepository;

@ResponseBody
@Controller
class CustomerRestController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerRestController.class);
    private final CustomerRepository customerRepository;
    private final ApplicationContext context;

    @Autowired
    CustomerRestController(CustomerRepository customerRepository, ApplicationContext context) {
        this.customerRepository = customerRepository;
        this.context = context;
    }

    @GetMapping("/customers")
    Flux<Customer> gelAllCustomers() {
        return this.customerRepository.findAll();
    }

    @GetMapping("/down")
    Mono<Void> down() {
        AvailabilityChangeEvent.publish(this.context, LivenessState.BROKEN);
        return Mono.empty();
    }
}
