package tech.idawud.customers.configs;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.idawud.customers.repositories.CustomerRepository;

@Configuration
public class CustomerConfigs {
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
    ApplicationRunner runnerOnKubernetes() {
        return args -> System.out.println("Hello, kubernetes");
    }

    @Bean
    ApplicationRunner runner(CustomerRepository repository) {
        return args -> repository.findAll().subscribe(System.out::println);
    }
}
