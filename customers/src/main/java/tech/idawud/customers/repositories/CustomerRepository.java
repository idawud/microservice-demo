package tech.idawud.customers.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tech.idawud.customers.models.Customer;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {

}
