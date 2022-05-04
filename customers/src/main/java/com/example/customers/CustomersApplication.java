package com.example.customers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class CustomersApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomersApplication.class, args);
	}

	@Bean
	@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
	ApplicationRunner runnerOnKubernetes() {
		return args -> System.out.println("Hello, kubernetes");
	}
	@Bean
	ApplicationRunner runner(CustomerRepository repository) {
		return args -> {
			repository.save(new Customer(5, "Rahhy")).block();
			repository.findAll().subscribe(System.out::println);
		};
	}
}

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

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

}

record Customer(Integer id, String name) { }
